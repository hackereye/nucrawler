package com.ndthuan.nucrawler.crawler;

import com.ndthuan.nucrawler.crawler.behaviors.CrawlOnce;
import com.ndthuan.nucrawler.crawler.behaviors.JobBehavior;
import com.ndthuan.nucrawler.crawler.linkfollowers.LinkFollower;
import com.ndthuan.nucrawler.crawler.requestdecorators.RequestDecorator;
import com.ndthuan.nucrawler.crawler.requestfactory.ForcedGet;
import com.ndthuan.nucrawler.crawler.requestfactory.RequestFactory;
import com.ndthuan.nucrawler.crawler.responsehandlers.ResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Crawler configuration
 */
public class Specification {
    private final List<ResponseHandler> responseHandlers = new CopyOnWriteArrayList<>();
    private final List<JobBehavior> jobBehaviors = new CopyOnWriteArrayList<>();
    private final List<LinkFollower> linkFollowers = new CopyOnWriteArrayList<>();
    private final List<RequestDecorator> requestDecorators = new CopyOnWriteArrayList<>();
    private final Set<String> acceptedSchemes;
    private final RequestFactory requestFactory;
    private final String userAgent;
    private final int maxWorkers;
    private final int maxDepth;
    private final HttpClientBuilder httpClientBuilder;

    private Specification(Builder builder) {
        this.userAgent = builder.userAgent;
        this.maxWorkers = builder.maxWorkers;
        this.maxDepth = builder.maxDepth;
        this.requestFactory = builder.requestFactory;
        this.httpClientBuilder = builder.httpClientBuilder;
        this.acceptedSchemes = builder.acceptedSchemes;

        this.requestDecorators.addAll(builder.requestDecorators);
        this.responseHandlers.addAll(builder.responseHandlers);
        this.jobBehaviors.addAll(builder.jobBehaviors);
        this.linkFollowers.addAll(builder.linkFollowers);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public List<ResponseHandler> getResponseHandlers() {
        return responseHandlers;
    }

    public List<JobBehavior> getJobBehaviors() {
        return jobBehaviors;
    }

    public List<LinkFollower> getLinkFollowers() {
        return linkFollowers;
    }

    public List<RequestDecorator> getRequestDecorators() {
        return requestDecorators;
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public int getMaxWorkers() {
        return maxWorkers;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public HttpClientBuilder getHttpClientBuilder() {
        return httpClientBuilder;
    }

    public Set<String> getAcceptedSchemes() {
        return acceptedSchemes;
    }

    public static class Builder {
        private Set<ResponseHandler> responseHandlers = new HashSet<>();
        private Set<JobBehavior> jobBehaviors = new HashSet<>();
        private Set<LinkFollower> linkFollowers = new HashSet<>();
        private Set<RequestDecorator> requestDecorators = new HashSet<>();
        private Set<String> acceptedSchemes = new HashSet<>();
        private RequestFactory requestFactory = new ForcedGet();
        private HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        private String userAgent = String.format("NuCrawler/%s", NuCrawler.VERSION);
        private int maxWorkers = Runtime.getRuntime().availableProcessors();
        private int maxDepth = 0;

        public Builder() {
            acceptedSchemes.add("http");
            acceptedSchemes.add("https");
        }

        /**
         * Creates a builder with some reasonable defaults for a crawler
         *
         * @return The builder
         */
        public static Builder create() {
            Builder builder = new Builder();
            builder.jobBehaviors.add(new CrawlOnce());

            return builder;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder addSuccessResponseHandler(ResponseHandler responseHandler) {
            responseHandlers.add(responseHandler);
            return this;
        }

        public Builder addBeforeRequestBehavior(JobBehavior jobBehavior) {
            jobBehaviors.add(jobBehavior);
            return this;
        }

        public Builder addRequestDecorator(RequestDecorator requestDecorator) {
            requestDecorators.add(requestDecorator);
            return this;
        }

        public Builder addAcceptedScheme(String scheme) {
            acceptedSchemes.add(scheme);
            return this;
        }

        public Builder removeAcceptedScheme(String scheme) {
            acceptedSchemes.remove(scheme);
            return this;
        }

        public Builder addLinkFollower(LinkFollower follower) {
            linkFollowers.add(follower);
            return this;
        }

        public Builder setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder setRequestFactory(RequestFactory requestFactory) {
            this.requestFactory = requestFactory;
            return this;
        }

        public void setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
        }

        public Specification build() {
            return new Specification(this);
        }
    }
}
