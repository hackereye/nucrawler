package com.ndthuan.nucrawler.queueing;

import java.util.concurrent.BlockingQueue;

/**
 * Job queue implementation using a Java thread-safe blockingQueue queue
 */
public class BlockingJobQueue implements JobQueue {
    private final BlockingQueue<Job> blockingQueue;

    /**
     * Constructor
     * @param blockingQueue The blocking queue implementation. Note that ArrayBlockingQueue has a fixed capacity.
     *              It might not be enough to store an unknown number of crawling jobs.
     */
    public BlockingJobQueue(BlockingQueue<Job> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public Job poll() {
        return blockingQueue.poll();
    }

    @Override
    public boolean add(Job job) {
        return blockingQueue.add(job);
    }

    @Override
    public void clear() {
        blockingQueue.clear();
    }

    @Override
    public boolean isEmpty() {
        return !blockingQueue.isEmpty();
    }
}
