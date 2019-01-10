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
///**
// *
// */
//package org.craftercms.engine.mobile;
//
//import junit.framework.Assert;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//
///**
// * @author mverkaik
// *
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:crafter/engine/services/main-services-context.xml"})
//public class UserAgentTemplateDetectorTest {
//
//	private final String genericTemplate =  "/foo/bar.ftl";
//	private final String iPhoneTemplate = "/foo/iphone_bar.ftl";
//	private final String iPadTemplate = "/foo/ipad_bar.ftl";
//
//	private final String iPhoneParam = "iphone";
//	private final String iPadParam = "ipad";
//
//	private final String iPhoneAgentHeader = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7";
//	private final String iPadAgentHeader = "Mozilla/5.0 (iPad; U; CPU OS OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B367 Safari/531.21.10";
//
//	@Autowired
//    private ApplicationContext applicationContext;
//
//	private UserAgentTemplateDetector getUserAgentTemplateDetector() {
//		return applicationContext.getBean(UserAgentTemplateDetector.class);
//	}
//
//	@Test
//	public void testResolveIphoneTemplateByParam() {
//		MockHttpServletRequest request = mockRequestWithAgentParam(iPhoneParam);
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(iPhoneTemplate, resolvedTemplate);
//	}
//
//	@Test
//	public void testResolveIphoneTemplateByHeader() {
//		MockHttpServletRequest request = mockRequestWithAgentHeader(iPhoneAgentHeader);
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(iPhoneTemplate, resolvedTemplate);
//	}
//
//	@Test
//	public void testResolveIpadTemplateByParam() {
//		MockHttpServletRequest request = mockRequestWithAgentParam(iPadParam);
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(iPadTemplate, resolvedTemplate);
//	}
//
//	@Test
//	public void testResolveIpadTemplateByHeader() {
//		MockHttpServletRequest request = mockRequestWithAgentHeader(iPadAgentHeader);
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(iPadTemplate, resolvedTemplate);
//	}
//
//	@Test
//	public void testResolveIphoneTemplateByStandardHeader() {
//		MockHttpServletRequest request = mockRequestWithStandardAgentHeader(iPhoneAgentHeader);
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(iPhoneTemplate, resolvedTemplate);
//	}
//
//	@Test
//	public void testResolveIpadTemplateByStandardHeader() {
//		MockHttpServletRequest request = mockRequestWithStandardAgentHeader(iPadAgentHeader);
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(iPadTemplate, resolvedTemplate);
//	}
//
//	@Test
//	public void testResolveUnrecognizedRequest() {
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		String resolvedTemplate = getUserAgentTemplateDetector()
//									.resolveAgentTemplate(request, genericTemplate);
//		Assert.assertEquals(genericTemplate, resolvedTemplate);
//	}
//
//	private MockHttpServletRequest mockRequestWithAgentParam(String userAgent) {
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		request.addParameter(getUserAgentTemplateDetector().getAgentQueryStringParamName(), userAgent);
//		return request;
//	}
//
//	private MockHttpServletRequest mockRequestWithAgentHeader(String userAgent) {
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		request.addHeader(getUserAgentTemplateDetector().getAgentHeaderName(), userAgent);
//		return request;
//	}
//
//	private MockHttpServletRequest mockRequestWithStandardAgentHeader(String userAgent) {
//		MockHttpServletRequest request = new MockHttpServletRequest();
//		request.addHeader("User-Agent", userAgent);
//		return request;
//	}
//}
