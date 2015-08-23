package org.craftercms.engine.targeting.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.engine.targeting.TargetIdResolver;
import org.craftercms.engine.util.config.TargetingProperties;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public abstract class AbstractTargetIdResolver implements TargetIdResolver {

    @Override
    public String getFallbackTargetId() throws IllegalStateException {
        return TargetingProperties.getFallbackTargetId();
    }

    @Override
    public List<String> getAvailableTargetIds() {
        String[] availableTargetIds = TargetingProperties.getAvailableTargetIds();
        if (ArrayUtils.isNotEmpty(availableTargetIds)) {
            return Arrays.asList(availableTargetIds);
        } else {
            throw new IllegalStateException("No available target IDs specified in the configuration");
        }
    }

}
