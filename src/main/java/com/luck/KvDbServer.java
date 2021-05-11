package com.luck;

import com.luck.config.Configuration;
import com.luck.entity.TraceInfo;
import com.luck.service.impl.HashIdKvDBImpl;
import com.luck.service.impl.SiteFilterImpl;
import com.luck.util.FileUtil;
import com.luck.util.LogFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KvDbServer {
    public static void main(String[] args) throws IOException {
        // 初始化
        Configuration conf = Configuration.create();
        Map<String, Long> idx = conf.getIdx();
        LogFile logFile = conf.getCurLog();
        HashIdKvDBImpl hashIdKvDB = new HashIdKvDBImpl(logFile, idx);

        // 读取数据
        List<String> siteLists = FileUtil.readLogByList(System.getProperty("user.dir") + "/src/main/resources/test1000-1.txt");
        List<TraceInfo> traceInfos = new ArrayList<>();
        for (String sites : siteLists){
            TraceInfo traceInfo = new TraceInfo();
            traceInfo.setTruckNo(sites.split("\\t")[0]);
            traceInfo.setLatitude(Double.valueOf(sites.split("\\t")[1]));
            traceInfo.setLongitude(Double.valueOf(sites.split("\\t")[2]));
            traceInfos.add(traceInfo);
        }
        SiteFilterImpl siteFilter = new SiteFilterImpl();

        // 数据过滤
        traceInfos = siteFilter.siteFilter(traceInfos);

        // 插入数据
        for (TraceInfo traceInfo : traceInfos){
            String truckNo = '1' + traceInfo.getTruckNo();
            String site = String.valueOf(traceInfo.getLatitude()) + ',' + traceInfo.getLongitude() +  ',' + traceInfo.getDistance();
            hashIdKvDB.set(truckNo, site);
        }

        //


        conf.close();
    }
}
