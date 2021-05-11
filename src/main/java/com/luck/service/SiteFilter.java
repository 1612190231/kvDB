package com.luck.service;

import com.luck.entity.TraceInfo;

import java.util.List;

public interface SiteFilter {
    List<TraceInfo> siteFilter(List<TraceInfo> siteList);

}
