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
package org.craftercms.engine.scripting.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptUrlTemplateScanner;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.util.UriTemplate;

/**
 * Default implementation of {@link ScriptUrlTemplateScanner}, which retrieves all the Groovy scripts at the
 * specified {@code scriptsFolder} path, and then looks for those that have the URL variable placeholder in them.
 *
 * @author avasquez
 */
public class ScriptUrlTemplateScannerImpl implements ScriptUrlTemplateScanner {

    public static final String DEFAULT_URL_VARIABLE_PLACEHOLDER_PATTERN = "\\{[^{}]+\\}";

    protected Pattern urlVariablePlaceholderPattern;
    protected String scriptsFolder;

    public ScriptUrlTemplateScannerImpl() {
        urlVariablePlaceholderPattern = Pattern.compile(DEFAULT_URL_VARIABLE_PLACEHOLDER_PATTERN);
    }

    public void setUrlVariablePlaceholderPattern(String urlVariablePlaceholderPattern) {
        this.urlVariablePlaceholderPattern = Pattern.compile(urlVariablePlaceholderPattern);
    }

    @Required
    public void setScriptsFolder(String scriptsFolder) {
        this.scriptsFolder = scriptsFolder;
    }

    @Override
    public List<UriTemplate> scan(SiteContext siteContext) {
        Context context = siteContext.getContext();
        ContentStoreService storeService = siteContext.getStoreService();
        ScriptFactory scriptFactory = siteContext.getScriptFactory();
        List<String> scriptUrls = new ArrayList<>();
        List<UriTemplate> urlTemplates = new ArrayList<>();

        findScripts(context, storeService, scriptFactory, scriptsFolder, scriptUrls);

        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            for (String scriptUrl : scriptUrls) {
                Matcher matcher = urlVariablePlaceholderPattern.matcher(scriptUrl);
                if (matcher.find()) {
                    urlTemplates.add(new UriTemplate(scriptUrl));
                }
            }
        }

        return urlTemplates;
    }

    public void findScripts(Context context, ContentStoreService storeService, ScriptFactory scriptFactory,
                            String folder, List<String> scriptUrls) {
        List<Item> items = storeService.findChildren(context, null, folder, null, null);
        String scriptFileExtension = scriptFactory.getScriptFileExtension();

        if (CollectionUtils.isNotEmpty(items)) {
            for (Item item : items) {
                if (!item.isFolder() && item.getName().endsWith(scriptFileExtension)) {
                    scriptUrls.add(item.getUrl());
                } else if (item.isFolder()) {
                    findScripts(context, storeService, scriptFactory, item.getUrl(), scriptUrls);
                }
            }
        }
    }

}
