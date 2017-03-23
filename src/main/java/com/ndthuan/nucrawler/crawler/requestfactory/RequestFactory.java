package com.ndthuan.nucrawler.crawler.requestfactory;

import com.ndthuan.nucrawler.crawler.Specification;
import com.ndthuan.nucrawler.api.Job;
import org.apache.http.client.methods.HttpUriRequest;

public interface RequestFactory {
    HttpUriRequest createRequest(Job job, Specification specification);
}
