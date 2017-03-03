package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.queueing.Job;

import java.net.URI;

public class FollowSubDomains implements LinkFollower {
    @Override
    public boolean shouldFollow(URI nextUri, Job currentJob) {
        URI currentUri = currentJob.getUri();
        return nextUri.getHost().equals(currentUri.getHost()) || nextUri.getHost().endsWith(currentUri.getHost());
    }
}
