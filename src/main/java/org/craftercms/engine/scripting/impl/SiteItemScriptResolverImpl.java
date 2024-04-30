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

package org.craftercms.engine.scripting.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Default implementation of {@link SiteItemScriptResolver}.
 *
 * @author Alfonso Vásquez
 */
public class SiteItemScriptResolverImpl implements SiteItemScriptResolver {

    private static final Log logger = LogFactory.getLog(SiteItemScriptResolverImpl.class);

    protected ContentStoreService storeService;
    protected String contentTypeXPathQuery;
    protected Pattern contentTypePattern;
    protected String scriptUrlFormat;
    protected String scriptsXPathQuery;

    public SiteItemScriptResolverImpl(ContentStoreService storeService, String contentTypeXPathQuery, String contentTypePattern,
                                      String scriptUrlFormat, String scriptsXPathQuery) {
        this.storeService = storeService;
        this.contentTypeXPathQuery = contentTypeXPathQuery;
        this.contentTypePattern = Pattern.compile(contentTypePattern);
        this.scriptUrlFormat = scriptUrlFormat;
        this.scriptsXPathQuery = scriptsXPathQuery;
    }

    @Override
    public List<String> getScriptUrls(SiteItem item) {
        List<String> scriptUrls = null;
        SiteContext siteContext = SiteContext.getCurrent();

        if (siteContext != null) {
            String contentType = item.queryValue(contentTypeXPathQuery);

            if (StringUtils.isNotEmpty(contentType)) {
                String scriptUrl = getScriptUrlForContentType(contentType);
                if (StringUtils.isNotEmpty(scriptUrl)) {
                    try {
                        // Check that the script exists. If not, ignore.
                        if (storeService.exists(siteContext.getContext(), scriptUrl)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Script for content type '" + contentType + "' found at " + scriptUrl);
                            }

                            scriptUrls = new ArrayList<>();
                            scriptUrls.add(scriptUrl);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("No script for content type '" + contentType + "' found at " + scriptUrl);
                        }
                    } catch (CrafterException e) {
                        logger.error("Error retrieving script for content type '" + contentType + "' at " +
                                     scriptUrl, e);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(scriptUrls)) {
                List<String> additionalUrls = item.queryValues(scriptsXPathQuery);

                if (scriptUrls == null) {
                    scriptUrls = new ArrayList<>();
                }

                if (CollectionUtils.isNotEmpty(additionalUrls)) {
                    scriptUrls.addAll(additionalUrls);
                }
            } else {
                scriptUrls = item.queryValues(scriptsXPathQuery);
            }
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
