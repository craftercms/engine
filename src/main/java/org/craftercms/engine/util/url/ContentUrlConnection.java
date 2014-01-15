package org.craftercms.engine.util.url;

import org.craftercms.core.service.Content;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Implementation of {@link java.net.URLConnection} that wraps a {@link org.craftercms.core.service.Content}.
 *
 * @author Alfonso Vásquez
 */
public class ContentUrlConnection extends URLConnection {

    protected Content content;

    public ContentUrlConnection(URL url, Content content) {
        super(url);

        this.content = content;
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return content.getInputStream();
    }

}
