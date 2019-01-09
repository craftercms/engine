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

package org.craftercms.engine.util.spring;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a simple way to override Log4j.xml files
 */
@SuppressWarnings("deprecation")
public class ExtendedLog4jConfigListener extends org.springframework.web.util.Log4jConfigListener {

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
