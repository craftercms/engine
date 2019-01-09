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
package org.craftercms.engine.service.context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.macro.MacroResolver;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Default implementation of {@link SiteListResolver}. Basically, parses the site root folder path to separate
 * the folder where all the sites reside and the site folder format. Then, iterates through the children of the
 * folder looking for site folders that match the format, extracting the site names from the folder name. To better
 * illustrate, here's an example:
 * <p/>
 * <p>Assume the site root folder path is file:/opt/websites/{siteName}app. The sites
 * folder, or basically the folder that contains all sites, is resolved to /opt/websites. The children of this folder
 * are brochureapp, corporateapp and plutonapp. By using the {siteName}app site name format, the site names are
 * finally determined to be brochure, corporate and pluton.</p>
 *
 * @author avasquez
 */
public class FolderScanningSiteListResolver implements SiteListResolver, ResourceLoaderAware {

    private static final Log logger = LogFactory.getLog(FolderScanningSiteListResolver.class);

    public static final String SITE_ROOT_FOLDER_PATH_REGEX = "^(([^:]+:)?(.+?/))([^/]*\\{%s\\}[^/]*)(/.*)?$";
    public static final int SITES_FOLDER_PATH_GROUP = 1;
    public static final int SITE_FOLDER_NAME_FORMAT_GROUP = 4;

    protected String siteRootFolderPath;
    protected String siteNameMacroName;
    protected MacroResolver macroResolver;
    protected ResourceLoader resourceLoader;

    protected String sitesFolderPath;
    protected Pattern siteFolderNamePattern;

    public FolderScanningSiteListResolver() {
        this.siteNameMacroName = SiteContextFactory.DEFAULT_SITE_NAME_MACRO_NAME;
    }

    @Required
    public void setSiteRootFolderPath(String siteRootFolderPath) {
        this.siteRootFolderPath = siteRootFolderPath;
    }

    public void setSiteNameMacroName(String siteNameMacroName) {
        this.siteNameMacroName = siteNameMacroName;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        String siteRootFolderPathRegex = String.format(SITE_ROOT_FOLDER_PATH_REGEX, siteNameMacroName);
        Pattern siteRootFolderPathPattern = Pattern.compile(siteRootFolderPathRegex);
        Matcher siteRootFolderPathMatcher = siteRootFolderPathPattern.matcher(siteRootFolderPath);

        if (siteRootFolderPathMatcher.matches()) {
            sitesFolderPath = siteRootFolderPathMatcher.group(SITES_FOLDER_PATH_GROUP);
            sitesFolderPath = macroResolver.resolveMacros(sitesFolderPath);

            String siteFolderNameFormat = siteRootFolderPathMatcher.group(SITE_FOLDER_NAME_FORMAT_GROUP);
            String siteFolderNameRegex = siteFolderNameFormat.replace("{" + siteNameMacroName + "}", "(.+)");

            siteFolderNamePattern = Pattern.compile(siteFolderNameRegex);
        } else {
            throw new IllegalStateException("The site root folder path " + siteRootFolderPath + " doesn't match " +
                                            "the regex " + siteRootFolderPathRegex);
        }
    }

    @Override
    public Collection<String> getSiteList() {
        List<String> siteNames = new ArrayList<>();
        File sitesFolder = getSitesFolder();

        if (sitesFolder != null) {
            File[] files = sitesFolder.listFiles();

            if (ArrayUtils.isNotEmpty(files)) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        Matcher siteFolderNameMatcher = siteFolderNamePattern.matcher(file.getName());
                        if (siteFolderNameMatcher.matches()) {
                            siteNames.add(siteFolderNameMatcher.group(1));
                        }
                    }
                }
            }
        }

        return siteNames;
    }

    protected File getSitesFolder() {
        try {
            File sitesFolder = resourceLoader.getResource(sitesFolderPath).getFile();
            if (sitesFolder.exists()) {
                logger.info("Sites folder resolved to " + sitesFolder.getAbsolutePath());

                return sitesFolder;
            } else {
                logger.error("Sites folder " + sitesFolderPath + " doesn't exist");

                return null;
            }
        } catch (IOException e) {
            logger.error("Unable to retrieve sites folder " + sitesFolderPath, e);

            return null;
        }
    }

}
