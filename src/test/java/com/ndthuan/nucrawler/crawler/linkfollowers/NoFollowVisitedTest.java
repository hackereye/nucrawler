package com.ndthuan.nucrawler.crawler.linkfollowers;

import com.ndthuan.nucrawler.history.InterProcessJobHistory;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NoFollowVisitedTest {
    @Test
    public void shouldFollowIfNeverVisited() throws Exception {
        InterProcessJobHistory jobHistory = new InterProcessJobHistory();
        LinkFollower follower = new NoFollowVisited(jobHistory);

        boolean actual = follower.shouldFollow(new URI("http://example.org/"), null);

        assertTrue(actual);
    }

    @Test
    public void shouldNotFollowIfNeverVisited() throws Exception {
        InterProcessJobHistory jobHistory = new InterProcessJobHistory();
        jobHistory.add(new URI("http://example.org/"));
        LinkFollower follower = new NoFollowVisited(jobHistory);

        boolean actual = follower.shouldFollow(new URI("http://example.org/"), null);

        assertFalse(actual);
    }
}