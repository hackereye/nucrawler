package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.api.Job;

import java.net.URI;

public class FollowSameHostOnly implements LinkFollower {
    private final boolean acceptWwwDiff;

    public FollowSameHostOnly(boolean acceptWwwDiff) {
        this.acceptWwwDiff = acceptWwwDiff;
    }

    public FollowSameHostOnly() {
        this(true);
    }

    @Override
    public boolean shouldFollow(URI nextUri, Job currentJob) {
        String currentHost = currentJob.getUri().getHost();
        String nextHost = nextUri.getHost();

        if (acceptWwwDiff) {
            currentHost = currentHost.replaceFirst("^www\\.", "");
            nextHost = nextHost.replaceFirst("www\\.", "");
        }

        return currentHost.equals(nextHost);
    }
}
