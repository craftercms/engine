package org.craftercms.engine.targeting.impl;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CandidateTargetIdsResolverImpl}.
 *
 * @author avasquez
 */
public class CandidateTargetIdsResolverImplTest {

    private CandidateTargetIdsResolverImpl candidateTargetIdsResolver;

    @Before
    public void setUp() throws Exception {
        candidateTargetIdsResolver = new CandidateTargetIdsResolverImpl();
    }

    @Test
    public void testGetTargetIds() throws Exception {
        List<String> targetIds = candidateTargetIdsResolver.getTargetIds("ja_jp_jp", "en");

        assertNotNull(targetIds);
        assertEquals(4, targetIds.size());
        assertEquals("ja_jp_jp", targetIds.get(0));
        assertEquals("ja_jp", targetIds.get(1));
        assertEquals("ja", targetIds.get(2));
        assertEquals("en", targetIds.get(3));
    }

}
