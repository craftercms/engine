package org.craftercms.engine.targeting.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.targeting.TargetIdResolver;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public class TargetedUrlByFileStrategy extends AbstractTargetedUrlStrategy {

    public static final String DEFAULT_TARGET_ID_SEPARATOR = "_";
    public static final String TARGETED_URL_PATTERN_FORMAT = "^(.+)%s(%s)(\\.[^.]+)$";

    public static final int PREFIX_GROUP = 1;
    public static final int TARGET_ID_GROUP = 2;
    public static final int SUFFIX_GROUP = 3;

    protected TargetIdResolver targetIdResolver;
    protected String targetIdSeparator;

    public TargetedUrlByFileStrategy() {
        targetIdSeparator = DEFAULT_TARGET_ID_SEPARATOR;
    }

    @Required
    public void setTargetIdResolver(TargetIdResolver targetIdResolver) {
        this.targetIdResolver = targetIdResolver;
    }

    public void setTargetIdSeparator(String targetIdSeparator) {
        this.targetIdSeparator = targetIdSeparator;
    }

    @Override
    public String asTargetedUrl(String url) {
        Matcher matcher = matchUrl(url);
        if (matcher != null) {
            return url;
        } else {
            String urlWithoutExt = FilenameUtils.removeExtension(url);
            String ext = FilenameUtils.getExtension(url);
            String targetId = targetIdResolver.getCurrentTargetId();

            return urlWithoutExt + "_" + targetId + "." + ext;
        }
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        String targetedUrl = "";

        if (StringUtils.isNotEmpty(prefix)) {
            targetedUrl += prefix;
        }
        if (StringUtils.isNotEmpty(targetId)) {
            if (StringUtils.isNotEmpty(targetedUrl)) {
                targetedUrl = StringUtils.appendIfMissing(targetedUrl, targetIdSeparator);
            }

            targetedUrl += targetId;
        }
        if (StringUtils.isNotEmpty(suffix)) {
            targetedUrl += StringUtils.prependIfMissing(suffix, ".");
        }

        return targetedUrl;
    }

    @Override
    protected String getPrefix(Matcher matcher) {
        return matcher.group(PREFIX_GROUP);
    }

    @Override
    protected String getTargetId(Matcher matcher) {
        return matcher.group(TARGET_ID_GROUP);
    }

    @Override
    protected String getSuffix(Matcher matcher) {
        return matcher.group(SUFFIX_GROUP);
    }

    @Override
    protected Pattern getTargetedUrlPattern() {
        String availableTargetIds = StringUtils.join(targetIdResolver.getAvailableTargetIds(), '|');
        String targetedUrlByFilePattern = String.format(TARGETED_URL_PATTERN_FORMAT, targetIdSeparator,
                                                        availableTargetIds);

        return Pattern.compile(targetedUrlByFilePattern);
    }

}
