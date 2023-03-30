package org.craftercms.engine;

import org.craftercms.search.opensearch.OpenSearchWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:crafter/engine/services/main-services-context.xml"})
@TestPropertySource(properties = {"crafter.engine.extension.base = classpath*:crafter/engine/extension"})
public class SpringContextTest extends AbstractTestNGSpringContextTests {
    @AfterTest
    public static void afterTest() {
    }

    @Autowired
    OpenSearchWrapper searchWrapper;

    @Test
    public void TestOk() {
        assertNotNull(searchWrapper);
    }
}
