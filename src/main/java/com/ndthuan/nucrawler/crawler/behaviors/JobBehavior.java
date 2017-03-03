package com.ndthuan.nucrawler.crawler.behaviors;

import com.ndthuan.nucrawler.crawler.NuCrawler;
import com.ndthuan.nucrawler.queueing.Job;

public interface JobBehavior {
    boolean shouldExecute(Job job, NuCrawler crawler);
}
