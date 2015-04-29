package org.craftercms.engine.util.tenant;

import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.security.utils.tenant.TenantsResolver;

/**
 * {@link TenantsResolver} that uses the current site name as tenant.
 *
 * @author avasquez
 */
public class SiteNameTenantResolver implements TenantsResolver {

    @Override
    public String[] getTenants() {
        return new String[] { SiteContext.getCurrent().getSiteName() };
    }

}
