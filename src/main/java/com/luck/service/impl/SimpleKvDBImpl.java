package com.luck.service.impl;
/**
 * SimpleKvDBImpl.java
 * 追加写
 * 全文件扫描读
 */
import com.luck.service.SimpleKvDB;
import com.luck.util.LogFile;
import com.luck.util.Util;
import org.apache.log4j.Logger;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleKvDBImpl implements SimpleKvDB {
    private Logger logger = Logger.getLogger(this.getClass());
    private LogFile logFile = new LogFile(FileSystems.getDefault().getPath("src/main/resources", "access.txt"));

    @Override
    public String get(String key) {
        try (Stream<String> lines = logFile.lines()) {
            // step1: 筛选出该key对应的的所有value（对应grep "^$1," database）
            List<String> values = lines
                    .filter(line -> Util.matchKey(key, line))
                    .collect(Collectors.toList());
            // step2: 选取最新值（对应 sed -e "s/^$1,//" | tail -n 1）
            String value = Util.valueOf(values.get(0));
            logger.info("getInfo: " + value);
            return value;
        }
    }

    @Override
    public void set(String key, String value) {
        // step1: 追加写（对应 echo "$1,$2" >> database）
        String record = Util.composeRecord(key, value);
        long length = logFile.append(record);
        logger.info("recordLength: " + length);
    }
}
