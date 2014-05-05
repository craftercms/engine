package org.craftercms.engine.scripting.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.ScriptResolver;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link org.craftercms.engine.scripting.ScriptResolver}.
 *
 * @author Alfonso Vásquez
 */
public class ScriptResolverImpl implements ScriptResolver {

    private static final Log logger = LogFactory.getLog(ScriptResolverImpl.class);

    protected ContentStoreService storeService;
    protected String contentTypeXPathQuery;
    protected Pattern contentTypePattern;
    protected String scriptUrlFormat;
    protected String scriptsXPathQuery;

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setContentTypeXPathQuery(String contentTypeXPathQuery) {
        this.contentTypeXPathQuery = contentTypeXPathQuery;
    }

    @Required
    public void setContentTypePattern(String contentTypePattern) {
        this.contentTypePattern = Pattern.compile(contentTypePattern);
    }

    @Required
    public void setScriptUrlFormat(String scriptUrlFormat) {
        this.scriptUrlFormat = scriptUrlFormat;
    }

    @Required
    public void setScriptsXPathQuery(String scriptsXPathQuery) {
        this.scriptsXPathQuery = scriptsXPathQuery;
    }

    @Override
    public List<String> getScriptUrls(SiteItem item) {
        List<String> scriptUrls = null;
        Context context = AbstractSiteContextResolvingFilter.getCurrentContext().getContext();
        String contentType = item.getItem().queryDescriptorValue(contentTypeXPathQuery);

        if (StringUtils.isNotEmpty(contentType)) {
            String scriptUrl = getScriptUrlForContentType(contentType);
            if (StringUtils.isNotEmpty(scriptUrl)) {
                try {
                    // Check that the script exists. If not, ignore.
                    storeService.getContent(context, scriptUrl);

                    scriptUrls = new ArrayList<String>();
                    scriptUrls.add(scriptUrl);
                } catch (PathNotFoundException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No script for content type '" + contentType + "' found at " + scriptUrl, e);
                    }
                } catch (CrafterException e) {
                    logger.error("Error while retrieving script for content type '" + contentType + "' at " +
                            scriptUrl, e);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            List<String> additionalUrls = item.getItem().queryDescriptorValues(scriptsXPathQuery);
            if (CollectionUtils.isNotEmpty(additionalUrls)) {
                scriptUrls.addAll(additionalUrls);
            }
        } else {
            scriptUrls = item.getItem().queryDescriptorValues(scriptsXPathQuery);
        }

        return scriptUrls;
    }

    protected String getScriptUrlForContentType(String contentType) {
        Matcher contentTypeMatcher = contentTypePattern.matcher(contentType);
        if (contentTypeMatcher.matches()) {
            String contentTypeName = contentTypeMatcher.group(1);
            contentTypeName = StringUtils.strip(contentTypeName, "/");

            return String.format(scriptUrlFormat, contentTypeName);
        } else {
            return null;
        }
    }

}
