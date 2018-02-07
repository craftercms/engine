package org.craftercms.engine.util;

import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.service.context.SiteContext;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

public class ContentModelUtils {

    public static final String FIELD_CONVERTER_BEAN_NAME = "crafter.contentModelFieldConverter";

    private ContentModelUtils() {

    }

    @SuppressWarnings("unchecked")
    public static final Object convertField(Element field) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            ApplicationContext appContext = siteContext.getGlobalApplicationContext();
            if (appContext != null) {
                Converter<Element, Object> converter = appContext.getBean(FIELD_CONVERTER_BEAN_NAME, Converter.class);
                if (converter != null) {
                    return converter.convert(field);
                }
            }
        }

        return field;
    }

}
