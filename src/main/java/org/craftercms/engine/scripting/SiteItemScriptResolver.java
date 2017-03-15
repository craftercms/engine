package org.craftercms.engine.scripting;

import java.util.List;

import org.craftercms.engine.model.SiteItem;

/**
 * Resolves the scripts URLs for a site item (page or component)
 *
 * @author Alfonso Vásquez
 */
public interface SiteItemScriptResolver {

    List<String> getScriptUrls(SiteItem item);

}
