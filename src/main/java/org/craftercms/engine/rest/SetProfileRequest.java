/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.rest;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the necessary parameters for a set profile request.
 */
public class SetProfileRequest {
    private final Map<String, Object> parameters;

    public SetProfileRequest() {
        parameters = new HashMap<>();
    }

    @JsonAnySetter
    public void addParameter(final String key, final Object value) {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
