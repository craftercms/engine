package org.craftercms.engine.controller;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController {

    public final static String URL_ROOT = "/api/1/monitoring";

    public static final String ACTION_PATH_VAR = "action";

    private String statusViewNamePrefix;

    @Required
    public void setStatusViewNamePrefix(String statusViewNamePrefix) {
        this.statusViewNamePrefix = statusViewNamePrefix;
    }

    @RequestMapping(value = "/{" + ACTION_PATH_VAR + "}", method = RequestMethod.GET)
    public String render(@PathVariable(ACTION_PATH_VAR) String action) {
        return statusViewNamePrefix + action + ".ftl";
    }
}
