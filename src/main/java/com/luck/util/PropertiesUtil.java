package com.luck.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesUtil {
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
         pps.load(in);
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
