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
package org.craftercms.engine.controller.preview.rest;

import org.apache.commons.lang.StringUtils;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.core.util.CollectionUtils;
import org.craftercms.security.api.SecurityConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for integration with Crafter Profile.
 *
 * @author Russ Danner
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + ProfileRestController.URL_ROOT)
public class ProfileRestController {

    public static final String URL_ROOT = "/profile";

    public static final String PROFILE_SESSION_ATTRIBUTE = "_cr_profile_state";

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getProfile(HttpSession session) {
        Map<String, String> profile = (Map<String, String>) session.getAttribute(PROFILE_SESSION_ATTRIBUTE);
        if (profile == null || StringUtils.isEmpty(profile.get("username"))) {
            profile = new HashMap<String, String>();
            profile.put("username", SecurityConstants.ANONYMOUS_USERNAME);

            session.setAttribute(PROFILE_SESSION_ATTRIBUTE, profile);
        }

        return profile;
    }

    @RequestMapping(value = "/set", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> setProfile(HttpServletRequest request, HttpSession session) {
        Map<String, String> profile = new HashMap<String, String>();
        Enumeration<String> paramNamesEnum = request.getParameterNames();

        while (paramNamesEnum.hasMoreElements()) {
            String paramName = paramNamesEnum.nextElement();
            profile.put(paramName, request.getParameter(paramName).trim());
        }

        if (!profile.containsKey("username")) {
            profile.put("username", SecurityConstants.ANONYMOUS_USERNAME);
        }

        session.setAttribute(PROFILE_SESSION_ATTRIBUTE, profile);

        return CollectionUtils.asMap("username", profile.get("username"));
    }

}
