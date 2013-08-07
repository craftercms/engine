package org.craftercms.engine.test.utils;

import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.cache.CacheCallback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

/**
 * Test utils for mocking a {@link CacheTemplate}.
 *
 * @author Alfonso Vásquez
 */
public class CacheTemplateMockUtils {

    public static CacheTemplate setUpExecuteWithNoCaching(CacheTemplate mock) {
        when(mock.execute(any(Context.class), any(CachingOptions.class), any(CacheCallback.class), anyVararg())).then(
                new Answer<Object>() {

                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        CacheCallback<?> callback = (CacheCallback<?>) args[2];

                        return callback.doCacheable();
                    }

                }
        );

        return mock;
    }

}
