/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import javax.servlet.http.HttpServletRequest;

/**
 * Some general servlet-related utility methods.
 * <p>
 * (This is a cut down version of the class of the same name in the CST Project)
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class WebUtilities {
    
    public static final String WITHIN_CONTEXT_REQUEST_URL_ATTRIBUTE_NAME = "uk.ac.ed.ph.snuggletex.webapp.WithinContextRequestUrl";
    
    /**
     * Returns the URL for the given request, starting from AFTER the context path and
     * including path info and query parameters.
     * <p>
     * The result is stored in the {@link HttpServletRequest} as an Attribute for later
     * retrieval so as to avoid needed recalculation.
     * 
     * @param request
     */
    public static String getWithinContextRequestUrl(HttpServletRequest request) {
        String result = (String) request.getAttribute(WITHIN_CONTEXT_REQUEST_URL_ATTRIBUTE_NAME);
        if (result==null) {
            StringBuilder builder = new StringBuilder(request.getServletPath());
            if (request.getPathInfo()!=null) {
                builder.append(request.getPathInfo());
            }
            if (request.getQueryString()!=null) {
                builder.append("?").append(request.getQueryString());
            }
            result = builder.toString();
            request.setAttribute(WITHIN_CONTEXT_REQUEST_URL_ATTRIBUTE_NAME, result);
        }
        return result;
    }
}
