package com.ndthuan.nucrawler.helpers;

import org.junit.Test;

import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class UriUtilsTest {
    @Test
    public void simplifyShouldRemoveFragment() throws Exception {
        URI uri = new URI("http://example.org/#something");

        URI simplified = UriUtils.simplify(uri);

        assertEquals("http://example.org/", simplified.toString());
    }

    @Test
    public void safelyResolve() throws Exception {
        URI uri = new URI("http://example.org/#something");

        URI resolved = UriUtils.safelyResolve(uri, "tư\"-cẩm-ly/xyz/");

        assertEquals("http://example.org/tư%22-cẩm-ly/xyz/", resolved.toString());
    }

    @Test
    public void simplifyShouldRemoveEmptyQuery() throws Exception {
        URI uri = new URI("http://example.org/?");

        URI simplified = UriUtils.simplify(uri);

        assertEquals("http://example.org/", simplified.toString());
    }

    @Test
    public void simplifyShouldKeepNonEmptyQuery() throws Exception {
        URI uri = new URI("http://example.org/?x=y");

        URI simplified = UriUtils.simplify(uri);

        assertEquals("http://example.org/?x=y", simplified.toString());
    }

    @Test
    public void simplifyShouldRemoveFragmentAndEmptyQuery() throws Exception {
        URI uri = new URI("http://example.org/?#something");

        URI simplified = UriUtils.simplify(uri);

        assertEquals("http://example.org/", simplified.toString());
    }

    @Test
    public void simplifyShouldRemoveFragmentAndKeepNonEmptyQuery() throws Exception {
        URI uri = new URI("http://example.org/?x=y#something");

        URI simplified = UriUtils.simplify(uri);

        assertEquals("http://example.org/?x=y", simplified.toString());
    }
}