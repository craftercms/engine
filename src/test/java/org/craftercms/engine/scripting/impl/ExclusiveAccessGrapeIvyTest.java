/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.scripting.impl;

import groovy.grape.Grape;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public class ExclusiveAccessGrapeIvyTest {

	@Test
	public void testExclusiveGrabRegister() {
		ExclusiveAccessGrapeIvy exclusiveGrapeIvy = new ExclusiveAccessGrapeIvy();
		try {
			exclusiveGrapeIvy.register();
			assertInstanceOf(ExclusiveAccessGrapeIvy.class, Grape.getInstance());
		} catch (NoSuchFieldException | IllegalAccessException e) {
			// If this fails, it means that Groovy has changed the internal structure of the Grape class and the
			// register method needs to be updated to reflect those changes.
			fail("Register method threw an exception: " + e.getMessage());
		} finally {
			// Reset Grape instance to avoid side effects on other tests
			try {
				Field instanceField = Grape.class.getDeclaredField("instance");
				instanceField.setAccessible(true);
				instanceField.set(null, null);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				// Ignore
			}
		}
	}
}
