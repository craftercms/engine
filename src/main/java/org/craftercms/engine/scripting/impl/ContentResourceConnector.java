package org.craftercms.engine.scripting.impl;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.util.url.ContentUrlStreamHandler;
import org.springframework.beans.factory.annotation.Required;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Implementation of {@link groovy.util.ResourceConnector} for retrieving {@link java.net.URLConnection} to
 * content in a Crafter content store.
 *
 * @author Alfonso Vásquez
 */
public class ContentResourceConnector implements ResourceConnector {

    protected URLStreamHandler urlStreamHandler;

    @Required
    public void setStoreService(ContentStoreService storeService) {
        urlStreamHandler = new ContentUrlStreamHandler(storeService);
    }

    @Override
    public URLConnection getResourceConnection(String name) throws ResourceException {
        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        String spec = ContentUrlStreamHandler.PROTOCOL + ":" + name;

        try {
            return new URL(null, spec, urlStreamHandler).openConnection();
        } catch (Exception e) {
            throw new ResourceException("Unable to open URL connection to '" + spec + "'", e);
        }
    }

}
