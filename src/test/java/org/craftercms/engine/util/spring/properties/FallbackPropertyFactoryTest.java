/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.util.spring.properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class FallbackPropertyFactoryTest {

    public static final String PROPERTY1 = "property1";
    public static final String PROPERTY2 = "property2";
    public static final String VALUE1 = "value1";
    public static final String VALUE2 = "value2";
    public static final String DEFAULT_VALUE = "default_value";
    @Mock
    protected PropertySourcesPropertyResolver propertyResolver;

    protected List<String> properties = List.of(PROPERTY1, PROPERTY2);

    @InjectMocks
    protected FallbackPropertyFactory<String> propertyFactory = new FallbackPropertyFactory<>(properties, String.class);

    @Before
    public void setUp() {
        propertyFactory.setDefaultValue(DEFAULT_VALUE);
    }

    @Test
    public void propertyFallbackTest() throws Exception {
        when(propertyResolver.getProperty(PROPERTY2, String.class)).thenReturn(VALUE2);
        propertyFactory.afterPropertiesSet();
        String propertyValue = this.propertyFactory.getObject();
        assertEquals(propertyValue, VALUE2, "Property does not match the expected value");
    }

    @Test
    public void firstPropertyExistsTest() throws Exception {
        lenient().when(propertyResolver.getProperty(PROPERTY2, String.class)).thenReturn(VALUE2);
        lenient().when(propertyResolver.getProperty(PROPERTY1, String.class)).thenReturn(VALUE1);
        propertyFactory.afterPropertiesSet();
        String propertyValue = this.propertyFactory.getObject();
        assertEquals(propertyValue, VALUE1, "Property does not match the expected value");
    }

    @Test
    public void defaultValueTest() throws Exception {
        propertyFactory.afterPropertiesSet();
        String propertyValue = this.propertyFactory.getObject();
        assertEquals(propertyValue, DEFAULT_VALUE, "Property does not match the expected value");
    }

}
