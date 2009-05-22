/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.SerializationMethod;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class WebPageOutputOptionsTemplates {
    
    public static WebPageOutputOptions createWebPageOptions(WebPageType webPageType) {
        ConstraintUtilities.ensureNotNull(webPageType, "webPageType");
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setWebPageType(webPageType);
        switch (webPageType) {
            case MOZILLA:
                options.setSerializationMethod(SerializationMethod.XHTML);
                options.setContentType("application/xhtml+xml");
                break;
                
            case CROSS_BROWSER_XHTML:
                options.setSerializationMethod(SerializationMethod.XHTML);
                options.setContentType("application/xhtml+xml");
                break;
                
            case MATHPLAYER_HTML:
                options.setSerializationMethod(SerializationMethod.HTML);
                options.setContentType("text/html");
                options.setPrefixingMathML(true);
                break;
                
            case PROCESSED_HTML:
                options.setSerializationMethod(SerializationMethod.HTML);
                options.setContentType("text/html");
                break;
                
            case CLIENT_SIDE_XSLT_STYLESHEET:
                options.setSerializationMethod(SerializationMethod.XML);
                options.setContentType("application/xhtml+xml");
                break;
                
            case UNIVERSAL_STYLESHEET:
                options.setSerializationMethod(SerializationMethod.XML);
                options.setContentType("application/xhtml+xml");
                break;
                
            default:
                throw new SnuggleRuntimeException("Unexpected switch case " + webPageType);
                
        }
        return options;
    }

}
