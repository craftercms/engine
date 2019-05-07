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

package org.craftercms.engine.graphql.impl.field;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.graphql.GraphQLFieldFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.databind.ObjectMapper;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLList.list;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_ITEM;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_KEY;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_SELECTED;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_VALUE;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SEPARATOR;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_ITEM;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_ITEMS;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_MULTIVALUE;
import static org.craftercms.engine.graphql.SchemaUtils.TEXT_FILTER;
import static org.craftercms.engine.graphql.SchemaUtils.setTypeFromFieldName;

/**
 * Implementation of {@link GraphQLFieldFactory} that handles checkbox-group fields
 * @author joseross
 * @since 3.1
 */
public class CheckboxGroupFieldFactory implements GraphQLFieldFactory {

    private static final Logger logger = LoggerFactory.getLogger(CheckboxGroupFieldFactory.class);

    protected String datasourceNameXPath;
    protected String datasourceSettingsXPathFormat;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Required
    public void setDatasourceNameXPath(final String datasourceNameXPath) {
        this.datasourceNameXPath = datasourceNameXPath;
    }

    @Required
    public void setDatasourceSettingsXPathFormat(String datasourceSettingsXPathFormat) {
        this.datasourceSettingsXPathFormat = datasourceSettingsXPathFormat;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void createField(final Document contentTypeDefinition, final Node contentTypeField,
                            final String contentTypeFieldId, final String parentGraphQLTypeName,
                            final GraphQLObjectType.Builder parentGraphQLType, final String graphQLFieldName,
                            final GraphQLFieldDefinition.Builder graphQLField) {
        String datasourceName = XmlUtils.selectSingleNodeValue(contentTypeField, datasourceNameXPath);
        String datasourceSettings = XmlUtils.selectSingleNodeValue(
                contentTypeDefinition, String.format(datasourceSettingsXPathFormat, datasourceName));
        String datasourceType = null;
        String datasourceSuffix = null;

        try {
            if(StringUtils.isNotEmpty(datasourceSettings)) {
                List<Map<String, Object>> typeSetting = objectMapper.readValue(datasourceSettings, List.class);
                Optional<Map<String, Object>> selectedType =
                    typeSetting.stream()
                               .filter(s -> (Boolean)s.get(FIELD_NAME_SELECTED)).findFirst();
                if (selectedType.isPresent()) {
                    datasourceType = selectedType.get().get(FIELD_NAME_VALUE).toString();
                    datasourceSuffix = StringUtils.substringAfter(datasourceType, FIELD_SEPARATOR);
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking data source type for '{}'", contentTypeFieldId);
        }

        String valueKey = FIELD_NAME_VALUE;
        if (StringUtils.isNotEmpty(datasourceSuffix)) {
            valueKey += FIELD_SEPARATOR + datasourceSuffix + FIELD_SUFFIX_MULTIVALUE;
        }

        GraphQLFieldDefinition.Builder valueField = GraphQLFieldDefinition.newFieldDefinition()
            .name(valueKey)
            .description("The value of the item");

        if (StringUtils.isNotEmpty(datasourceType)) {
            setTypeFromFieldName(datasourceType, valueField);
        } else {
            valueField.type(GraphQLString);
            valueField.argument(TEXT_FILTER);
        }

        GraphQLObjectType itemType = GraphQLObjectType.newObject()
            .name(parentGraphQLTypeName + FIELD_SEPARATOR + graphQLFieldName + FIELD_SUFFIX_ITEM)
            .description("Item for field " + contentTypeFieldId)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_KEY)
                .description("The key of the item")
                .type(GraphQLString)
                .argument(TEXT_FILTER))
            .field(valueField)
            .build();

        GraphQLObjectType itemWrapper = GraphQLObjectType.newObject()
            .name(parentGraphQLTypeName + FIELD_SEPARATOR + graphQLFieldName + FIELD_SUFFIX_ITEMS)
            .description("Wrapper for field " + contentTypeFieldId)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEM)
                .description("List of items for field " + contentTypeFieldId)
                .type(list(itemType)))
            .build();

        graphQLField.type(itemWrapper);
    }

}
