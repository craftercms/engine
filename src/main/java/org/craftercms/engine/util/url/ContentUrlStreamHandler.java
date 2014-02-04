package org.craftercms.engine.util.url;

import groovy.util.ResourceException;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * {@link java.net.URLStreamHandler} for handling {@link java.net.URLConnection}s to the Crafter content store.
 */
public class ContentUrlStreamHandler extends URLStreamHandler {

    public static final String PROTOCOL = "content-store";

    protected ContentStoreService storeService;

    public ContentUrlStreamHandler(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        if (!url.getProtocol().equals(PROTOCOL)) {
            throw new MalformedURLException("Unrecognized protocol: " + url.getProtocol());
        }

        Context context = AbstractSiteContextResolvingFilter.getCurrentContext().getContext();

        try {
            Content content = storeService.getContent(context, url.getFile());

            return new ContentUrlConnection(url, content);
        } catch (PathNotFoundException e) {
            throw new FileNotFoundException("No script found at '" + url.getFile() + "' in content store");
        } catch (Exception e) {
            throw new IOException("Error retrieving script at '" + url.getFile() + "' in content store", e);
        }
    }

}
