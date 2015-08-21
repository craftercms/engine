package org.craftercms.engine.targeting.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public abstract class AbstractTargetedUrlStrategy implements TargetedUrlStrategy {

    @Override
    public TargetedUrlComponents parseTargetedUrl(String targetedUrl) {
        Matcher matcher = matchUrl(targetedUrl);
        if (matcher != null) {
            TargetedUrlComponents urlComp = new TargetedUrlComponents();
            urlComp.setPrefix(getPrefix(matcher));
            urlComp.setTargetId(getTargetId(matcher));
            urlComp.setSuffix(getSuffix(matcher));

            return urlComp;
        } else {
            return null;
        }
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        String targetedUrl = "";

        if (StringUtils.isNotEmpty(prefix)) {
            targetedUrl += prefix;
        }
        if (StringUtils.isNotEmpty(targetId)) {
            targetedUrl += targetId;
        }
        if (StringUtils.isNotEmpty(suffix)) {
            targetedUrl += suffix;
        }

        return targetedUrl;
    }

    protected Matcher matchUrl(String url) {
        Pattern pattern = getTargetedUrlPattern();
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            return matcher;
        } else {
            return null;
        }
    }

    protected abstract String getPrefix(Matcher matcher);

    protected abstract String getTargetId(Matcher matcher);

    protected abstract String getSuffix(Matcher matcher);

    protected abstract Pattern getTargetedUrlPattern();

}
