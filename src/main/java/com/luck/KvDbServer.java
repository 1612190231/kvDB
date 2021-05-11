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
        Configuration conf = Configuration.create();
        Map<String, Long> idx = conf.getIdx();
        LogFile logFile = conf.getCurLog();
        HashIdKvDBImpl hashIdKvDB = new HashIdKvDBImpl();
        hashIdKvDB.setCurLog(logFile);
        hashIdKvDB.setIdx(idx);
        List<String> siteLists = FileUtil.readLogByList(System.getProperty("user.dir") + "/src/main/resources/test1000-1.txt");
        List<TraceInfo> traceInfos = new ArrayList<>();
        for (String sites : siteLists){
            TraceInfo traceInfo = new TraceInfo();
            traceInfo.setTruckNo(sites.split("\\t")[0]);
//            String site = sites.split("\\t")[1] + ',' + sites.split("\\t")[2];
            traceInfo.setLatitude(Double.valueOf(sites.split("\\t")[1]));
            traceInfo.setLongitude(Double.valueOf(sites.split("\\t")[2]));
//            hashIdKvDB.set(truckNo, site);
//            System.out.println(truckNo + '：' + site);
            traceInfos.add(traceInfo);
        }
        SiteFilterImpl siteFilter = new SiteFilterImpl();
        traceInfos = siteFilter.siteFilter(traceInfos);
        traceInfos = siteFilter.calculateFilter(traceInfos);
        for (TraceInfo traceInfo : traceInfos){
            String truckNo = '1' + traceInfo.getTruckNo();
            String site = String.valueOf(traceInfo.getLatitude()) + ',' + traceInfo.getLongitude() +  ',' + traceInfo.getDistance();
            hashIdKvDB.set(truckNo, site);
            System.out.println(truckNo + '：' + site);
        }
        conf.close();
    }
}
