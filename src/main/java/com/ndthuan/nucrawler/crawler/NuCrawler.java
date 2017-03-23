package com.ndthuan.nucrawler.crawler;

import com.ndthuan.nucrawler.api.JobHistory;
import com.ndthuan.nucrawler.crawler.behaviors.JobBehavior;
import com.ndthuan.nucrawler.crawler.linkfollowers.LinkFollower;
import com.ndthuan.nucrawler.crawler.requestdecorators.RequestDecorator;
import com.ndthuan.nucrawler.crawler.responsehandlers.ResponseHandler;
import com.ndthuan.nucrawler.helpers.IrresolvableUriException;
import com.ndthuan.nucrawler.helpers.UriUtils;
import com.ndthuan.nucrawler.api.Job;
import com.ndthuan.nucrawler.queueing.JobMaker;
import com.ndthuan.nucrawler.api.JobQueue;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NuCrawler {
    final static String VERSION = "0.1";
    private final static Logger LOG = LoggerFactory.getLogger(NuCrawler.class);
    private final static AtomicInteger INSTANCES = new AtomicInteger(0);

    private final Specification specification;
    private final JobQueue jobQueue;
    private final CloseableHttpClient httpClient;
    private final JobHistory jobHistory;
    private final SimpleTaskManager taskManager = new SimpleTaskManager(
        this,
        String.format("nucrawler%s", INSTANCES.incrementAndGet())
    );

    public NuCrawler(Specification specification, JobQueue jobQueue, JobHistory jobHistory) {
        this.specification = specification;
        this.jobQueue = jobQueue;
        this.jobHistory = jobHistory;

        httpClient = specification.getHttpClientBuilder()
            // we want to control redirection manually
            .setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                    return false;
                }
            })
            .build();
    }

    public Specification getSpecification() {
        return specification;
    }

    public JobQueue getJobQueue() {
        return jobQueue;
    }

    public JobHistory getJobHistory() {
        return jobHistory;
    }

    /**
     * Fetches all seeding URI's in the current thread then launches workers to process the queue.
     *
     * @param seedUris Seeding URI's
     */
    public void crawl(String... seedUris) {
        for (String seedUri : seedUris) {
            try {
                visitSeedPage(seedUri);
            } catch (URISyntaxException e) {
                if (LOG.isDebugEnabled()) LOG.debug("URISyntaxException: {}", seedUri, e);
            }
        }

        resume();
    }

    /**
     * Processes all queued jobs in threads. Note that if inter-process queue is used, there will be nothing to resume.
     */
    public synchronized void resumeAndWait() {
        taskManager.resume();
        taskManager.await();
    }

    /**
     * Processes all queued jobs in threads. Wait for them to finish then shutdown.
     */
    public synchronized void resume() {
        resumeAndWait();
        shutdown();
    }

    private void shutdown() {
        taskManager.stop();
    }

    /**
     * Fetches a single page, applies all response handlers as in crawling mode.
     *
     * @param uri The URI to visitSeedPage
     * @throws URISyntaxException if the URI syntax is invalid
     */
    public void visitSeedPage(String uri) throws URISyntaxException {
        execute(JobMaker.makeSeedingJob(new URI(uri)));
    }

    /**
     * Downloads an HTTP resource and saves response body in a designated location.
     *
     * @param uri The URI to download
     * @param referrer The referral URI
     * @param saveTo The path to save to
     *
     * @return successfully or not
     */
    public boolean download(URI uri, URI referrer, Path saveTo) {
        try (CloseableHttpResponse response = makeRequest(new Job(uri, 0, referrer))) {
            if (response.getStatusLine().getStatusCode() == 200) {
                Files.copy(response.getEntity().getContent(), saveTo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Executes a crawling job. All of pre-execution behaviors, status handling,
     * post-execution handlers... are done here.
     *
     * @param job The job to execute
     */
    private void execute(Job job) {
        if (!canExecute(job)) {
            return;
        }

        URI jobUri = job.getUri();
        try (CloseableHttpResponse response = makeRequest(job)) {
            int status = response.getStatusLine().getStatusCode();

            if (status >= 500) {
                if (LOG.isDebugEnabled()) LOG.debug("Requeueing {}: {}", jobUri, status);
                jobQueue.add(job);
            } else if (status >= 400) {
                if (LOG.isDebugEnabled()) LOG.debug("Ignoring {}: {}", jobUri, status);
                jobHistory.add(jobUri);
            } else if (status >= 300) {
                if (job.getDepth() < specification.getMaxDepth()) {
                    followRedirection(response, job);
                }
                jobHistory.add(jobUri);
            } else {
                handleSuccessfulRequest(response, job);
                jobHistory.add(jobUri);
            }
        } catch (IOException e) {
            LOG.warn("Error visiting {}", jobUri, e);
        }
    }

    private URI resolve(String relativeUri, URI baseUri) throws IrresolvableUriException {
        return UriUtils.simplify(
            UriUtils.safelyResolve(baseUri, relativeUri)
        );
    }

    private boolean canExecute(Job job) {
        URI jobUri = job.getUri();

        if (!isAcceptedScheme(jobUri.getScheme())) {
            if (LOG.isDebugEnabled()) LOG.debug("Disallowed URI scheme: {}", jobUri);
            return false;
        }

        if (job.getDepth() > specification.getMaxDepth()) {
            if (LOG.isDebugEnabled()) LOG.debug("Max depth exceeded: {}", jobUri);
            return false;
        }

        for (JobBehavior behavior : specification.getJobBehaviors()) {
            if (!behavior.shouldExecute(job, this)) {
                if (LOG.isDebugEnabled()) LOG.debug("{} blocked crawling of {}", behavior.getClass().getSimpleName(), jobUri);
                return false;
            }
        }

        return true;
    }

    private boolean canFollow(URI nextUri, Job job) {
        if (!isAcceptedScheme(nextUri.getScheme())) {
            return false;
        }

        if (job.getUri().equals(nextUri)) {
            return false;
        }

        for (LinkFollower linkFollower : specification.getLinkFollowers()) {
            if (!linkFollower.shouldFollow(nextUri, job)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAcceptedScheme(String scheme) {
        return specification.getAcceptedSchemes().contains(scheme);
    }

    private CloseableHttpResponse makeRequest(Job job) throws IOException {
        LOG.info("Visiting {} (depth: {})", job.getUri(), job.getDepth());

        HttpUriRequest request = specification.getRequestFactory().createRequest(job, specification);
        request = decorateRequest(request);

        if (job.getReferrer() != null) {
            request.addHeader("Referer", job.getReferrer().toString());
        }

        if (specification.getUserAgent() != null) {
            request.addHeader("User-Agent", specification.getUserAgent());
        }

        return httpClient.execute(request);
    }

    private HttpUriRequest decorateRequest(HttpUriRequest request) {
        for (RequestDecorator decorator : specification.getRequestDecorators()) {
            request = decorator.decorate(request);
        }

        return request;
    }

    /**
     * Invokes response handlers on a successful request
     *
     * @param response The response
     * @param job The job
     */
    private void handleSuccessfulRequest(CloseableHttpResponse response, Job job) {
        HttpEntity entity = response.getEntity();
        if (entity.getContentType().getValue().toLowerCase().startsWith("text/html")) {
            Document document;
            try {
                Header encoding = entity.getContentEncoding();
                String contentEncoding = (encoding != null ? encoding.getValue() : "UTF-8");

                document = Jsoup.parse(entity.getContent(), contentEncoding, job.getUri().toString());
            } catch (IOException e) {
                LOG.warn("Error parsing HTML at {}", job.getUri(), e);
                return;
            }

            executeHandlers(new HtmlCrawlingResult(this, job, response, document));

            if (job.getDepth() < specification.getMaxDepth()) {
                followLinks(document, job);
            }
        } else {
            executeHandlers(new CrawlingResult(this, job, response));
        }
    }

    private void followRedirection(HttpResponse response, Job job) {
        URI jobUri = job.getUri();

        for (Header locationHeader : response.getHeaders("Location")) {
            URI nextUri;
            try {
                nextUri = resolve(locationHeader.getValue().trim(), jobUri);
            } catch (IrresolvableUriException e) {
                return;
            }

            if (!canFollow(nextUri, job)) {
                return;
            }

            if (LOG.isDebugEnabled()) LOG.debug("{} redirecting to {}", jobUri, nextUri);
            jobQueue.add(JobMaker.makeRegularJob(nextUri, job));
        }
    }

    /**
     * Finds and put all linked URIs in queue.
     *
     * @param document Parsed HTML document
     * @param job The job
     */
    private void followLinks(Document document, Job job) {
        URI jobUri = job.getUri();
        Set<URI> foundUrisSet = new HashSet<>();

        for (Element link : document.select("a[href]")) {
            String href = link.attr("href").trim();

            URI nextUri;
            try {
                nextUri = resolve(href, jobUri);
            } catch (IrresolvableUriException e) {
                continue;
            }

            foundUrisSet.add(nextUri);
        }

        for (URI nextUri : foundUrisSet) {
            if (!canFollow(nextUri, job)) continue;
            jobQueue.add(JobMaker.makeRegularJob(nextUri, job));
        }
    }

    private void executeHandlers(CrawlingResult crawlingResult) {
        for (ResponseHandler responseHandler : specification.getResponseHandlers()) {
            try {
                responseHandler.handle(crawlingResult);
            } catch (Exception e){
                LOG.info(
                    "Error invoking handler {} for {}",
                    responseHandler.getClass(),
                    crawlingResult.getJob().getUri(),
                    e
                );
            }
        }
    }

    private static class CrawlingTask implements Runnable {
        private final NuCrawler crawler;

        private CrawlingTask(NuCrawler crawler) {
            this.crawler = crawler;
        }

        @Override
        public void run() {
            int idleCounter = 0;

            while (true) {
                Job job = crawler.getJobQueue().poll();

                if (job == null) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ignored) {
                    }

                    // we should break if there is still no new job after 5 seconds
                    // TODO make this configurable?
                    if (++idleCounter >= 5) break;

                    continue;
                } else {
                    // reset counter if a job is available
                    idleCounter = 0;
                }

                crawler.execute(job);
            }
        }
    }

    private static class SimpleTaskManager {
        private final Map<String, Thread> threads = new ConcurrentHashMap<>();
        private final NuCrawler crawler;
        private final String threadPrefix;

        private SimpleTaskManager(NuCrawler crawler, String threadPrefix) {
            this.crawler = crawler;
            this.threadPrefix = threadPrefix;
        }

        private void resume() {
            for (int workerCount = 1; workerCount <= crawler.getSpecification().getMaxWorkers(); workerCount++) {
                String threadName = String.format("%s-%s", threadPrefix, workerCount);
                if (
                    !threads.containsKey(threadName)
                    || !threads.get(threadName).isAlive()
                    || threads.get(threadName).isInterrupted()
                ) {
                    Thread thread = new Thread(new CrawlingTask(crawler), threadName);
                    thread.start();
                    threads.put(threadName, thread);
                }
            }
        }

        private void await() {
            for (Thread thread : threads.values()) {
                try {
                    thread.join();
                } catch (InterruptedException ignored) {
                }
            }
        }

        private synchronized void stop() {
            for (Thread thread : threads.values()) {
                thread.interrupt();
            }
        }
    }
}
