package com.ndthuan.nucrawler.crawler.requestfactory;

import com.ndthuan.nucrawler.crawler.Specification;
import com.ndthuan.nucrawler.queueing.Job;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

public class ForcedGet implements RequestFactory {
    @Override
    public HttpUriRequest createRequest(Job job, Specification specification) {
        return new HttpGet(job.getUri());
    }
}
