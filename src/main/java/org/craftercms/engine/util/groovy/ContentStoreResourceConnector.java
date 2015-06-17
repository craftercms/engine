package org.craftercms.engine.util.groovy;

import java.net.URLConnection;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.url.ContentStoreUrlStreamHandler;

/**
 * Implementation of {@link groovy.util.ResourceConnector} for retrieving {@link java.net.URLConnection} to
 * content in a Crafter content store.
 *
 * @author Alfonso Vásquez
 */
public class ContentStoreResourceConnector implements ResourceConnector {

    protected ContentStoreUrlStreamHandler urlStreamHandler;

    public ContentStoreResourceConnector(SiteContext siteContext) {
        urlStreamHandler = new ContentStoreUrlStreamHandler(siteContext);
    }

    @Override
    public URLConnection getResourceConnection(String name) throws ResourceException {
        try {
            return urlStreamHandler.createUrl(name).openConnection();
        } catch (Exception e) {
            throw new ResourceException("Unable to open URL connection to '" + name + "'", e);
        }
    }

}
