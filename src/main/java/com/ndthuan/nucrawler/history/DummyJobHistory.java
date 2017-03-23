package com.ndthuan.nucrawler.history;

import com.ndthuan.nucrawler.api.JobHistory;

import java.net.URI;

public class DummyJobHistory implements JobHistory {
    @Override
    public boolean hasCrawled(URI uri) {
        return false;
    }

    @Override
    public void add(URI uri) {

    }

    @Override
    public void clear() {

    }
}
