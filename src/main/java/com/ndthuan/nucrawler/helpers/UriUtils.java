package com.ndthuan.nucrawler.helpers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UriUtils {
    /**
     * Removes fragment and empty query from a URI
     *
     * @param uri The URI
     *
     * @return The simplified URI
     */
    public static URI simplify(URI uri) {
        try {
            String query = uri.getQuery();
            if (query != null && query.isEmpty()) {
                query = null;
            }

            return new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                query,
                null
            );
        } catch (URISyntaxException ignored) {
        }

        return uri;
    }

    // TODO need a better name for this method?
    public static URI safelyResolve(URI baseUri, String relativeUri) throws IrresolvableUriException {
        try {
            // TODO Is using a URL to resolve a URI something weird?
            // URI.resolve(str) doesn't accept special characters like spaces, quotes... in str.
            // URL can also resolve relative URLs that contain special characters in path but it doesn't encode them
            URL absUrl = new URL(baseUri.toURL(), relativeUri);

            // creating new Uri using resolved URL info so that it correctly encodes the path for us
            return new URI(
                absUrl.getProtocol(),
                absUrl.getUserInfo(),
                absUrl.getHost(),
                absUrl.getPort(),
                absUrl.getPath(), // special characters here will be correctly encoded, path separators left intact
                absUrl.getQuery(),
                null
            );
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IrresolvableUriException(
                String.format("Unable to resolve the given relative URI: %s", relativeUri),
                e
            );
        }
    }
}
