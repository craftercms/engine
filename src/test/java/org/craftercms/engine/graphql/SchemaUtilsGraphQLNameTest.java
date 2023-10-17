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
package org.craftercms.engine.graphql;

import org.junit.jupiter.api.Test;

import static org.craftercms.engine.graphql.SchemaUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaUtilsGraphQLNameTest {

    @Test
    public void getComponentTypeNameTest() {
        checkGraphQLContentTypeConversion("component_header", "/component/header");
    }

    @Test
    public void getPageTypeNameTest() {
        checkGraphQLContentTypeConversion("page_article", "/page/article");
    }

    @Test
    public void getDashInTypeNameTest() {
        checkGraphQLContentTypeConversion("page_news__article", "/page/news-article");
    }

    @Test
    public void getMultipleLevelsTypeNameTest() {
        checkGraphQLContentTypeConversion("page_articles___news", "/page/articles/news");
    }

    @Test
    public void getTaxonomyTypeNameTest() {
        checkGraphQLContentTypeConversion("taxonomy___category", "/taxonomy/category");
    }

    @Test
    public void getCustomContentTypeNameTest() {
        checkGraphQLContentTypeConversion("content_left__toolbar", "/content/left-toolbar");
    }

    @Test
    public void getMultiLevelCustomContentTypeNameTest() {
        checkGraphQLContentTypeConversion("content_widget___left__toolbar", "/content/widget/left-toolbar");
    }

    @Test
    public void fieldNameUnderscoreTest() {
        checkGraphQLConversion("title_s", "title_s");
    }

    @Test
    public void fieldNameSingleWordTest() {
        checkGraphQLConversion("title", "title");
    }

    @Test
    public void fieldNameDashTest() {
        checkGraphQLConversion("title__alt", "title-alt");
    }

    public void checkGraphQLConversion(String expected, String original) {
        assertEquals(expected, getGraphQLName(original), "GraphQL name is not correct");
        assertEquals(original, getOriginalName(expected), "Original name is not correct");
    }

    public void checkGraphQLContentTypeConversion(String expected, String original) {
        assertEquals(expected, getGraphQLName(original), "GraphQL name is not correct");
        assertEquals(original, getContentTypeOriginalName(expected), "Original name is not correct");
    }
}
