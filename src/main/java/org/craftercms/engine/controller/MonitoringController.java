package org.craftercms.engine.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.craftercms.commons.monitoring.MemoryMonitor;
import org.craftercms.commons.monitoring.StatusMonitor;
import org.craftercms.commons.monitoring.VersionMonitor;
import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    public final static String URL_ROOT = "/api/1/monitoring";
    public final static String MEMORY_TARGET_URL = "/memory";
    public final static String STATUS_TARGET_URL = "/status";
    public final static String VERSION_TARGET_URL = "/version";
    public final static String LOG_TARGET_URL = "/log";

    @GetMapping(MEMORY_TARGET_URL)
    public ResponseEntity<List<MemoryMonitor>> getMemoryStats() {
        return new ResponseEntity<>(MemoryMonitor.getMemoryStats(), HttpStatus.OK);
    }


    @GetMapping(STATUS_TARGET_URL)
    public ResponseEntity<StatusMonitor> getCurrentStatus() {
        return new ResponseEntity<>(StatusMonitor.getCurrentStatus(), HttpStatus.OK);
    }

    @GetMapping(VERSION_TARGET_URL)
    public ResponseEntity<VersionMonitor> getCurrentVersion() throws Exception {
        try {
            return new ResponseEntity<>(VersionMonitor.getVersion(this.getClass()), HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Unable to read manifest file", e);
            throw new Exception("Unable to read manifest file");
        }
    }

    @GetMapping(LOG_TARGET_URL)
    public List<HashMap<String,Object>> getAppLogger(@RequestParam String site, @RequestParam long since) {
        return CircularQueueLogAppender.loggerQueue().getLoggedEvents(site, since);
    }

}
