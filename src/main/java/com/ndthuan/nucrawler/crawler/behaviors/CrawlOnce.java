package com.ndthuan.nucrawler.crawler.behaviors;

import com.ndthuan.nucrawler.crawler.NuCrawler;
import com.ndthuan.nucrawler.api.Job;

public class CrawlOnce implements JobBehavior {
    @Override
    public boolean shouldExecute(Job job, NuCrawler crawler) {
        return job.getDepth() < 1 || !crawler.getJobHistory().hasCrawled(job.getUri());
    }
}
