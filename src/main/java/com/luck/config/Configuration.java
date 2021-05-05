package com.luck.config;

import com.luck.util.LogFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map.Entry;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.*;

public class Configuration {
    private LogFile curLog = new LogFile(FileSystems.getDefault().getPath("src/main/resources", "access.txt"));;
    private Map<String, Long> idx;
    private ClassLoader classLoader;
    private String filePath = System.getProperty("user.dir") + "src/main/resources/map.properties";

    public LogFile getCurLog() {
        return curLog;
    }

    public void setCurLog(LogFile curLog) {
        this.curLog = curLog;
    }

    public Map<String, Long> getIdx() {
        return idx;
    }

    public void setIdx(Map<String, Long> idx) {
        this.idx = idx;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Configuration(){
        this.idx = this.getProperties();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static Configuration create() {
        Configuration conf = new Configuration();
        conf.setClassLoader(Configuration.class.getClassLoader());
        return conf;
    }

    //获取properties文件中的内容,并返回map
    public Map<String, Long> getProperties() {
        Map<String, Long> map = new HashMap<String, Long>();
        InputStream in = null;
        Properties p = new Properties();;
        try {
            in = new BufferedInputStream(new FileInputStream(new File(filePath)));
            p.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Entry<Object, Object>> entrySet = p.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            map.put((String) entry.getKey(), (Long) entry.getValue());
        }
        return map;
    }


}
