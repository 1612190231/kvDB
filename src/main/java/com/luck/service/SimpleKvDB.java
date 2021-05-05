package com.luck.service;

public interface SimpleKvDB {
    String get(String key);

    void set(String key, String value);

}
