/* $Id: SnuggleTeXConfiguration.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.conversion.AbstractWebPageBuilderOptions;

/**
 * Builds on {@link DOMBuilderOptions} to add in options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleTeXSession}
 * (e.g. {@link SnuggleTeXSession#createWebPage(AbstractWebPageBuilderOptions)}).
 * 
 * <h2>Note</h2>
 * 
 * There are some dependencies between various properties here due to real-world difficulties
 * in serving up Mathematical web content.
 * Rather than failing, you may find that properties you set here get changed by SnuggleTeX
 * to make them more sane.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class MathMLWebPageBuilderOptions extends AbstractWebPageBuilderOptions {
    
    /**
     * Enumerates the different web page "templates" supported.
     */
    public static enum WebPageType {
        
        /** 
         * Mozilla-compatible output. XHTML + MathML; no XML declaration; no DOCTYPE.
         * This is intended to be served as <tt>application/xhtml+xml</tt> with
         * encoding declared via HTTP header and <tt>meta</tt> element.
         * <p>
         * This is the best option for serving content exclusively on Mozilla-based browsers.
         */
        MOZILLA,
        
        /**
         * "Cross-browser" XHTML + MathML; has XML declaration and DOCTYPE declaration;
         * served as <tt>application/xhtml+xml</tt> with <tt>charset</tt> declared only
         * in <tt>meta</tt> element in order to appease MathPlayer.
         * <p>
         * Works on both Mozilla and IE6/7 (provided MathPlayer has been installed).
         * <p>
         * The main issue with this is that IE will want to download the relevant DTD, which
         * hinders performance slightly.
         */
        CROSS_BROWSER_XHTML,
        
        /**
         * HTML + MathML intended for Internet Explorer 6/7 with the MathPlayer plug-in: served
         * as <tt>text/html</tt>.
         * <p>
         * This only works on IE + MathPlayer but is a good option if that's your target
         * audience. 
         */
        MATHPLAYER_HTML,
        
        /**
         * XHTML + MathML containing one or more processing instructions designed to invoke
         * client-side XSLT. Served as <tt>application/xhtml+xml</tt> with no XML declaration
         * and no DOCTYPE.
         * <p>
         * Combining this with the Universal Math Stylesheet or something similar can give
         * good cross-browser results.
         */
        CLIENT_SIDE_XSLT_STYLESHEET,

        ;
    }
    
    /** Desired "type" of web page to be constructed. */
    private WebPageType pageType;
    
    /** 
     * Array of relative URLs specifying client-side XSLT stylesheets to be specified in the
     * resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * This is ignored for {@link WebPageType#MATHPLAYER_HTML}. Also, if nothing is set
     * here for a {@link WebPageType#CLIENT_SIDE_XSLT_STYLESHEET} then {@link WebPageType#MOZILLA}
     * will be used as a template instead.
     */
    private String[] clientSideXSLTStylesheetURLs;
    
    public MathMLWebPageBuilderOptions() {
        super();
        this.pageType = WebPageType.MOZILLA;
        this.clientSideXSLTStylesheetURLs = StringUtilities.EMPTY_STRING_ARRAY;
    }

    
    public WebPageType getPageType() {
        return pageType;
    }
    
    public void setPageType(WebPageType type) {
        this.pageType = type;
    }
    
    
    public String[] getClientSideXSLTStylesheetURLs() {
        return clientSideXSLTStylesheetURLs;
    }
    
    public void setClientSideXSLTStylesheetURLs(String... clientSideXSLTStylesheetURLs) {
        this.clientSideXSLTStylesheetURLs = clientSideXSLTStylesheetURLs;
    }
}