package org.craftercms.engine.targeting.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.targeting.CandidateTargetIdsResolver;

/**
 * Created by alfonsovasquez on 14/8/15.
 */
public class CandidateTargetIdsResolverImpl implements CandidateTargetIdsResolver {

    public static final String DEFAULT_TARGET_ID_SEPARATOR = "_";

    protected String targetIdSeparator;

    public CandidateTargetIdsResolverImpl() {
        targetIdSeparator = DEFAULT_TARGET_ID_SEPARATOR;
    }

    public void setTargetIdSeparator(String targetIdSeparator) {
        this.targetIdSeparator = targetIdSeparator;
    }

    @Override
    public List<String> getTargetIds(String targetId, String defaultTargetId) {
        List<String> targetIds = new ArrayList<>();
        String[] targetIdComponents = StringUtils.split(targetId, targetIdSeparator);

        targetIds.add(targetId);

        if (ArrayUtils.isNotEmpty(targetIdComponents)) {
            for (int i = targetIdComponents.length - 1; i > 0; i--) {
                targetIds.add(StringUtils.join(targetIdComponents, targetIdSeparator, 0, i));
            }
        }

        if (!targetIds.contains(defaultTargetId)) {
            targetIds.add(defaultTargetId);
        }

        return targetIds;
    }

}
