package org.craftercms.engine.scripting;

import java.util.List;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.util.UriTemplate;

/**
 * Scans the site context for scripts that have URL variables, for example, /scripts/rest/user/{username}.get.json.
 *
 * @author avasquez.
 */
public interface ScriptUrlTemplateScanner {

    /**
     * Scans the site context at a certain path to discover script URL templates.
     *
     * @param siteContext the site context to scan
     *
     * @return the list of URL templates
     */
    List<UriTemplate> scan(SiteContext siteContext);

}
