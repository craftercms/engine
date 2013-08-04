/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.engine.scripting.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.craftercms.engine.exception.ScriptRenderingException;
import org.craftercms.engine.exception.UnrecognizableMimeTypeException;
import org.craftercms.engine.exception.UnrecognizableMimeTypeException;
import org.craftercms.engine.scripting.ScriptView;
import org.craftercms.engine.scripting.ScriptViewResolver;
import org.craftercms.engine.scripting.Status;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the view to use for a certain script base URL, HTTP method, status and acceptable mime types. If  {@code status.redirect ==
 * false}, the view at {viewName}.{method}.{format}.{viewExt} will be used (where format is the file extension of any of the mime types).
 * Otherwise, it will search for the following views in order:
 *
 * <ol>
 *     <li>{viewName}.{method}.{format}.{status}.{viewExt}</li>
 *     <li>{viewName}.{method}.{format}.status.{viewExt}</li>
 *     <li>{method}.{format}.{status}.{viewExt}</li>
 *     <li>{method}.{format}.status.{viewExt}</li>
 *     <li>{format}.{status}.{viewExt}</li>
 *     <li>{format}.status.{viewExt}</li>
 * </ol>
 *
 * @author Alfonso Vásquez
 */
public abstract class AbstractScriptViewResolver implements ScriptViewResolver {

    private static final String VIEW_URL_FORMAT = "%s.%s.%s.%s";
    private static final String[] STATUS_VIEW_URL_FORMATS = {
            "%1$s.%2$s.%3$s.%4$d.%5$s",
            "%1$s.%2$s.%3$s.status.%5$s",
            "%2$s.%3$s.%4$d.%5$s",
            "%2$s.%3$s.status.%5$s",
            "%3$s.%4$d.%5$s",
            "%3$s.status.%5$s"
    };

    protected String viewFileExtension;

    protected AbstractScriptViewResolver(String viewFileExtension) {
        this.viewFileExtension = viewFileExtension;
    }

    @Override
    public ScriptView resolveView(String viewName, String method, List<MediaType> acceptableMimeTypes, Status status, Locale locale)
            throws UnrecognizableMimeTypeException, ScriptRenderingException {
        if (status.isRedirect()) {
            return resolveStatusView(viewName, method, acceptableMimeTypes, status.getCode(), locale);
        } else {
            return resolveDefaultView(viewName, method, acceptableMimeTypes, locale);
        }
    }

    protected ScriptView resolveDefaultView(String viewName, String method, List<MediaType> acceptableMimeTypes, Locale locale)
            throws UnrecognizableMimeTypeException, ScriptRenderingException {
        for (MediaType mimeType : acceptableMimeTypes) {
            String format = getFormat(mimeType.toString());
            String viewUrl = String.format(VIEW_URL_FORMAT, viewName, method, format, viewFileExtension);

            ScriptView view = getView(viewUrl, mimeType.toString(), locale);
            if (view != null) {
                return view;
            }
        }

        return null;
    }

    protected ScriptView resolveStatusView(String viewName, String method, List<MediaType> acceptableMimeTypes, int statusCode,
                                           Locale locale) throws UnrecognizableMimeTypeException, ScriptRenderingException {
        for (String viewUrlFormat : STATUS_VIEW_URL_FORMATS) {
            for (MediaType mimeType : acceptableMimeTypes) {
                String format = getFormat(mimeType.toString());
                String viewUrl = String.format(viewUrlFormat, viewName, method, format, statusCode, viewFileExtension);

                ScriptView view = getView(viewUrl, mimeType.toString(), locale);
                if (view != null) {
                    return view;
                }
            }
        }

        return null;
    }

    protected String getFormat(String mimeType) throws UnrecognizableMimeTypeException {
        try {
            String format = MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
            if (StringUtils.isNotEmpty(format)) {
                return StringUtils.stripStart(format, ".");
            } else {
                throw new UnrecognizableMimeTypeException("Unable to get format for mime type '" + mimeType + "'");
            }
        } catch (MimeTypeException e) {
            throw new UnrecognizableMimeTypeException("Unable to get mime type for name '" + mimeType + "'", e);
        }
    }

    protected abstract ScriptView getView(String url, String mimeType, Locale locale) throws ScriptRenderingException;

}
