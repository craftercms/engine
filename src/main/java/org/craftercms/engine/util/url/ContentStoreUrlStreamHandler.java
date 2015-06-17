package org.craftercms.engine.util.url;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Content;
import org.craftercms.engine.service.context.SiteContext;

/**
 * {@link java.net.URLStreamHandler} for handling {@link java.net.URLConnection}s to the Crafter content store.
 *
 * @author avasquez
 */
public class ContentStoreUrlStreamHandler extends URLStreamHandler {

    private static final String URL_REGEX = "[^:]+:.+";

    protected SiteContext siteContext;

    public ContentStoreUrlStreamHandler(SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    public URL createUrl(String filename) throws MalformedURLException {
        if (!filename.matches(URL_REGEX)) {
            filename = siteContext.getSiteName() + ':' + (!filename.startsWith("/")? "/" : "") + filename;
        }

        return new URL(null, filename, this);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        try {
            Content content = siteContext.getStoreService().getContent(siteContext.getContext(), url.getFile());

            return new ContentStoreUrlConnection(url, content);
        } catch (PathNotFoundException e) {
            throw new FileNotFoundException("No script found at '" + url.getFile() + "' in content store");
        } catch (Exception e) {
            throw new IOException("Error retrieving script at '" + url.getFile() + "' in content store", e);
        }
    }

}
