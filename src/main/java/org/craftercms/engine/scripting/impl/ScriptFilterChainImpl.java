package org.craftercms.engine.scripting.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.util.GroovyUtils;

/**
 * {@link FilterChain} implementation that executes a chain of scripts, before delegating to the actual servlet
 * filter chain.
 *
 * @author avasquez
 */
public class ScriptFilterChainImpl implements FilterChain {

    private static final Log logger = LogFactory.getLog(ScriptFilterChainImpl.class);

    private Iterator<Script> scriptIterator;
    private FilterChain delegateChain;
    private ServletContext servletContext;

    public ScriptFilterChainImpl(Iterator<Script> scriptIterator, FilterChain delegateChain,
                                 ServletContext servletContext) {
        this.scriptIterator = scriptIterator;
        this.delegateChain = delegateChain;
        this.servletContext = servletContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (scriptIterator.hasNext()){
            Script script = scriptIterator.next();

            if (logger.isDebugEnabled()) {
                logger.debug("Executing filter script " + script);
            }

            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            Map<String, Object> variables = new HashMap<>();

            GroovyUtils.addCommonVariables(variables, httpRequest, httpResponse, servletContext);
            GroovyUtils.addSecurityVariables(variables);
            GroovyUtils.addFilterChainVariable(variables, this);

            try {
                script.execute(variables);
            } catch (ScriptException e) {
                throw new ServletException("Error while executing filter script " + script, e);
            }
        } else {
            delegateChain.doFilter(request, response);
        }
    }

}
