package org.craftercms.engine.util.servlet;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

/**
 * A simple class that adds a bunch of attributes passed as a property to the current servlet context.
 *
 * @author avasquez
 */
public class ServletContextAttributesBootstrap implements ServletContextAware {

    private ServletContext servletContext;
    private Map<String, Object> attributes;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Required
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @PostConstruct
    public void init() {
        if (servletContext == null) {
            throw new IllegalStateException("There's no current ServletContext");
        }

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            servletContext.setAttribute(entry.getKey(), entry.getValue());
        }
    }

}
