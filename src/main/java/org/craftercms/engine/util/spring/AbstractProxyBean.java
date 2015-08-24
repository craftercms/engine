package org.craftercms.engine.util.spring;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 21/8/15.
 */
public abstract class AbstractProxyBean<T> {

    protected ApplicationContextAccessor applicationContext;
    protected String beanName;

    @Required
    public void setApplicationContext(ApplicationContextAccessor applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Required
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    protected T getBean() {
        return applicationContext.get(beanName, getBeanClass());
    }

    protected abstract Class<? extends T> getBeanClass();

}
