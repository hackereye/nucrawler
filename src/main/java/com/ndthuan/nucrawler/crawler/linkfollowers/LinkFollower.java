package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.queueing.Job;

import java.net.URI;

public interface LinkFollower {
    boolean shouldFollow(URI nextUri, Job currentJob);
}
