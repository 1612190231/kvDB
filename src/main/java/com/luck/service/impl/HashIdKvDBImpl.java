package com.luck.service.impl;

import com.luck.service.SimpleKvDB;
import com.luck.util.LogFile;
import com.luck.util.Util;
import org.apache.log4j.Logger;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

public class HashIdKvDBImpl implements SimpleKvDB {
    /**
     * HashIdxKvDb.java
     * 追加写
     * 使用Hash索引提升读性能
     */
    private Logger logger = Logger.getLogger(this.getClass());
    private LogFile curLog;
    // 索引，value为该key对应的数据在文件中的offset
    private Map<String, Long> idx;

    public HashIdKvDBImpl(LogFile logFile, Map<String, Long> idx){
        this.curLog = logFile;
        this.idx = idx;
    }

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

    @Override
    public String get(String key) {
        if (!idx.containsKey(key)) {
            return "";
        }
        // step1: 读取索引
        long offset = idx.get(key);
        // step2: 根据索引读取value
        String record = curLog.read(offset);
        return Util.valueOf(record);
    }

    @Override
    public void set(String key, String value) {
        String record = Util.composeRecord(key, value);
        long curSize = curLog.size();
        // step1: 追加写数据
        if (curLog.append(record) != 0) {
            // step2: 更新索引
            idx.put(key, curSize);
        }
    }
}
