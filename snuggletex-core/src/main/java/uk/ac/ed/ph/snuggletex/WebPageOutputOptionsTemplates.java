/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;

/**
 * Utility class that creates pre-configured and usable {@link WebPageOutputOptions} instances
 * for the various types of {@link WebPageType}s supported by SnuggleTeX.
 * 
 * @deprecated As of SnuggleTeX 1.3.0, use {@link WebPageOutputOptionsBuilder}. This class
 *   will be removed in SnuggleTeX 1.4.
 *
 * @author  David McKain
 * @version $Revision$
 */
@Deprecated
public final class WebPageOutputOptionsTemplates {
    
    @Deprecated
    public static final String DEFAULT_CONTENT_TYPE = WebPageOutputOptions.DEFAULT_CONTENT_TYPE;
    
    @Deprecated
    public static final String DEFAULT_LANG = WebPageOutputOptions.DEFAULT_LANG;
    
    /**
     * Creates a {@link WebPageOutputOptions} suitable for the given {@link WebPageType} that
     * is pre-configured to work as described in the {@link WebPageType}.
     * <p>
     * In particular, the following properties are set to appropriate values:
     * <ul>
     *   <li>{@link WebPageOutputOptions#setContentType(String)}</li>
     *   <li>{@link WebPageOutputOptions#setPrefixingMathML(boolean)}</li>
     *   <li>{@link XMLStringOutputOptions#setSerializationMethod(SerializationMethod)}</li>
     *   <li>{@link XMLStringOutputOptions#setDoctypePublic(String)}</li>
     *   <li>{@link XMLStringOutputOptions#setDoctypeSystem(String)}</li>
     *   <li>{@link XMLStringOutputOptions#setIncludingXMLDeclaration(boolean)}</li>
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
     * 
     * @deprecated This was deprecated in SnuggleTeX 1.3.0 and will be removed in 1.4.
     *   Use {@link WebPageOutputOptionsBuilder} instead.
     */
    @SuppressWarnings("javadoc")
    @Deprecated
    public static WebPageOutputOptions createWebPageOptions(WebPageType webPageType) {
        ConstraintUtilities.ensureNotNull(webPageType, "webPageType");
        WebPageOutputOptions options = new WebPageOutputOptions();
        switch (webPageType) {
            case MOZILLA:
                options.setSerializationMethod(SerializationMethod.XHTML);
                options.setContentType("application/xhtml+xml");
                break;
                
            case CROSS_BROWSER_XHTML:
                /* NB: There must be no namespace prefix for MathML or XHTML here, otherwise
                 * it won't validate against the DTD. We don't enforce this here.
                 */
                options.setSerializationMethod(SerializationMethod.XHTML);
                options.setIncludingXMLDeclaration(true);
                options.setDoctypePublic(W3CConstants.XHTML_11_MATHML_20_PUBLIC_IDENTIFIER);
                options.setDoctypeSystem(W3CConstants.XHTML_11_MATHML_20_SYSTEM_IDENTIFIER);
                options.setNoCharsetInContentTypeHeader(true);
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
                
            case UNIVERSAL_STYLESHEET:
                options.setSerializationMethod(SerializationMethod.XML);
                options.setIncludingXMLDeclaration(true);
                break;
                
            case CLIENT_SIDE_XSLT_STYLESHEET:
                options.setSerializationMethod(SerializationMethod.XML);
                break;
                
            default:
                throw new SnuggleRuntimeException("Unexpected switch case " + webPageType);
                
        }
        return options;
    }
}
