package com.luck.util;

import java.sql.Timestamp;

public class Util {

    public static String valueOf(String record){
        String value = record.split(": ")[1].split("timeStamp")[0];
        return value;
    }

    public static String composeRecord(String key, String value){
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        return key + ": " + value + "   timeStamp: " + currentTime;
    }

    public static boolean matchKey(String key, String lines){
        return key.equals(lines.split(": ")[0]);
    }
}
