/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
