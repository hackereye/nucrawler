# nucrawler
Java web crawler (primarily designed as a scraping tool)

# Example usage (to be updated)
```java
Specification specification = Specification.Builder.create().build();
JobQueue jobQueue = new BlockingJobQueue(new LinkedBlockingQueue<>());
JobHistory jobHistory = new InterProcessJobHistory();
NuCrawler crawler = new NuCrawler(specification, jobQueue, jobHistory);
crawler.crawl("http://example.org", "https://wikipedia.org");
```

# To do
- Beautify & clean up code
- Write detailed usage documentation
- Create redis queue component
- Create persistent storage queue component
- Respect robots.txt and 'robots' meta instructions