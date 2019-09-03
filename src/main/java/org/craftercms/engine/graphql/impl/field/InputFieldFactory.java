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

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.graphql.GraphQLFieldFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import static graphql.Scalars.GraphQLString;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SEPARATOR;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_TOKENIZED;
import static org.craftercms.engine.graphql.SchemaUtils.TEXT_FILTER;
import static org.craftercms.engine.graphql.SchemaUtils.setTypeFromFieldName;

/**
 * Implementation of {@link GraphQLFieldFactory} that handles input fields
 *
 * @author joseross
 * @since 3.1.2
 */
public class InputFieldFactory implements GraphQLFieldFactory {

    /**
     * The XPath selector for the tokenize property
     */
    protected String tokenizeXPath;

    public InputFieldFactory(final String tokenizeXPath) {
        this.tokenizeXPath = tokenizeXPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createField(final Document contentTypeDefinition, final Node contentTypeField,
                            final String contentTypeFieldId, final String parentGraphQLTypeName,
                            final GraphQLObjectType.Builder parentGraphQLType, final String graphQLFieldName,
                            final GraphQLFieldDefinition.Builder graphQLField) {
        if (Boolean.parseBoolean(XmlUtils.selectSingleNodeValue(contentTypeField, tokenizeXPath))) {
            // Add the tokenized field as string with text filters
            parentGraphQLType.field(GraphQLFieldDefinition.newFieldDefinition()
                .name(StringUtils.substringBeforeLast(graphQLFieldName, FIELD_SEPARATOR) + FIELD_SUFFIX_TOKENIZED)
                .description("Tokenized version of " + contentTypeFieldId)
                .type(GraphQLString)
                .argument(TEXT_FILTER));
        }

        // Add the original according to the postfix
        setTypeFromFieldName(contentTypeFieldId, graphQLField);
    }

}
