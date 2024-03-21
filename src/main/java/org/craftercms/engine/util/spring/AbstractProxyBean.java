/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.util.spring;

/**
 * Created by alfonsovasquez on 21/8/15.
 */
public abstract class AbstractProxyBean<T> {

    protected ApplicationContextAccessor applicationContext;
    protected String beanName;

    public AbstractProxyBean(ApplicationContextAccessor applicationContext, String beanName) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
    }

    protected T getBean() {
        return applicationContext.get(beanName, getBeanClass());
    }

    protected abstract Class<? extends T> getBeanClass();

}
