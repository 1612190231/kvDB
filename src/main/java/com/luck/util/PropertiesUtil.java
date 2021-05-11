package com.luck.util;

import java.io.*;
import java.util.*;

public class PropertiesUtil {

    public static final Properties p = new Properties();
    public static final String path = System.getProperty("user.dir") + "/src/main/resources/map.properties";


    /**
     * 通过类装载器 初始化Properties
     */
    public static void init(String filePath) {
        //转换成流
        InputStream inputStream = null;
        try {
            //从输入流中读取属性列表（键和元素对）
            inputStream = new BufferedInputStream(new FileInputStream(new File(filePath)));
            p.load(new InputStreamReader(inputStream, "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 通过key获取value
     * @param key
     * @return
     */
    public static String get(String key) {
        return p.getProperty(key);
    }
    /**
     * 修改或者新增key
     * @param key
     * @param value
     */
    public static void update(String key, String value) {
        p.setProperty(key, value);
        FileOutputStream oFile = null;
        try {
            oFile = new FileOutputStream(path);
            //将Properties中的属性列表（键和元素对）写入输出流
            p.store(new OutputStreamWriter(oFile, "utf-8"), "");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 通过key删除value
     * @param key
     */
    public static void delete(String key) {
        p.remove(key);
        FileOutputStream oFile = null;
        try {
            oFile = new FileOutputStream(path);
            p.store(oFile, "");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 循环所有key value
     */
    public static void list() {
        Enumeration en = p.propertyNames(); //得到配置文件的名字
        while(en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = p.getProperty(strKey);
            System.out.println(strKey + "=" + strValue);
        }
    }

    //写入Properties信息
    public static void setProperties1 (String filePath, Map<String, Long> idx) throws IOException {
        PropertiesUtil.init(filePath);

        for(Map.Entry<String, Long> entry : idx.entrySet()){
            //修改
            PropertiesUtil.update(entry.getKey(),String.valueOf(entry.getValue()));
//            System.out.println(PropertiesUtil.get("password"));
            //以适合使用 load 方法加载到 Properties 表中的格式，
            //将此 Properties 表中的属性列表（键和元素对）写入输出流
            System.out.println(entry.getKey()+":"+ entry.getValue());
        }
    }

    //获取properties文件中的内容,并返回map
    public static Map<String, Long> getProperties(String filePath) {
        Map<String, Long> map = new HashMap<String, Long>();
        InputStream in = null;
        Properties p = new Properties();;
        try {
            in = new BufferedInputStream(new FileInputStream(new File(filePath)));
            p.load(new InputStreamReader(in, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Map.Entry<Object, Object>> entrySet = p.entrySet();
        for (Map.Entry<Object, Object> entry : entrySet) {
            map.put((String) entry.getKey(), Long.valueOf((String) entry.getValue()));
        }
        return map;
    }
    //写入Properties信息
    public static void setProperties (String filePath, Map<String, Long> idx) throws IOException {
        Properties pps = new Properties();

        InputStream in = new FileInputStream(filePath);
        //从输入流中读取属性列表（键和元素对）
        pps.load(new InputStreamReader(in, "utf-8"));
        //调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
        //强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
        OutputStream out = new FileOutputStream(filePath);
        for(Map.Entry<String, Long> entry : idx.entrySet()){
            pps.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            //以适合使用 load 方法加载到 Properties 表中的格式，
            //将此 Properties 表中的属性列表（键和元素对）写入输出流
            System.out.println(entry.getKey()+":"+ entry.getValue());
        }
        pps.store(new OutputStreamWriter(out, "utf-8"), "111");
        out.close();
    }
}

