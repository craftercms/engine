/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.spring.servlet;

import org.springframework.context.ApplicationContextAware;

import javax.servlet.Servlet;

/**
 * Extension of {@link Servlet} that also implements {@link ApplicationContextAware}
 *
 * @author joseross
 * @since 3.1.6
 */
public interface AppContextAwareServlet extends Servlet, ApplicationContextAware {

    // no additional methods needed

}
