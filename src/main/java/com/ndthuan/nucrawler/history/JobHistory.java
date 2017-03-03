package com.ndthuan.nucrawler.history;

import java.net.URI;

public interface JobHistory {
    boolean hasCrawled(URI uri);

    void add(URI uri);

    void clear();
}
