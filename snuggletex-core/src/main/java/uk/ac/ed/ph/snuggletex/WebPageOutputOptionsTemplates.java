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
 * Utility class that creates pre-configured and usable {@link WebPageOutputOptions} instances
 * for the various types of {@link WebPageType}s supported by SnuggleTeX.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class WebPageOutputOptionsTemplates {
    
    public static final String DEFAULT_CONTENT_TYPE = "application/xhtml+xml";
    public static final String DEFAULT_LANG = "en";
    
    /**
     * Creates a {@link WebPageOutputOptions} suitable for the given {@link WebPageType} that
     * is pre-configured to work as described in the {@link WebPageType}.
     * <p>
     * In particular, the following properties are set to appropriate values:
     * <ul>
     *   <li>{@link WebPageOutputOptions#setContentType(String)}</li>
     *   <li>{@link WebPageOutputOptions#setSerializationMethod(SerializationMethod)}</li>
     *   <li>{@link WebPageOutputOptions#setPrefixingMathML(boolean)}</li>
     * </ul>
     * You may set other properties (or override these ones!) afterwards as required.
     * 
     * <h2>JEuclid Notes</h2>
     * 
     * If you are using the JEuclid extension for converting MathML to images, then you should
     * use the {@link uk.ac.ed.ph.snuggletex.jeuclid.JEuclidUtilities} helper class to create
     * appropriate {@link WebPageOutputOptions}.
     *   
     * @param webPageType type of web page you want
     */
    public static WebPageOutputOptions createWebPageOptions(WebPageType webPageType) {
        ConstraintUtilities.ensureNotNull(webPageType, "webPageType");
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setWebPageType(webPageType);
        switch (webPageType) {
            case MOZILLA:
                options.setSerializationMethod(SerializationMethod.XHTML);
                break;
                
            case CROSS_BROWSER_XHTML:
                options.setSerializationMethod(SerializationMethod.XHTML);
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
                break;
                
            case UNIVERSAL_STYLESHEET:
                options.setSerializationMethod(SerializationMethod.XML);
                break;
                
            default:
                throw new SnuggleRuntimeException("Unexpected switch case " + webPageType);
                
        }
        return options;
    }
}
