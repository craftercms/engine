package org.craftercms.engine.targeting;

import java.util.List;

/**
 * Created by alfonsovasquez on 14/8/15.
 */
public interface CandidateTargetedUrlsResolver {

    List<String> getUrls(String targetedUrl);

}
