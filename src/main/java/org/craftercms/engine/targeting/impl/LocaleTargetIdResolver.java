package org.craftercms.engine.targeting.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.engine.util.config.TargetingProperties;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public class LocaleTargetIdResolver extends AbstractTargetIdResolver {

    @Override
    public String getCurrentTargetId() throws IllegalStateException {
        Locale currentLocale = LocaleContextHolder.getLocale();
        if (currentLocale != null) {
            return currentLocale.toString();
        } else {
            throw new IllegalStateException("No locale associated with the current thread");
        }
    }

    @Override
    public List<String> getAvailableTargetIds() {
        String[] availableTargetIds = TargetingProperties.getAvailableTargetIds();
        if (ArrayUtils.isEmpty(availableTargetIds)) {
            List<Locale> availableLocales = LocaleUtils.availableLocaleList();
            List<String> availableLocaleStrs = new ArrayList<>(availableLocales.size());

            for (Locale locale : availableLocales) {
                availableLocaleStrs.add(locale.toString());
            }

            return availableLocaleStrs;
        } else {
            return Arrays.asList(availableTargetIds);
        }
    }

}
