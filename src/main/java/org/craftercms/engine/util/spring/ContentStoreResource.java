package org.craftercms.engine.util.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.craftercms.core.service.Content;
import org.craftercms.core.store.impl.filesystem.FileSystemFile;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.url.ContentStoreUrlStreamHandler;
import org.springframework.core.io.AbstractResource;

/**
 * A {@link org.springframework.core.io.Resource} for a Crafter content store {@link Content}.
 *
 * @author avasquez
 */
public class ContentStoreResource extends AbstractResource {

    protected SiteContext context;
    protected String url;

    public ContentStoreResource(SiteContext context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    public boolean exists() {
        return context.getStoreService().exists(context.getContext(), url);
    }

    @Override
    public URL getURL() throws IOException {
        ContentStoreUrlStreamHandler urlStreamHandler = new ContentStoreUrlStreamHandler(context);

        return urlStreamHandler.createUrl(url);
    }

    @Override
    public long contentLength() throws IOException {
        Content content = getContent();
        if (content != null) {
            return content.getLength();
        } else {
            return 0;
        }
    }

    @Override
    public long lastModified() throws IOException {
        Content content = getContent();
        if (content != null) {
            return content.getLength();
        } else {
            throw new FileNotFoundException(getDescription() + " not found");
        }
    }

    @Override
    public File getFile() throws IOException {
        Content content = getContent();
        if (content != null) {
            if (content instanceof FileSystemFile) {
                return ((FileSystemFile) content).getFile();
            } else {
                throw new FileNotFoundException(getDescription() + " doesn't correspond to a file in the filesystem");
            }
        } else {
            throw new FileNotFoundException(getDescription() + " not found");
        }
    }

    @Override
    public String getFilename() {
        return FilenameUtils.getName(url);
    }

    @Override
    public String getDescription() {
        return context.getSiteName() + ":" + url;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Content content = getContent();
        if (content != null) {
            return content.getInputStream();
        } else {
            throw new FileNotFoundException(getDescription() + " not found");
        }
    }

    public Content getContent() {
        return context.getStoreService().findContent(context.getContext(), url);
    }

}
