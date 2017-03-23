package com.ndthuan.nucrawler.history;

import com.ndthuan.nucrawler.api.JobHistory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InterProcessJobHistory implements JobHistory {
    private final Map<String, Byte> history = new ConcurrentHashMap<>();

    @Override
    public boolean hasCrawled(URI uri) {
        return history.containsKey(uri.toString());
    }

    @Override
    public void add(URI uri) {
        history.put(uri.toString(), (byte) 0);
    }

    @Override
    public void clear() {
        history.clear();
    }
}
