/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.mobile;

public class UserAgent {

	private String queryStringParamValue;
	private String detectionRegex;
	private String templatePrefix;
	
	public UserAgent() { }
	
	public String getDetectionRegex() {
		return detectionRegex;
	}
	public void setDetectionRegex(String detectionRegex) {
		this.detectionRegex = detectionRegex;
	}
	public String getTemplatePrefix() {
		return templatePrefix;
	}
	public void setTemplatePrefix(String templatePrefix) {
		this.templatePrefix = templatePrefix;
	}
	
	public String getQueryStringParamValue() {
		return this.queryStringParamValue;
	}
	
	public void setQueryStringParamValue(String queryStringParamValue) {
		this.queryStringParamValue = queryStringParamValue;
	}

}
