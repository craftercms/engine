package org.craftercms.engine.util.spring.social;

import java.util.Map;
import java.util.Set;

import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;

/**
 * {@link org.springframework.social.connect.ConnectionFactoryLocator} that resolves {@link org.springframework.social
 * .connect.ConnectionFactory}s according to the current site. Basically, each site has it's own locator, which is
 * used to find the corresponding {@code ConnectionFactory} for the site. If you want to use a global locator in case
 * there's no specific site locator, just specify one with * as site name.
 *
 * @author avasquez
 */
public class ConnectionFactoryBySiteLocator implements ConnectionFactoryLocator {

    public static final String DEFAULT_LOCATOR_KEY = "*";

    private Map<String, ConnectionFactoryLocator> connectionFactoryLocators;

    @Required
    public void setConnectionFactoryLocators(final Map<String, ConnectionFactoryLocator> connectionFactoryLocators) {
        this.connectionFactoryLocators = connectionFactoryLocators;
    }

    @Override
    public ConnectionFactory<?> getConnectionFactory(final String providerId) {
        return getCurrentConnectionFactoryLocator().getConnectionFactory(providerId);
    }

    @Override
    public <A> ConnectionFactory<A> getConnectionFactory(final Class<A> apiType) {
        return getCurrentConnectionFactoryLocator().getConnectionFactory(apiType);
    }

    @Override
    public Set<String> registeredProviderIds() {
        return getCurrentConnectionFactoryLocator().registeredProviderIds();
    }

    private ConnectionFactoryLocator getCurrentConnectionFactoryLocator() {
        SiteContext context = AbstractSiteContextResolvingFilter.getCurrentContext();
        if (context == null) {
            throw new IllegalStateException("No site context associated to current request");
        }

        String siteName = context.getSiteName();
        ConnectionFactoryLocator locator = connectionFactoryLocators.get(siteName);

        if (locator == null) {
            locator = connectionFactoryLocators.get(DEFAULT_LOCATOR_KEY);
            if (locator == null) {
                throw new IllegalStateException("No ConnectionFactoryLocator found for site name '" + siteName + "'");
            }
        }

        return locator;
    }

}
