/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.core.util.cache.CachingAwareObject;
import org.craftercms.engine.exception.HttpStatusCodeAwareException;
import org.craftercms.engine.exception.RenderingException;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.util.GroovyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author Alfonso Vásquez
 */
public class CrafterPageView extends AbstractView implements CachingAwareObject, InitializingBean {

    private static final Log logger = LogFactory.getLog(CrafterPageView.class);

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";
    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String KEY_PAGE_MODEL = "model";
    public static final String KEY_MODE_PREVIEW = "modePreview";

    protected transient String scope;
    protected transient Object key;
    protected transient List<Object> dependencyKeys;
    protected transient Long cachingTime;

    protected SiteItem page;
    protected boolean modePreview;
    protected Locale locale;
    protected SiteItemService siteItemService;
    protected String pageViewNameXPathQuery;
    protected String mimeTypeXPathQuery;
    protected List<Script> scripts;
    protected ViewResolver delegatedViewResolver;

    public SiteItem getPage() {
        return page;
    }

    @Required
    public void setPage(SiteItem page) {
        this.page = page;
    }

    @Required
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Required
    public void setModePreview(boolean modePreview) {
        this.modePreview = modePreview;
    }

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Required
    public void setPageViewNameXPathQuery(String pageViewNameXPathQuery) {
        this.pageViewNameXPathQuery = pageViewNameXPathQuery;
    }

    @Required
    public void setMimeTypeXPathQuery(String mimeTypeXPathQuery) {
        this.mimeTypeXPathQuery = mimeTypeXPathQuery;
    }

    @Required
    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    @Required
    public void setDelegatedViewResolver(ViewResolver delegatedViewResolver) {
        this.delegatedViewResolver = delegatedViewResolver;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public void setKey(Object key) {
        this.key = key;
    }

    @Override
    public List<Object> getDependencyKeys() {
        return dependencyKeys;
    }

    @Override
    public void setDependencyKeys(List<Object> dependencyKeys) {
        this.dependencyKeys = dependencyKeys;
    }

    @Override
    public void addDependencyKeys(Collection<Object> dependencyKeys) {
        if (this.dependencyKeys == null) {
            this.dependencyKeys = new ArrayList<Object>();
        }

        this.dependencyKeys.addAll(dependencyKeys);
    }

    @Override
    public void addDependencyKey(Object dependencyKey) {
        if (dependencyKeys == null) {
            dependencyKeys = new ArrayList<Object>();
        }

        dependencyKeys.add(dependencyKey);
    }

    @Override
    public boolean removeDependencyKeys(Collection<Object> dependencyKeys) {
        if (this.dependencyKeys != null) {
            return this.dependencyKeys.removeAll(dependencyKeys);
        } else {
            return false;
        }
    }

    @Override
    public boolean removeDependencyKey(Object dependencyKey) {
        if (dependencyKeys != null) {
            return dependencyKeys.remove(dependencyKey);
        } else {
            return false;
        }
    }

    @Override
    public Long getCachingTime() {
        return cachingTime;
    }

    @Override
    public void setCachingTime(Long cachingTime) {
        this.cachingTime = cachingTime;
    }

    @Override
    public String toString() {
        return "CrafterPageView[" +
                "page=" + page +
                ", locale=" + locale +
                ']';
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String mimeType = getMimeType();
        if (StringUtils.isNotEmpty(mimeType)) {
            setContentType(MediaType.parseMediaType(mimeType).toString() + ";charset=" + DEFAULT_CHARSET);
        } else {
            setContentType(DEFAULT_CONTENT_TYPE);
        }
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        Map<String, Object> scriptVariables = createScriptVariables(request, response, model);

        if (CollectionUtils.isNotEmpty(scripts)) {
            for (Script script : scripts) {
                executeScript(script, scriptVariables);

                // If the response has been already committed by one of the scripts, stop and don't render the view
                if (response.isCommitted()) {
                    logger.debug("Response already committed by script");

                    return;
                }
            }
        }

        model.put(KEY_PAGE_MODEL, page);
        model.put(KEY_MODE_PREVIEW, modePreview);

        renderActualView(getPageViewName(), model, request, response);
    }

    protected void executeScript(Script script, Map<String, Object> scriptVariables) throws Exception {
        try {
            script.execute(scriptVariables);
        } catch (Exception e) {
            Exception cause = (Exception) ExceptionUtils.getThrowableOfType(e, HttpStatusCodeAwareException.class);
            if (cause != null) {
                throw cause;
            } else {
                throw e;
            }
        }
    }

    protected Map<String, Object> createScriptVariables(HttpServletRequest request, HttpServletResponse response,
                                                        Map<String, Object> model) {
        Map<String, Object> scriptVariables = new HashMap<String, Object>();
        GroovyUtils.addCommonVariables(scriptVariables, request, response, getServletContext());
        GroovyUtils.addCrafterVariables(scriptVariables, page);
        GroovyUtils.addModelVariable(scriptVariables, model);

        return scriptVariables;
    }

    protected String getPageViewName() throws RenderingException {
        String pageViewName = page.getItem().queryDescriptorValue(pageViewNameXPathQuery);
        if (StringUtils.isNotEmpty(pageViewName)) {
            return pageViewName;
        } else {
            throw new RenderingException("No view name found for " + page);
        }
    }

    protected String getMimeType() {
        return page.getItem().queryDescriptorValue(mimeTypeXPathQuery);
    }

    protected void renderActualView(String pageViewName, Map<String, Object> model, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        View actualView = delegatedViewResolver.resolveViewName(pageViewName, locale);
        if (actualView == null) {
            throw new RenderingException("No view was resolved for page view name '" + pageViewName + "'");
        }
        if (actualView instanceof AbstractView) {
            ((AbstractView) actualView).setContentType(getContentType());
        }

        actualView.render(model, request, response);
    }

}
