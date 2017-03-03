package com.ndthuan.nucrawler.crawler.responsehandlers;

import com.ndthuan.nucrawler.crawler.CrawlingResult;

public interface ResponseHandler {
    void handle(CrawlingResult crawlingResult);
}
