import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AgentMainTest {

    public static void agentmain(String agentArgs, Instrumentation inst) {

        logger.info("init agent");
        // 添加类转换器
        inst.addTransformer(new MyTransformer(), true);

        logger.info("agent successfully loaded into agent");
        // 遍历所有已加载的类
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (AgentMainTest.WEB_SERVER_LIST.contains(clazz.getName())) {
                try {
                    logger.info("find web server " + clazz.getName());
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    logger.severe("exception retransforming class: " + clazz.getName());
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        logger.info("success...");
    }

    // SpringMVC框架下Web中间件在进入自定义Filter之前最后一个Filter
    public static final ArrayList<String> WEB_SERVER_LIST = new ArrayList<String>() {{
        add("org.apache.catalina.core.ApplicationFilterChain");         // Tomcat / GlassFish
        add("io.undertow.servlet.core.ManagedFilter");                  // Undertow
        add("org.eclipse.jetty.servlet.FilterHolder");                  // Jetty    jdk8
        add("org.eclipse.jetty.ee10.servlet.FilterHolder");             // Jetty    jdk17
        add("weblogic.servlet.internal.FilterChainImpl");               // WebLogic
        add("com.caucho.server.dispatch.FilterChainImpl");              // Resin
        add("com.ibm.ws.webcontainer.filter.WebAppFilterManager");      // WebSphere
        add("com.apusic.web.filter.FilterChainImpl");                   // Apusic
        add("org.apache.catalina.core.ApplicationFilterChain");         // BES
        add("com.inforsuite.web.filter.FilterChainImpl");               // InforSuite
        add("com.tongweb.catalina.core.ApplicationFilterChain");        // TongWeb
    }};

    // agent调试日志文件，会在执行agent同一目录下生成
    public static final String LOG_FILENAME = "agentLog.txt";

    // 日志文件是否为追加模式
    public static final boolean LOG_FILENAME_APPEND_SWITCH = true;

    // 请求头参数配置
    public static final String HEADER_PARAM = "User-Agent";

    // 请求头参数值配置
    public static final String HEADER_PARAM_VALUE = "Ioyrns";


    public static final Logger logger = Logger.getLogger("AgentLogger");



    // 初始化日志配置
    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILENAME, LOG_FILENAME_APPEND_SWITCH); // 追加模式
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(true); // 输出到控制台
            logger.setLevel(Level.INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
