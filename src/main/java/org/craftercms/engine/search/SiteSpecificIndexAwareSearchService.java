package org.craftercms.engine.search;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.search.exception.SearchException;
import org.craftercms.search.service.Query;
import org.craftercms.search.service.SearchService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * {@link SearchService} that makes sure all calls are done against a site-specific index ID, of the format
 * {@code ${currentSiteName}-.*}. If no index is specified in a method call, {@code ${currentSiteName}-default}
 * is used. If the index is specified, if it doesn't match {@code ${currentSiteName}-.*}, an
 * {@code IllegalArgumentException} is thrown.
 *
 * @author avasquez
 */
public class SiteSpecificIndexAwareSearchService implements SearchService {

    public static final String DEFAULT_DEFAULT_INDEX_ID_SUFFIX = "default";
    public static final String DEFAULT_INDEX_ID_FORMAT = "%s-%s";
    public static final String DEFAULT_MULTI_VALUE_SEPARATOR = ",";
    public static final String DEFAULT_MULTI_VALUE_IGNORE_PATTERN = "^.+_(html|i|s|l|t|b|f|d|dt)$";

    protected SearchService actualSearchService;
    protected String defaultIndexIdSuffix;
    protected String indexIdFormat;
    protected String multiValueSeparator;
    protected String multiValueIgnorePattern;

    public SiteSpecificIndexAwareSearchService() {
        defaultIndexIdSuffix = DEFAULT_DEFAULT_INDEX_ID_SUFFIX;
        indexIdFormat = DEFAULT_INDEX_ID_FORMAT;
        multiValueSeparator = DEFAULT_MULTI_VALUE_SEPARATOR;
        multiValueIgnorePattern = DEFAULT_MULTI_VALUE_IGNORE_PATTERN;
    }

    @Required
    public void setActualSearchService(SearchService actualSearchService) {
        this.actualSearchService = actualSearchService;
    }

    public void setDefaultIndexIdSuffix(String defaultIndexIdSuffix) {
        this.defaultIndexIdSuffix = defaultIndexIdSuffix;
    }

    public void setIndexIdFormat(String indexIdFormat) {
        this.indexIdFormat = indexIdFormat;
    }

    public void setMultiValueIgnorePattern(String multiValueIgnorePattern) {
        this.multiValueIgnorePattern = multiValueIgnorePattern;
    }

    public void setMultiValueSeparator(String multiValueSeparator) {
        this.multiValueSeparator = multiValueSeparator;
    }

    @Override
    public Map<String, Object> search(Query query) throws SearchException {
        return actualSearchService.search(verifyIndexId(null), query);
    }

    @Override
    public Map<String, Object> search(String indexId, Query query) throws SearchException {
        return actualSearchService.search(verifyIndexId(indexId), query);
    }

    @Override
    public String update(String site, String id, String xml, boolean ignoreRootInFieldNames) throws SearchException {
        return actualSearchService.update(verifyIndexId(null), site, id, xml, ignoreRootInFieldNames);
    }

    @Override
    public String update(String indexId, String site, String id, String xml,
                         boolean ignoreRootInFieldNames) throws SearchException {
        return actualSearchService.update(verifyIndexId(indexId), site, id, xml, ignoreRootInFieldNames);
    }

    @Override
    public String delete(String site, String id) throws SearchException {
        return actualSearchService.delete(verifyIndexId(null), site, id);
    }

    @Override
    public String delete(String indexId, String site, String id) throws SearchException {
        return actualSearchService.delete(verifyIndexId(indexId), site, id);
    }

    @Override
    @Deprecated
    public String updateDocument(String site, String id, File document) throws SearchException {
        return updateFile(site, id, document);
    }

    @Override
    @Deprecated
    public String updateDocument(String site, String id, File document,
                                 Map<String, String> additionalFields) throws SearchException {
        return updateFile(site, id, document, getAdditionalFieldMapAsMultiValueMap(additionalFields));
    }

    @Override
    public String updateFile(String site, String id, File file) throws SearchException {
        return actualSearchService.updateFile(verifyIndexId(null), site, id, file);
    }

    @Override
    public String updateFile(String indexId, String site, String id, File file) throws SearchException {
        return actualSearchService.updateFile(verifyIndexId(indexId), site, id, file);
    }

    @Override
    public String updateFile(String site, String id, File file,
                             Map<String, List<String>> additionalFields) throws SearchException {
        return actualSearchService.updateFile(verifyIndexId(null), site, id, file, additionalFields);
    }

    @Override
    public String updateFile(String indexId, String site, String id, File file,
                             Map<String, List<String>> additionalFields) throws SearchException {
        return actualSearchService.updateFile(verifyIndexId(indexId), site, id, file);
    }

    @Override
    public String commit() throws SearchException {
        return actualSearchService.commit(verifyIndexId(null));
    }

    @Override
    public String commit(String indexId) throws SearchException {
        return actualSearchService.commit(verifyIndexId(indexId));
    }

    protected String verifyIndexId(String indexId) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("No current site context found");
        }

        if (StringUtils.isEmpty(indexId)) {
            indexId = String.format(indexIdFormat, siteContext.getSiteName(), defaultIndexIdSuffix);
        } else {
            String indexIdRegex = String.format(indexIdFormat, siteContext.getSiteName(), ".*");
            if (!indexId.matches(indexIdRegex)) {
                throw new IllegalArgumentException("Specified index ID should match " + indexIdRegex);
            }
        }

        return indexId;
    }

    protected Map<String, List<String>> getAdditionalFieldMapAsMultiValueMap(Map<String, String> originalMap) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(originalMap.size());
        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (!fieldName.matches(multiValueIgnorePattern)) {
                multiValueMap.put(fieldName, Arrays.asList(fieldValue.split(multiValueSeparator)));
            } else {
                multiValueMap.add(fieldName, fieldValue);
            }
        }

        return multiValueMap;
    }

}
