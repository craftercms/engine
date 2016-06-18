package org.craftercms.engine.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController {

    public final static String URL_ROOT = "/api/1/monitoring";

    public static final String ACTION_PATH_VAR = "action";

    private String statusMessage;

    @Required
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getStatus() {
        return Collections.singletonMap("status", statusMessage);
    }

    @RequestMapping(value = "/log", method = RequestMethod.GET)
    @ResponseBody
    public List<HashMap<String,Object>> logger(@RequestParam String siteId, @RequestParam long since) {
        return CircularQueueLogAppender.loggerQueue().getLoggedEvents(siteId,since);
    }

}
