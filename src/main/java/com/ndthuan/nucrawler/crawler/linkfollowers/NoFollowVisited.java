package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.api.Job;
import com.ndthuan.nucrawler.api.JobHistory;

import java.net.URI;

public class NoFollowVisited implements LinkFollower {
    private final JobHistory jobHistory;

    public NoFollowVisited(JobHistory jobHistory) {
        this.jobHistory = jobHistory;
    }

    @Override
    public boolean shouldFollow(URI nextUri, Job currentJob) {
        return !jobHistory.hasCrawled(nextUri);
    }
}
