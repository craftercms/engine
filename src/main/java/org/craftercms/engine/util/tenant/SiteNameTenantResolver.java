package org.craftercms.engine.util.tenant;

import javax.servlet.http.HttpServletRequest;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.profile.social.utils.TenantsResolver;

/**
 * {@link org.craftercms.profile.social.utils.TenantsResolver} that uses the current site name as tenant.
 *
 * @author avasquez
 */
public class SiteNameTenantResolver implements TenantsResolver {

    @Override
    public String[] getTenants() {
        HttpServletRequest request = RequestContext.getCurrent().getRequest();
        String tenant = (String) request.getAttribute(SiteContext.SITE_NAME_ATTRIBUTE);

        return new String[] { tenant };
    }

}
