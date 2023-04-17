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

package org.craftercms.engine;

import org.craftercms.search.opensearch.OpenSearchWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:crafter/engine/services/main-services-context.xml"})
@TestPropertySource(properties = {"crafter.engine.extension.base = classpath*:crafter/engine/extension"})
public class SpringContextTest extends AbstractTestNGSpringContextTests {

    @Autowired
    OpenSearchWrapper searchWrapper;

    @Test
    public void testOk() {
        assertNotNull(searchWrapper);
    }
}
