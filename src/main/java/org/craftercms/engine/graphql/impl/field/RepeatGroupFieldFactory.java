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

import java.util.Collections;
import java.util.List;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.apache.commons.collections.CollectionUtils;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.graphql.GraphQLFieldFactory;
import org.craftercms.engine.graphql.GraphQLTypeFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Required;

import static graphql.schema.GraphQLList.list;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_ITEM;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SEPARATOR;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_ITEM;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_ITEMS;

/**
 * Implementation of {@link GraphQLFieldFactory} that handles repeating-group fields
 * @author joseross
 * @since 3.1
 */
public class RepeatGroupFieldFactory implements GraphQLFieldFactory {

    protected String fieldsXPath;

    protected GraphQLTypeFactory typeFactory;

    @Required
    public void setFieldsXPath(final String fieldsXPath) {
        this.fieldsXPath = fieldsXPath;
    }

    @Required
    public void setTypeFactory(final GraphQLTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public void createField(final Document contentTypeDefinition, final Node contentTypeField,
                            final String contentTypeFieldId, final String parentGraphQLTypeName,
                            final GraphQLObjectType.Builder parentGraphQLType, final String graphQLFieldName,
                            final GraphQLFieldDefinition.Builder graphQLField) {
        // For Repeating Groups we need to create a wrapper type and do everything all over
        GraphQLObjectType.Builder repeatType = GraphQLObjectType.newObject()
            .name(parentGraphQLTypeName + FIELD_SEPARATOR + graphQLFieldName + FIELD_SUFFIX_ITEM)
            .description("Item for repeat group of " + contentTypeFieldId);

        List<Node> fields =
            XmlUtils.selectNodes(contentTypeField, fieldsXPath, Collections.emptyMap());

        // Call recursively for all fields in the repeating group
        if (CollectionUtils.isNotEmpty(fields)) {
            fields.forEach(f -> typeFactory.createField(contentTypeDefinition, f, parentGraphQLTypeName, repeatType));
        }

        GraphQLObjectType wrapperType = GraphQLObjectType.newObject()
            .name(parentGraphQLTypeName + FIELD_SEPARATOR + graphQLFieldName + FIELD_SUFFIX_ITEMS)
            .description("Wrapper for list of items of " + contentTypeFieldId)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEM)
                .description("List of items of " + contentTypeFieldId)
                .type(list(repeatType.build())))
            .build();

        graphQLField.type(wrapperType);
    }

}
