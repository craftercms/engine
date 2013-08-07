package org.craftercms.engine.test.utils;

import org.apache.commons.io.IOUtils;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.impl.CachedContent;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Test utils for mocking a {@link ContentStoreService}.
 *
 * @author Alfonso Vásquez
 */
public class ContentStoreServiceMockUtils {

    public static ContentStoreService setUpGetContentFromClassPath(ContentStoreService mock) {
        Answer<Content> getContentAnswer = new Answer<Content>() {

            @Override
            public Content answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String url;

                if (args.length == 2) {
                    url = (String) args[1];
                } else {
                    url = (String) args[2];
                }

                Content content = getContentFromClassPath(url);
                if (content == null) {
                    throw new PathNotFoundException();
                }

                return content;
            }

        };

        when(mock.getContent(any(Context.class), anyString())).then(getContentAnswer);
        when(mock.getContent(any(Context.class), any(CachingOptions.class), anyString())).then(getContentAnswer);

        return mock;
    }

    public static Content getContentFromClassPath(String url) throws IOException {
        ClassPathResource resource = new ClassPathResource(url);
        if (resource.exists()) {
            byte[] data = IOUtils.toByteArray(resource.getInputStream());

            return new CachedContent(data, resource.lastModified());
        } else {
            return null;
        }
    }

}
