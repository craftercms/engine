package org.craftercms.engine.targeting.impl;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by alfonsovasquez on 17/8/15.
 */
public class CandidateTargetIdsResolverImplTest {

    private CandidateTargetIdsResolverImpl candidateTargetIdsResolver;

    @Before
    public void setUp() throws Exception {
        candidateTargetIdsResolver = new CandidateTargetIdsResolverImpl();
    }

    @Test
    public void testGetTargetIds() throws Exception {
        List<String> targetIds = candidateTargetIdsResolver.getTargetIds("ja_JP_JP", "en");

        assertNotNull(targetIds);
        assertEquals(4, targetIds.size());
        assertEquals("ja_JP_JP", targetIds.get(0));
        assertEquals("ja_JP", targetIds.get(1));
        assertEquals("ja", targetIds.get(2));
        assertEquals("en", targetIds.get(3));
    }

}
