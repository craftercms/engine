package org.craftercms.engine.store.s3;

import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.StoreException;
import org.craftercms.core.service.Content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class S3Content implements Content {

    /**
     * When the file was last modified.
     */
    protected long lastModified;

    /**
     * The length of the file.
     */
    protected long length;

    /**
     * The content of the file.
     */
    protected byte[] content;

    public S3Content(final S3Object s3Object) {
        lastModified = s3Object.getObjectMetadata().getLastModified().getTime();
        length = s3Object.getObjectMetadata().getContentLength();
        content = new byte[(int)length];

        try(InputStream is = s3Object.getObjectContent()) {
            IOUtils.readFully(is, content);
        } catch (Exception e) {
            throw new StoreException("Error reading S3 item " + s3Object, e);
        }
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

}
