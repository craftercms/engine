package org.craftercms.engine.controller;

import java.util.HashMap;
import java.util.List;

import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping(value = "/log", method = RequestMethod.GET,produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<HashMap<String,Object>> logger(@RequestParam final String siteId) {
        return CircularQueueLogAppender.loggerQueue().getLoggedEvents(siteId);
    }
}
