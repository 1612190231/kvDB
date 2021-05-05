package com.luck.util;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// 追加写的日志文件，存储数据库数据
public class LogFile {

    private Logger logger = Logger.getLogger(this.getClass());

    // 文件所在路径
    private Path path;

    public LogFile(Path path){
        this.path = path;
    }

    //读取存储文件的所有内容
    public Stream<String> lines(){
        List<String> list = new ArrayList<String>();
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")){
            long length = file.length();//获得文件长度
            long count = 0;//定义行数
            if (length == 0L) {//文件内容为空
                return list.stream();
            } else {
                // 开始位置
                long pos = length - 1;
                while (pos > 0) {
                    pos--;
                    // 开始读取
                    file.seek(pos);
                    //有换行符，则读取
                    if (file.readByte() == '\n') {
                        String line = new String(file.readLine().getBytes("ISO-8859-1"),"UTF-8");
                        list.add(line);
                        count++;
                    }
                }

                if (pos == 0) {
                    file.seek(0);
                    list.add(new String(file.readLine().getBytes("ISO-8859-1"),"UTF-8"));
                }
            }
            Stream<String> stream = list.stream();
            return stream;
        } catch (IOException e){
            e.printStackTrace();
        }
        return list.stream();
    }

    // 向日志文件中写入一行记录，自动添加换行符
    // 返回成功写入的字节大小
    public long append(String record) {
        try {
            record += System.lineSeparator();
            Files.write(path, record.getBytes(), StandardOpenOption.APPEND);

            logger.info("setInfo: " + record);
            return record.getBytes().length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 读取offset为起始位置的一行
    public String read(long offset) {
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            file.seek(offset);
            return file.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public long size(){
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            return file.length();
        } catch (IOException e){
            e.printStackTrace();
        }
        return 0;
    }

}
