package org.craftercms.engine.targeting;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public interface TargetedUrlStrategy {

    String toTargetedUrl(String url);

    TargetedUrlComponents parseTargetedUrl(String targetedUrl);

    String buildTargetedUrl(String prefix, String targetId, String suffix);

}
