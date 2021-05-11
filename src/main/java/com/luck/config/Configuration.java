package com.luck.config;

import com.luck.util.LogFile;
import com.luck.util.PropertiesUtil;

import java.io.*;
import java.util.Map.Entry;
import java.nio.file.FileSystems;
import java.util.*;

public class Configuration {
    private LogFile curLog = new LogFile(FileSystems.getDefault().getPath("src/main/resources", "access.txt"));;
    private Map<String, Long> idx;
    private ClassLoader classLoader;
    private String filePath = System.getProperty("user.dir") + "/src/main/resources/map.properties";

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

    private Configuration(){
        this.idx = PropertiesUtil.getProperties(this.filePath);
    }

    private void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static Configuration create() {
        Configuration conf = new Configuration();
        conf.setClassLoader(Configuration.class.getClassLoader());
        return conf;
    }

    public void close() throws IOException {
        PropertiesUtil.setProperties(this.filePath, this.idx);
    }


}
