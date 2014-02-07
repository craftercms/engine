package org.craftercms.engine.scripting;

import org.craftercms.engine.model.SiteItem;

import java.util.List;

/**
 * Resolves the scripts URLs for a site item (page or component)
 *
 * @author Alfonso Vásquez
 */
public interface ScriptResolver {

    List<String> getScriptUrls(SiteItem item);

}
