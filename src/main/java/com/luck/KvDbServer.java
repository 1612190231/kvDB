package com.luck;

import com.luck.config.Configuration;
import com.luck.service.impl.HashIdKvDBImpl;
import com.luck.util.LogFile;

import java.util.Map;

public class KvDbServer {
    public static void main(String[] args) {
        Configuration conf = Configuration.create();
        Map<String, Long> idx = conf.getIdx();
        LogFile logFile = conf.getCurLog();
        HashIdKvDBImpl hashIdKvDB = new HashIdKvDBImpl();
        hashIdKvDB.setCurLog(logFile);
        hashIdKvDB.setIdx(idx);

        hashIdKvDB.set("LastName", "Li");
        System.out.println(hashIdKvDB.get("LastName"));
    }
}
