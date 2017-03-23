package com.ndthuan.nucrawler.crawler;

import com.ndthuan.nucrawler.api.Job;
import org.apache.http.HttpResponse;

public class CrawlingResult {
    private final NuCrawler crawler;

    private final Job job;

    private final HttpResponse httpResponse;

    CrawlingResult(NuCrawler crawler, Job job, HttpResponse httpResponse) {
        this.crawler = crawler;
        this.job = job;
        this.httpResponse = httpResponse;
    }

    public NuCrawler getCrawler() {
        return crawler;
    }

    public Job getJob() {
        return job;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }
}
