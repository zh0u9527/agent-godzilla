import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import java.io.FileWriter;
import java.io.IOException;

public class AgentMainTest {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        // 添加类转换器
        inst.addTransformer(new MyTransformer(), true);

        // 输出文件路径（可根据需要修改）
        String filePath = "loaded_classes.txt";

        try (FileWriter writer = new FileWriter(filePath, false)) {
            // 遍历所有已加载类
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                try {
                    ClassLoader loader = clazz.getClassLoader();
                    String loaderName = (loader == null) ? "BootstrapClassLoader" : loader.toString();

                    writer.write(clazz.getName() + "  |  " + loaderName + System.lineSeparator());

                    // 如果是目标类则重转换
                    if (clazz.getName().equals("org.apache.catalina.core.ApplicationFilterChain")) {
                        try {
                            System.out.println("Loading application filter chain...");
                            inst.retransformClasses(clazz);
                        } catch (UnmodifiableClassException e) {
                            writer.write("[WARN] Failed to retransform: " + clazz.getName() + System.lineSeparator());
                        }
                    }
                } catch (Throwable t) {
                    // 防止某些类信息异常导致中断
                    writer.write("[ERROR] " + clazz.getName() + " : " + t.getMessage() + System.lineSeparator());
                }
            }

            writer.write("=== Completed listing all classes ===" + System.lineSeparator());
            System.out.println("ClassLoader info saved to: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("success");
    }
}
