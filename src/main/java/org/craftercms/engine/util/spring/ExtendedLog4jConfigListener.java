package org.craftercms.engine.util.spring;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.Log4jConfigListener;

/**
 * Create a simple way to override Log4j.xml files
 */
@SuppressWarnings("deprecation")
public class ExtendedLog4jConfigListener extends Log4jConfigListener {

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        try {
            super.contextInitialized(event);
        } catch (IllegalArgumentException ex) {
            Logger logger = LoggerFactory.getLogger(ExtendedLog4jConfigListener.class);
            logger.info("File classpath:crafter/engine/extension/log4j-override.xml was not found, going with " +
                        "built-in log4j settings");
        }
    }

}
