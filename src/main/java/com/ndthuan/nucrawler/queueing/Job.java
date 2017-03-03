package com.ndthuan.nucrawler.queueing;

import java.net.URI;

public class Job {
    private final URI uri;

    private final int depth;

    private final URI referrer;

    public Job(URI uri, int depth, URI referrer) {
        this.uri = uri;
        this.depth = depth;
        this.referrer = referrer;
    }

    public URI getUri() {
        return uri;
    }

    public int getDepth() {
        return depth;
    }

    public URI getReferrer() {
        return referrer;
    }
}
