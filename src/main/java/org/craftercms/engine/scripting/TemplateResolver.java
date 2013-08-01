package org.craftercms.engine.scripting;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * Resolves the template to use for a certain script URL, HTTP method, format and status. If the status object doesn't indicate a
 * redirect, the template at {scriptBaseUrl}.{method}.{format}.ftl will be used. If not, it will search for the following templates
 * in order:
 *
 * <ol>
 *     <li>{scriptBaseUrl}.{method}.{format}.{status}.ftl</li>
 *     <li>{scriptBaseUrl}.{method}.{format}.status.ftl</li>
 *     <li>{method}.{format}.{status}.ftl</li>
 *     <li>{method}.{format}.status.ftl</li>
 *     <li>{format}.{status}.ftl</li>
 *     <li>{format}.status.ftl</li>
 * </ol>
 *
 * @author Alfonso Vásquez
 */
public class TemplateResolver {

    private static final String TEMPLATE_URL_FORMAT = "%s.%s.%s.ftl";
    private static final String[] STATUS_TEMPLATE_URL_FORMATS = {
            "%1$s.%2$s.%3$s.%4$d.ftl",
            "%1$s.%2$s.%3$s.status.ftl",
            "%2$s.%3$s.%4$d.ftl",
            "%2$s.%3$s.status.ftl",
            "%3$s.%4$d.ftl",
            "%3$s.status.ftl"
    };

    protected Configuration configuration;

    public TemplateResolver(Configuration configuration) {
        this.configuration = configuration;
    }

    public Template resolveTemplate(String scriptBaseUrl, String method, String format, Status status, Locale locale) throws IOException {
        if (status.isRedirect()) {
            return resolveStatusTemplate(scriptBaseUrl, method, format, status, locale);
        } else {
            String templateUrl = String.format(TEMPLATE_URL_FORMAT, scriptBaseUrl, method, format);

            return getTemplate(templateUrl, locale);
        }
    }

    public Template resolveStatusTemplate(String scriptBaseUrl, String method, String format, Status status, Locale locale)
            throws IOException {
        for (String templateUrlFormat : STATUS_TEMPLATE_URL_FORMATS) {
            String templateUrl = String.format(templateUrlFormat, scriptBaseUrl, method, format, status);
            Template template = getTemplate(templateUrl, locale);

            if (template != null) {
                return template;
            }
        }

        return null;
    }

    protected Template getTemplate(String url, Locale locale) throws IOException {
        try {
            return configuration.getTemplate(url, locale);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}
