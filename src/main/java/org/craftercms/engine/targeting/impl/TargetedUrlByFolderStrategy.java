package org.craftercms.engine.targeting.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.util.UrlUtils;

/**
 * {@link org.craftercms.engine.targeting.TargetedUrlStrategy} implementation that recognizes the target ID in
 * the first folder of the URL name (e.g. /en/products/index.xml).
 *
 * @author avasquez
 */
public class TargetedUrlByFolderStrategy extends AbstractTargetedUrlStrategy {

    public static final String TARGETED_URL_REGEX_FORMAT = "^/?(%s)(/.+)?$";

    public static final int TARGET_ID_GROUP = 1;
    public static final int SUFFIX_GROUP = 2;

    @Override
    public String toTargetedUrl(String url) {
        Matcher matcher = matchUrl(url);
        if (matcher != null) {
            return url;
        } else {
            String targetId = targetIdManager.getCurrentTargetId();

            if (!url.startsWith("/")) {
                url = "/" + url;
            }

            return "/" + targetId + url;
        }
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        String targetedUrl = "";

        if (StringUtils.isNotEmpty(prefix)) {
            targetedUrl = UrlUtils.appendUrl(targetedUrl, prefix);
        }
        if (StringUtils.isNotEmpty(targetId)) {
            targetedUrl = UrlUtils.appendUrl(targetedUrl, targetId);
        }
        if (StringUtils.isNotEmpty(suffix)) {
            targetedUrl = UrlUtils.appendUrl(targetedUrl, suffix);
        }

        targetedUrl = StringUtils.prependIfMissing(targetedUrl, "/");

        return targetedUrl;
    }

    @Override
    protected String getPrefix(Matcher matcher) {
        return null;
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
        String availableTargetIds = StringUtils.join(targetIdManager.getAvailableTargetIds(), '|');
        String targetedUrlByFilePattern = String.format(TARGETED_URL_REGEX_FORMAT, availableTargetIds);

        return Pattern.compile(targetedUrlByFilePattern);
    }

}
