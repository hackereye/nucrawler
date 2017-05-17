package com.ndthuan.nucrawler.api;

public interface JobQueue {
    Job poll() throws Throwable;
    boolean add(Job job) throws Throwable;
    void clear() throws Throwable;
    boolean isEmpty() throws Throwable;
}
