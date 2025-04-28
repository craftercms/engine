/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.util.http;

/**
 * Enum representing the SameSite attribute for cookies.
 * <p>
 * This enum defines the possible values for the SameSite attribute, which is used to control
 * whether cookies are sent with cross-site requests. The values are:
 * <ul>
 *     <li>Strict: Cookies are only sent in a first-party context.</li>
 *     <li>Lax: Cookies are sent in a first-party context and with top-level navigation.</li>
 *     <li>None: Cookies are sent in all contexts, including cross-origin requests.</li>
 * </ul>
 */
public enum SameSite {
	STRICT("Strict"),
	LAX("Lax"),
	NONE("None");

	private final String value;

	SameSite(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SameSite fromValue(String value) {
		for (SameSite s : values()) {
			if (s.value.equalsIgnoreCase(value)) {
				return s;
			}
		}
		throw new IllegalArgumentException("Invalid SameSite value: " + value);
	}
}

