package com.ndthuan.nucrawler.queueing;

public interface JobQueue {
    Job poll();
    boolean add(Job job);
    void clear();
    boolean isEmpty();
}
