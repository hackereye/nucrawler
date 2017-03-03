package com.ndthuan.nucrawler.crawler;

import com.ndthuan.nucrawler.crawler.responsehandlers.ResponseHandler;
import com.ndthuan.nucrawler.history.InterProcessJobHistory;
import com.ndthuan.nucrawler.history.JobHistory;
import com.ndthuan.nucrawler.queueing.BlockingJobQueue;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO replace real URLs with mocks
 */
public class NuCrawlerTest {
    private NuCrawler crawler;
    private JobHistory jobHistory = new InterProcessJobHistory();
    private final Set<String> foundTitles = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        Specification specification = Specification.Builder.create()
            .setMaxDepth(1)
            .setUserAgent(String.format("NuCrawler/%s", NuCrawler.VERSION))
            .addSuccessResponseHandler(new ResponseHandler() {
                @Override
                public synchronized void handle(CrawlingResult crawlingResult) {
                    if (crawlingResult instanceof HtmlCrawlingResult) {
                        foundTitles.add(((HtmlCrawlingResult) crawlingResult).getDocument().select("title").text());
                    }
                }
            })
            .build();

        jobHistory.clear();

        crawler = new NuCrawler(
            specification,
            new BlockingJobQueue(new LinkedBlockingQueue<>()),
            jobHistory
        );
    }

    @Test
    public void crawlShouldFollowRedirect() throws Exception {
        crawler.crawl("https://en.wikipedia.org/");

        assertTrue("Should have visited main page", jobHistory.hasCrawled(new URI("https://en.wikipedia.org/wiki/Main_Page")));
        assertTrue("Should have visited en. site", jobHistory.hasCrawled(new URI("https://en.wikipedia.org/")));
        assertTrue("Should have seen Wikipedia title", foundTitles.contains("Wikipedia, the free encyclopedia"));
    }

    @Test
    public void crawlShouldFollowLinks() throws Exception {
        crawler.crawl("http://example.org/");

        assertTrue("Should have visited linked page", jobHistory.hasCrawled(new URI("http://www.iana.org/domains/example")));
    }

    @Test
    public void shouldNotCrawlNonHttp() throws Exception {
        crawler.crawl("ftp://example.org");

        assertFalse("Should not crawl non-http", jobHistory.hasCrawled(new URI("ftp://example.org")));
    }
}