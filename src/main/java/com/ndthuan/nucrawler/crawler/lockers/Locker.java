package com.ndthuan.nucrawler.crawler.lockers;

public interface Locker {
    boolean acquireLock(String key);

    void unlock(String key);
}
