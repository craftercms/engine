package org.craftercms.engine.test.utils;

import org.craftercms.commons.lang.Callback;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.cache.CacheTemplate;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.when;

/**
 * Test utils for mocking a {@link CacheTemplate}.
 *
 * @author Alfonso Vásquez
 */
public class CacheTemplateMockUtils {

    public static CacheTemplate setUpWithNoCaching(CacheTemplate mock) {
        when(mock.getObject(any(Context.class), any(Callback.class), anyVararg())).then(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Callback<?> callback = (Callback<?>) args[1];

                return callback.execute();
            }

        });
        when(mock.getObject(any(Context.class), any(CachingOptions.class), any(Callback.class), anyVararg())).then(
            new Answer<Object>() {

                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments();
                    Callback<?> callback = (Callback<?>) args[2];

                    return callback.execute();
                }

            }
        );

        return mock;
    }

}
