package com.ndthuan.nucrawler.crawler.requestdecorators;

import org.apache.http.client.methods.HttpUriRequest;

public interface RequestDecorator {
    HttpUriRequest decorate(HttpUriRequest request);
}
