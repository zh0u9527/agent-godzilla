import com.sun.tools.attach.VirtualMachine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class BootstrapId {

    /**
     * 运行jar时，应该确保运行jar的jdk版本与需要附件项目的jdk版本一致，否则会出现 Non-numeric value found - int expected
     * @param args
     */
    public static void main(String[] args){
        try {
            printHelp(args);

            // Auto find target process
            String pid = findTargetPID(args[0]);
            if (pid == null) {
                AgentMainTest.logger.warning("[-] Target PID not found.");
                System.exit(1);
            }

            // Get the current agent jar path
            String jar = getJar(BootstrapId.class);

            System.out.println("jar = " + jar);

            // Attach to target process
            AgentMainTest.logger.info("[+] Trying to attach to process: " + pid);
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(jar);
            vm.detach();

            AgentMainTest.logger.info("[√] Agent successfully loaded into process: " + pid);
        }
        catch (Exception e) {
            AgentMainTest.logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void printHelp(String[] args) {
        if (args.length != 1) {
            AgentMainTest.logger.info("Usage: java -jar xxx.jar [id|processName]");
            System.out.println();
            AgentMainTest.logger.info("Examples:");
            AgentMainTest.logger.info("  java -jar xxx.jar 123456         # PID");
            AgentMainTest.logger.info("  java -jar xxx.jar web.jar        # processLine.contains(arg.toLowerCase(jarName))");
            AgentMainTest.logger.info("  java -jar xxx.jar Application    # processLine.contains(arg.toLowerCase(mainClassName))");
            System.out.println();
            AgentMainTest.logger.info("Tips:");
            AgentMainTest.logger.info("  use jps -l to list Java processes");
            AgentMainTest.logger.info("  linux: ps -eo pid,cmd | grep '[j]ava'");
            AgentMainTest.logger.info("  windows: jps -l");
            AgentMainTest.logger.info("  godzilla:\n\tUser-Agent: Ioyrns\n\tpassword: pass\n\tkey: key");
            System.err.println();
            AgentMainTest.logger.info("  When starting the agent, the JDK version must match the JDK version of the target application.\n" +
                    "For example, if the target project is running on JDK 17, the agent must also be started using JDK 17.");
            System.exit(1); // 直接退出程序
        }
    }


    /**
     * 1、判断arg是否为字符串型数字，如果是则直接返回 *
     * 2、判断当前系统类型，则执行jps -l（如果出现异常则执行ps -eo pid,cmd或执行Windows下的相关命令来获取进程列表）来检测是否包含arg参数值，如果是则返回进程列表中对应的进程id
     */
    public static String findTargetPID(String arg) {
        // 1. Check if arg is a numeric PID
        if (arg.matches("\\d+")) {
            return arg;
        }

        List<String> candidates = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        try {
            Process process;
            if (os.contains("win")) {
                // Windows: try jps -l first, fallback to wmic
                try {
                    process = Runtime.getRuntime().exec("jps -l");
                } catch (Exception e) {
                    process = Runtime.getRuntime().exec(
                            new String[]{"cmd.exe", "/c", "wmic process where \"name='java.exe'\" get ProcessId,CommandLine"});
                }
            } else {
                // Linux/macOS: try jps -l first, fallback to ps
                try {
                    process = Runtime.getRuntime().exec("jps -l");
                } catch (Exception e) {
                    process = Runtime.getRuntime().exec(new String[]{"bash", "-c", "ps -eo pid,cmd | grep '[j]ava'"});
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String pid = null;
                String mainClass = null;

                // Parse jps -l output: PID MainClass
                if (line.matches("^\\d+\\s+.+$")) {
                    String[] parts = line.split("\\s+", 2);
                    pid = parts[0];
                    mainClass = parts[1];
                } else {
                    // Parse Windows wmic or ps output
                    String[] parts = line.split("\\s+", 2);
                    if (parts.length >= 2) {
                        pid = parts[0];
                        mainClass = parts[1];
                    }
                }

                if (pid == null || mainClass == null) continue;

                // Exclude self process
                if (mainClass.contains("BootstrapId")) continue;

                // Match target process
                if (mainClass.toLowerCase().contains(arg.toLowerCase())) {
                    candidates.add(pid + " [" + mainClass + "]");
                }
            }

            if (candidates.isEmpty()) {
                AgentMainTest.logger.warning("[-] No target Java process found. Make sure JVM is running and argument is correct.");
                return null;
            }

            AgentMainTest.logger.info("[*] Detected candidate Java processes:");
            for (String c : candidates) {
                AgentMainTest.logger.info("    " + c);
            }

            // Return the first candidate PID
            return candidates.get(0).split(" ")[0];

        } catch (Exception e) {
            AgentMainTest.logger.severe(e.getMessage());
            throw new RuntimeException("Failed to get JVM process list: " + e.getMessage(), e);
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
