/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;

/**
 * Utility class that creates pre-configured and usable {@link WebPageOutputOptions} instances
 * for various common scenarios.
 * 
 * This replaces {@link WebPageOutputOptionsTemplates}, which is now deprecated and will be removed
 * in SnuggleTeX 1.4
 * 
 * @since 1.3.0
 *
 * @author  David McKain
 * @version $Revision$
 */
@SuppressWarnings("javadoc")
public final class WebPageOutputOptionsBuilder {
    
    /**
     * Creates {@link WebPageOutputOptions} suitable for creating an XHTML + MathML web page,
     * using MathJax for cross-browser rendering of the MathML.
     * <p>
     * This is one of the most effective web page outputs.
     * 
     * @see #createHTML5MathJaxOptions()
     */
    public static final WebPageOutputOptions createXHTMLMathJaxOptions() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setSerializationMethod(SerializationMethod.XHTML);
        options.setIncludingXMLDeclaration(false);
        options.setMathJax(true);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} suitable for creating an XHTML + MathML web page,
     * using MathJax for cross-browser rendering of the MathML.
     * <p>
     * This is one of the most effective web page outputs.
     * 
     * @see #createHTML5MathJaxOptions()
     * 
     * @param customMathJaxUrl custom MathJax URL, which can be used to point to a local
     *   installation of MathJax or pass custom configuration details to MathJax.
     */
    public static final WebPageOutputOptions createXHTMLMathJaxOptions(String customMathJaxUrl) {
        WebPageOutputOptions options = createXHTMLMathJaxOptions();
        options.setCustomMathJaxUrl(customMathJaxUrl);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} suitable for creating an HTML 5 web page with
     * embedded MathML, using MathJax for cross-browser rendering of the MathML.
     * <p>
     * This is one of the most effective web page outputs.
     * 
     * @see #createXHTMLMathJaxOptions()
     */
    public static final WebPageOutputOptions createHTML5MathJaxOptions() {
        WebPageOutputOptions options = createHTML5Options();
        options.setMathJax(true);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} suitable for creating an HTML 5 web page with
     * embedded MathML, using MathJax for cross-browser rendering of the MathML.
     * <p>
     * This is one of the most effective web page outputs.
     * 
     * @see #createXHTMLMathJaxOptions()
     * 
     * @param customMathJaxUrl custom MathJax URL, which can be used to point to a local
     *   installation of MathJax or pass custom configuration details to MathJax.
     */
    public static final WebPageOutputOptions createHTML5MathJaxOptions(String customMathJaxUrl) {
        WebPageOutputOptions options = createHTML5MathJaxOptions();
        options.setCustomMathJaxUrl(customMathJaxUrl);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} suitable for deploying solely in a Mozilla-based
     * browser, such as Firefox, using its native support for MathML.
     * <p>
     * This is a niche output kept around for legacy purposes. You should look at some of the
     * newer HTML5 + MathJax-related options for better compatibility and easier deployment. 
     * 
     * <h3>Technical notes</h3>
     * 
     * This generates XHTML + MathML; no XML declaration; no DOCTYPE.
     * <p>
     * This is intended to be served as <tt>application/xhtml+xml</tt> with
     * encoding declared via HTTP header and <tt>meta</tt> element.
     * <p>
     * This will display as an XML tree on IE, which is not useful.
     */
    public static final WebPageOutputOptions createMozillaSpecificOptions() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setMathJax(false);
        options.setSerializationMethod(SerializationMethod.XHTML);
        options.setIncludingXMLDeclaration(false);
        options.setContentType("application/xhtml+xml");
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} suitable for deploying solely in Internet
     * Explorer having the MathPlayer plug-in installed.
     * <p>
     * This is a niche output kept around for legacy purposes. You should look at some of the
     * newer HTML5 + MathJax-related options for better compatibility and easier deployment. 
     *  
     * <h3>Technical notes</h3>
     * 
     * Intended to be served as <tt>text/html</tt>, containing prefixed MathML islands.
     * <p>
     * This only works on IE clients with the MathPlayer plug-in preinstalled,
     * but is a good option if that's your target audience.
     * <p>
     * This will display wrongly on IE6/7 if MathPlayer is not installed.
     */
    public static final WebPageOutputOptions createIEMathPlayerSpecificOptions() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setSerializationMethod(SerializationMethod.HTML);
        options.setContentType("text/html");
        options.setPrefixingMathML(true);
        options.setAddingMathPlayerImport(true);
        return options;
    }
    
    /**
     * Create "Cross-browser" XHTML + MathML suitable for deploying to Mozilla and 
     * Internet Explorer + MathPlayer using a client-side XSLT often called the "Universal StyleSheet".
     * <p>
     * See <a href="www.w3.org/Math/XSL/Overview-tech.html">XSLT Stylesheets for MathML</a>
     * for more details.
     * <p>
     * You must specify a URL for the client-side XSLT stylesheet that will be referenced in
     * the resulting web page so that it is available to browsers. You will need to take into
     * account security restrictions, such as "same domain", so will probably need to make sure
     * you put the XSLT somewhere that is "local" to where you are serving up your pages.
     * <p>
     * Note that the SnuggleTeX source distribution contains a slightly fixed version of the
     * USS that works in versions of IE newer than 6, but hasn't been extensively tested.
     * <p>
     * This is a niche output kept around for legacy purposes. You should look at some of the
     * newer HTML5 + MathJax-related options for better compatibility and easier deployment. 
     * 
     * <h3>Technical notes</h3>
     * 
     * The resulting page is created with an XML declaration but no DOCTYPE declaration.
     * <p>
     * The <strong>pref:renderer</strong> attribute on the <tt>html</tt> element will be set
     * to <tt>mathplayer-dl</tt>.
     */
    public static final WebPageOutputOptions createUniversalStylesheetOptions(String clientStylesheetUrl) {
        return createUniversalStylesheetOptions(clientStylesheetUrl, "mathplayer-dl");
    }
    
    public static final WebPageOutputOptions createUniversalStylesheetOptions(String clientStylesheetUrl, String rendererPreference) {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setSerializationMethod(SerializationMethod.XML);
        options.setIncludingXMLDeclaration(true);
        options.setMathPrefRenderer(rendererPreference);
        options.setClientSideXSLTStylesheetURLs(clientStylesheetUrl);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} that would previously have been considered
     * "cross-browser" in that it works on both Mozilla (i.e. Firefox) and Internet Explorer 6+
     * (provided the MathPlayer plugin is installed).
     * <p>
     * This is a niche output kept around for legacy purposes. You should look at some of the
     * newer HTML5 + MathJax-related options for better compatibility and easier deployment. 
     * 
     * <h3>Technical notes</h3>
     * 
     * Output has XML declaration and DOCTYPE declaration
     * consisting of the Public identifier defined in {@link W3CConstants#XHTML_11_MATHML_20_PUBLIC_IDENTIFIER}
     * and System identifier defined in {@link W3CConstants#XHTML_11_MATHML_20_SYSTEM_IDENTIFIER}.
     * The <tt>charset</tt> is declared only in the <tt>meta</tt> element in order
     * to appease MathPlayer.
     * <p>
     * This is intended to be served as <tt>application/xhtml+xml</tt>
     * <p>
     * Works on both Mozilla and IE6/7 (<strong>provided</strong> MathPlayer has been installed).
     * This will display wrongly on IE6/7 if MathPlayer is not installed.
     * <p>
     * The main issue with this is that IE will want to download the relevant DTD, which
     * hinders performance slightly.
     */
    public static final WebPageOutputOptions createLegacyCrossBrowserOptions() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setMathJax(false);
        options.setSerializationMethod(SerializationMethod.XHTML);
        options.setIncludingXMLDeclaration(true);
        options.setContentType("application/xhtml+xml");
        options.setNoCharsetInContentTypeHeader(true);
        /* NB: There must be no namespace prefix for MathML or XHTML here, otherwise
         * it won't validate against the DTD. Even though this is the default, we'll
         * set it explicitly here for documentation sake. This is NOT checked when
         * generating the resulting page, so don't change these!
         */
        options.setXHTMLPrefix("");
        options.setMathMLPrefix("");
        options.setDoctypePublic(W3CConstants.XHTML_11_MATHML_20_PUBLIC_IDENTIFIER);
        options.setDoctypeSystem(W3CConstants.XHTML_11_MATHML_20_SYSTEM_IDENTIFIER);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} used to serve up an XHTML page, possibly including
     * MathML and other XML islands. These islands would normally need further processing before
     * hitting a browser, so this output is probably not useful on its own.
     * 
     * <h3>Technical notes</h3>
     * 
     * The JEuclid process hooks in here.
     */
    public static final WebPageOutputOptions createXHTMLOptions() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setSerializationMethod(SerializationMethod.XHTML);
        return options;
    }

    /**
     * Creates {@link WebPageOutputOptions} used to serve up an HTML 4 page, possibly including
     * MathML and other XML islands. These islands would normally need further processing before
     * hitting a browser, so this output is probably not useful on its own.
     * 
     * <h3>Technical notes</h3>
     * 
     * The JEuclid process hooks in here.
     */
    public static final WebPageOutputOptions createHTML4Options() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setSerializationMethod(SerializationMethod.HTML);
        return options;
    }
    
    /**
     * Creates {@link WebPageOutputOptions} used to serve up an HTML 5 page, with MathML and any
     * other XML islands "adopted" into HTML 4 by having their namespace details removed.
     * <p>
     * These islands would normally need further processing before
     * hitting a browser, so this output is probably not useful on its own.
     * 
     * <h3>Technical notes</h3>
     * 
     * The JEuclid process hooks in here.
     */
    public static final WebPageOutputOptions createHTML5Options() {
        WebPageOutputOptions options = new WebPageOutputOptions();
        options.setSerializationMethod(SerializationMethod.STRICTLY_HTML);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setHtml5(true);
        return options;
    }
}
