package com.ndthuan.nucrawler.queueing;

import java.net.URI;

public class JobMaker {
    public static Job makeSeedingJob(URI uri) {
        return new Job(uri, 0, null);
    }

    public static Job makeSeedingJob(URI uri, URI referrer) {
        return new Job(uri, 0, referrer);
    }

    public static Job makeRegularJob(URI uri, Job currentJob) {
        return new Job(uri, currentJob.getDepth() + 1, currentJob.getUri());
    }
}
