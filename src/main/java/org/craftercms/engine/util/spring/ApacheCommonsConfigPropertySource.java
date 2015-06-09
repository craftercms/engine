package org.craftercms.engine.util.spring;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.configuration.Configuration;
import org.springframework.core.env.EnumerablePropertySource;

/**
 * Implementation of {@link EnumerablePropertySource} where a source is an Apache Commons {@link Configuration}.
 *
 * @author avasquez
 */
public class ApacheCommonsConfigPropertySource extends EnumerablePropertySource<Configuration> {

    public ApacheCommonsConfigPropertySource(String name, Configuration source) {
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        return IteratorUtils.toArray(source.getKeys(), String.class);
    }

    @Override
    public Object getProperty(String name) {
        return source.getString(name);
    }

}
