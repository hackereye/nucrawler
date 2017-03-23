package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.api.Job;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class FollowSubDomainsTest {
    @Test
    public void shouldFollow() throws Exception {
        LinkFollower follower = new FollowSubDomains();

        Job job = new Job(
            new URI("http://example.org/abc"),
            0,
            null
        );

        assertTrue("Should follow same host", follower.shouldFollow(new URI("http://example.org/"), job));
        assertTrue("Should follow sub domains", follower.shouldFollow(new URI("http://en.example.org/"), job));
    }
}