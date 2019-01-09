/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.util.spring.resources;

import org.apache.commons.io.IOUtils;
import org.craftercms.commons.spring.resources.RangeAwareResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Extension of {@link ResourceRegionHttpMessageConverter} that checks if the resource implement
 * {@link RangeAwareResource}. If it does, it calls {@link RangeAwareResource#getInputStream(long, long)} to get
 * the region(s) to write.
 *
 * @author avasquez
 */
public class RangeAwareResourceRegionHttpMessageConverter extends ResourceRegionHttpMessageConverter {

    @Override
    @SuppressWarnings("unchecked")
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        if (object instanceof ResourceRegion) {
            writeResourceRegion((ResourceRegion) object, outputMessage);
        } else {
            Collection<ResourceRegion> regions = (Collection<ResourceRegion>) object;
            if (regions.size() == 1) {
                writeResourceRegion(regions.iterator().next(), outputMessage);
            } else {
                writeResourceRegionCollection((Collection<ResourceRegion>) object, outputMessage);
            }
        }
    }


    protected void writeResourceRegion(ResourceRegion region, HttpOutputMessage outputMessage) throws IOException {
        Assert.notNull(region, "ResourceRegion must not be null");
        HttpHeaders responseHeaders = outputMessage.getHeaders();

        long start = region.getPosition();
        long end = start + region.getCount() - 1;
        Long resourceLength = region.getResource().contentLength();
        end = Math.min(end, resourceLength - 1);
        long rangeLength = end - start + 1;
        responseHeaders.add("Content-Range", "bytes " + start + '-' + end + '/' + resourceLength);
        responseHeaders.setContentLength(rangeLength);

        InputStream in = null;
        try {
            Resource resource = region.getResource();
            if (resource instanceof RangeAwareResource) {
                in = ((RangeAwareResource) resource).getInputStream(start, end);
                StreamUtils.copy(in, outputMessage.getBody());
            } else {
                in = resource.getInputStream();
                StreamUtils.copyRange(in, outputMessage.getBody(), start, end);
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    protected void writeResourceRegionCollection(Collection<ResourceRegion> resourceRegions,
                                                 HttpOutputMessage outputMessage) throws IOException {
        Assert.notNull(resourceRegions, "Collection of ResourceRegion should not be null");
        HttpHeaders responseHeaders = outputMessage.getHeaders();

        MediaType contentType = responseHeaders.getContentType();
        String boundaryString = MimeTypeUtils.generateMultipartBoundaryString();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, "multipart/byteranges; boundary=" + boundaryString);
        OutputStream out = outputMessage.getBody();

        for (ResourceRegion region : resourceRegions) {
            long start = region.getPosition();
            long end = start + region.getCount() - 1;
            InputStream in = null;
            try {
                // Writing MIME header.
                println(out);
                print(out, "--" + boundaryString);
                println(out);
                if (contentType != null) {
                    print(out, "Content-Type: " + contentType.toString());
                    println(out);
                }
                Long resourceLength = region.getResource().contentLength();
                end = Math.min(end, resourceLength - 1);
                print(out, "Content-Range: bytes " + start + '-' + end + '/' + resourceLength);
                println(out);
                println(out);

                // Printing content
                Resource resource = region.getResource();
                if (resource instanceof RangeAwareResource) {
                    in = ((RangeAwareResource) resource).getInputStream(start, end);
                    StreamUtils.copy(in, out);
                } else {
                    in = resource.getInputStream();
                    StreamUtils.copyRange(in, out, start, end);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        println(out);
        print(out, "--" + boundaryString + "--");
    }

    protected void println(OutputStream os) throws IOException {
        os.write('\r');
        os.write('\n');
    }

    protected void print(OutputStream os, String buf) throws IOException {
        os.write(buf.getBytes(StandardCharsets.US_ASCII));
    }

}
