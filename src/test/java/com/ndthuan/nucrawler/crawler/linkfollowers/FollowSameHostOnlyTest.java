package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.queueing.Job;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class FollowSameHostOnlyTest {
    @Test
    public void shouldFollowStrictly() throws Exception {
        LinkFollower follower = new FollowSameHostOnly(false);

        Job job = new Job(
            new URI("http://www.example.org"),
            0,
            null
        );

        assertFalse(follower.shouldFollow(new URI("http://example.org"), job));
        assertTrue(follower.shouldFollow(new URI("http://www.example.org"), job));
        assertFalse(follower.shouldFollow(new URI("http://random.example.org"), job));
    }

    @Test
    public void shouldFollowFlexibly() throws Exception {
        LinkFollower follower = new FollowSameHostOnly();

        Job job = new Job(
            new URI("http://www.example.org"),
            0,
            null
        );

        assertTrue(follower.shouldFollow(new URI("http://example.org"), job));
        assertTrue(follower.shouldFollow(new URI("http://www.example.org"), job));
        assertFalse(follower.shouldFollow(new URI("http://random.example.org"), job));
    }
}