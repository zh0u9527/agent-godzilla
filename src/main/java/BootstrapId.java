import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class BootstrapId {

    public static void main(String[] args)
            throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {

        // 自动查找目标进程
        String pid = findTargetPID();

        // 获取当前 agent jar 路径
        String jar = getJar(BootstrapId.class);

        // 附加到目标进程
        System.out.println("[+] 尝试附加到进程: " + pid);
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(jar);
        vm.detach();

        System.out.println("[√] Agent 已成功加载到进程：" + pid);
    }

    /**
     * 自动查找可注入的 Java 进程（支持 Tomcat / Spring Boot）
     */
    public static String findTargetPID() {
        List<String> candidates = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("jps -l");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(" ");
                if (parts.length < 2) continue;

                String pid = parts[0];
                String mainClass = parts[1];

                // 排除自身进程
                if (mainClass.contains("BootstrapId")) continue;

                // 匹配常见 Java Web 程序
                if (mainClass.contains("org.apache.catalina.startup.Bootstrap")   // Tomcat
                        || mainClass.contains("org.springframework.boot.loader.JarLauncher") // Spring Boot
                        || mainClass.toLowerCase().contains("spring")               // Spring-based
                        || mainClass.toLowerCase().contains("com.application")) {       // 自定义 main
                    candidates.add(pid + " [" + mainClass + "]");
                }
            }

            if (candidates.isEmpty()) {
                System.err.println("[-] 未找到 Spring Boot 或 Tomcat 进程。请确保目标 JVM 已启动。");
                return null;
            }

            System.out.println("[*] 检测到候选 Java 进程：");
            for (String c : candidates) {
                System.out.println("    " + c);
            }

            // 默认取第一个候选（如果多个你也可以手动选择）
            return candidates.get(0).split(" ")[0];

        } catch (Exception e) {
            throw new RuntimeException("获取 JVM 进程列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取当前类所在的 Jar 路径
     */
    public static String getJar(Class<?> clazz) {
        ProtectionDomain pd = clazz.getProtectionDomain();
        URL location = pd.getCodeSource().getLocation();
        String path = location.getPath();
        if (System.getProperty("os.name").toLowerCase().contains("win") && path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
