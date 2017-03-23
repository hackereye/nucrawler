package com.ndthuan.nucrawler.api;

public interface JobQueue {
    Job poll();
    boolean add(Job job);
    void clear();
    boolean isEmpty();
}
