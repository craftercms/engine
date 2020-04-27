/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.craftercms.engine.util.spring.servlet;

import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * Based on {@link org.springframework.web.servlet.mvc.ServletWrappingController}
 *
 * Note: the class can't be extended because all fields are private
 *
 * @author joseross
 * @since 3.1.6
 */
public class AppContextAwareServletWrappingController extends AbstractController
        implements BeanNameAware, InitializingBean, DisposableBean {

    // Override the class to use the extended Servlet
    private Class<? extends AppContextAwareServlet> servletClass;

    private String servletName;

    private Properties initParameters = new Properties();

    private String beanName;

    // Override the instance to use the extended Servlet
    private AppContextAwareServlet servletInstance;


    public AppContextAwareServletWrappingController() {
        super(false);
    }


    /**
     * Set the class of the servlet to wrap.
     * Needs to implement {@code javax.servlet.Servlet}.
     * @see javax.servlet.Servlet
     */
    public void setServletClass(Class<? extends AppContextAwareServlet> servletClass) {
        this.servletClass = servletClass;
    }

    /**
     * Set the name of the servlet to wrap.
     * Default is the bean name of this controller.
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    /**
     * Specify init parameters for the servlet to wrap,
     * as name-value pairs.
     */
    public void setInitParameters(Properties initParameters) {
        this.initParameters = initParameters;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }


    /**
     * Initialize the wrapped Servlet instance.
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.servletClass == null) {
            throw new IllegalArgumentException("'servletClass' is required");
        }
        if (this.servletName == null) {
            this.servletName = this.beanName;
        }
        this.servletInstance = this.servletClass.newInstance();

        // expose the app context to the servlet instance
        servletInstance.setApplicationContext(getApplicationContext());

        this.servletInstance.init(new DelegatingServletConfig());
    }


    /**
     * Invoke the wrapped Servlet instance.
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        this.servletInstance.service(request, response);
        return null;
    }


    /**
     * Destroy the wrapped Servlet instance.
     * @see javax.servlet.Servlet#destroy()
     */
    @Override
    public void destroy() {
        this.servletInstance.destroy();
    }


    /**
     * Internal implementation of the ServletConfig interface, to be passed
     * to the wrapped servlet. Delegates to ServletWrappingController fields
     * and methods to provide init parameters and other environment info.
     */
    private class DelegatingServletConfig implements ServletConfig {

        @Override
        public String getServletName() {
            return servletName;
        }

        @Override
        public ServletContext getServletContext() {
            return AppContextAwareServletWrappingController.this.getServletContext();
        }

        @Override
        public String getInitParameter(String paramName) {
            return initParameters.getProperty(paramName);
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Enumeration<String> getInitParameterNames() {
            return (Enumeration) initParameters.keys();
        }
    }

}
