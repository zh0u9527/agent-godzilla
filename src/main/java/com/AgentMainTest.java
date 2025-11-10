package com;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMainTest {

    public static void agentmain(String agentArgs, Instrumentation inst) {

        Constant001.logger.info("init agent");
        // 添加类转换器
        inst.addTransformer(new MyTransformer(), true);

        Constant001.logger.info("agent successfully loaded into agent");
        // 遍历所有已加载的类
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (Constant001.WEB_SERVER_LIST.contains(clazz.getName())) {
                try {
                    Constant001.logger.info("find web server " + clazz.getName());
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    Constant001.logger.severe("exception retransforming class: " + clazz.getName());
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        Constant001.logger.info("success...");
    }

}
