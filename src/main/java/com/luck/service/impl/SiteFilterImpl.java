package com.luck.service.impl;

import com.luck.entity.TraceInfo;
import com.luck.service.SiteFilter;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SiteFilterImpl implements SiteFilter {
    private double getDistanceMeter(GlobalCoordinates gpsFrom, GlobalCoordinates gpsTo, Ellipsoid ellipsoid){

        //创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(ellipsoid, gpsFrom, gpsTo);

        return geoCurve.getEllipsoidalDistance();
    }

    public List<TraceInfo> siteFilter(List<TraceInfo> siteList){
        List<TraceInfo> traceInfos = new ArrayList<>();
        for (TraceInfo traceInfo : siteList){
            if (traceInfo.getLatitude() < 35.2582116 &&
                    traceInfo.getLatitude() > 35.0582116 &&
                    traceInfo.getLongitude() < 119.431599 &&
                    traceInfo.getLongitude() > 119.231599 ){
                traceInfos.add(traceInfo);
            }
        }
        return traceInfos;
    }

    public List<TraceInfo> calculateFilter(List<TraceInfo> siteList){
        List<TraceInfo> traceInfos = new ArrayList<>();
        // 日钢经纬度
        GlobalCoordinates target = new GlobalCoordinates(35.1582116, 119.331599);

        for (TraceInfo traceInfo : siteList){
            GlobalCoordinates source = new GlobalCoordinates(traceInfo.getLatitude(), traceInfo.getLongitude());
            traceInfo.setDistance(getDistanceMeter(source, target, Ellipsoid.WGS84));
            if ( traceInfo.getDistance() < 10000) {
                traceInfos.add(traceInfo);
            }
        }
        return traceInfos;
    }
}
