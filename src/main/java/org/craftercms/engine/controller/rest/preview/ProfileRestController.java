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
package org.craftercms.engine.controller.rest.preview;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * REST controller for integration with Crafter Profile.
 *
 * @author Russ Danner
 * @author Alfonso Vásquez
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + ProfileRestController.URL_ROOT)
public class ProfileRestController {

    public static final String URL_ROOT = "/profile";

    public static final String PROFILE_SESSION_ATTRIBUTE = "_cr_profile_state";

    public static final String CLEANSE_ATTRS_CONFIG_KEY = "preview.targeting.cleanseAttributes";

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @SuppressWarnings("unchecked")
    public Map<String, String> getProfile(HttpSession session) {
        Map<String, String> profile = (Map<String, String>) session.getAttribute(PROFILE_SESSION_ATTRIBUTE);
        if (profile == null) {
            profile = new HashMap<>();
            session.setAttribute(PROFILE_SESSION_ATTRIBUTE, profile);
        }

        return profile;
    }

    @RequestMapping(value = "/set", method = RequestMethod.GET)
    public Map<String, String> setProfile(HttpServletRequest request, HttpSession session) {
        boolean cleanseAttributes = shouldCleanseAttributes();

        Map<String, String> profile = new HashMap<String, String>();
        Enumeration<String> paramNamesEnum = request.getParameterNames();

        while (paramNamesEnum.hasMoreElements()) {
            String paramName = paramNamesEnum.nextElement();
            String paramValue = request.getParameter(paramName);
            if (isNotEmpty(paramValue)) {
                String value = paramValue.trim();
                profile.put(paramName, cleanseAttributes? StringEscapeUtils.escapeHtml4(value) : value);
            }
        }

        // change the id so the authentication object is updated
        profile.put("id", new ObjectId().toHexString());

        session.setAttribute(PROFILE_SESSION_ATTRIBUTE, profile);

        return profile;
    }

    @SuppressWarnings("rawtypes")
    protected boolean shouldCleanseAttributes() {
        HierarchicalConfiguration siteConfig = ConfigUtils.getCurrentConfig();
        return siteConfig != null && siteConfig.getBoolean(CLEANSE_ATTRS_CONFIG_KEY, true);
    }

}
