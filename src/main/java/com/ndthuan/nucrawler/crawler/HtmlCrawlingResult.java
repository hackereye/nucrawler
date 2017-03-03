package com.ndthuan.nucrawler.crawler;

import com.ndthuan.nucrawler.queueing.Job;
import org.apache.http.HttpResponse;
import org.jsoup.nodes.Document;

public class HtmlCrawlingResult extends CrawlingResult {
    private Document document;

    HtmlCrawlingResult(NuCrawler crawler, Job job, HttpResponse httpResponse, Document document) {
        super(crawler, job, httpResponse);
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
