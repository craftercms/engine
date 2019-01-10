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
