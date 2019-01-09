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
import java.util.Collections;
import java.util.List;

import org.craftercms.engine.macro.MacroResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.junit.Assert.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link FolderScanningSiteListResolver}.
 *
 * @author avasquez
 */
public class FolderScanningSiteListResolverTest {

    private static final String BROCHURE_SITE_NAME = "brochure";
    private static final String CORPORATE_SITE_NAME = "corporate";
    private static final String PLUTON_SITE_NAME = "pluton";
    private static final String BROCHURE_SITE_FOLDER_NAME = BROCHURE_SITE_NAME + "app";
    private static final String CORPORATE_SITE_FOLDER_NAME = CORPORATE_SITE_NAME + "app";
    private static final String PLUTON_SITE_FOLDER_NAME = PLUTON_SITE_NAME + "app";
    private static final String SITE_NAME_MACRO_NAME = "siteName";
    private static final String SITE_FOLDER_NAME = "{" + SITE_NAME_MACRO_NAME + "}" + "app";
    private static final String SITE_ROOT_FOLDER_PATH_SUFFIX = File.separator + "{" + SITE_NAME_MACRO_NAME + "}" +
                                                               File.separator + "work-area";


    @Rule
    public TemporaryFolder sitesFolder = new TemporaryFolder();

    private FolderScanningSiteListResolver siteListResolver;

    @Before
    public void setUp() throws Exception {
        String siteRootFolderPath = "file:" + sitesFolder.getRoot().getAbsolutePath() + File.separator +
                                    SITE_FOLDER_NAME + SITE_ROOT_FOLDER_PATH_SUFFIX;

        sitesFolder.newFolder(BROCHURE_SITE_FOLDER_NAME);
        sitesFolder.newFolder(CORPORATE_SITE_FOLDER_NAME);
        sitesFolder.newFolder(PLUTON_SITE_FOLDER_NAME);

        siteListResolver = new FolderScanningSiteListResolver();
        siteListResolver.setMacroResolver(createMacroResolver());
        siteListResolver.setResourceLoader(createResourceLoader());
        siteListResolver.setSiteNameMacroName(SITE_NAME_MACRO_NAME);
        siteListResolver.setSiteRootFolderPath(siteRootFolderPath);

        siteListResolver.init();
    }

    @Test
    public void testGetSiteList() throws Exception {
        List<String> siteNames = (List<String>)siteListResolver.getSiteList();

        assertNotNull(siteNames);
        assertEquals(3, siteNames.size());

        Collections.sort(siteNames);

        assertEquals(BROCHURE_SITE_NAME, siteNames.get(0));
        assertEquals(CORPORATE_SITE_NAME, siteNames.get(1));
        assertEquals(PLUTON_SITE_NAME, siteNames.get(2));
    }

    private MacroResolver createMacroResolver() {
        MacroResolver macroResolver = mock(MacroResolver.class);

        doAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String)invocation.getArguments()[0];
            }

        }).when(macroResolver).resolveMacros(anyString());

        return macroResolver;
    }

    private ResourceLoader createResourceLoader() {
        return new FileSystemResourceLoader();
    }

}
