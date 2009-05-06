/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.commons.util.StringUtilities;

/**
 * Builds on {@link DOMOutputOptions} to add in options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleSession}
 * (e.g. {@link SnuggleSession#createWebPage(BaseWebPageOptions)}).
 * 
 * <h2>Note</h2>
 * 
 * There are some dependencies between various properties here due to real-world difficulties
 * in serving up Mathematical web content.
 * Rather than failing, you may find that properties you set here get changed by SnuggleTeX
 * to make them more sane.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathMLWebPageOptions extends BaseWebPageOptions {
    
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
         * <p>
         * This will display as an XML tree on IE, which is not useful.
         */
        MOZILLA,
        
        /**
         * "Cross-browser" XHTML + MathML; has XML declaration and DOCTYPE declaration;
         * served as <tt>application/xhtml+xml</tt> with <tt>charset</tt> declared only
         * in <tt>meta</tt> element in order to appease MathPlayer.
         * <p>
         * Works on both Mozilla and IE6/7 (<strong>provided</strong> MathPlayer has been installed).
         * This will display wrongly on IE6/7 if MathPlayer is not installed.
         * <p>
         * The main issue with this is that IE will want to download the relevant DTD, which
         * hinders performance slightly.
         */
        CROSS_BROWSER_XHTML,
        
        /**
         * "Cross-browser" XHTML + MathML suitable for Mozilla and Internet Explorer 6/7, using
         * the client-side Universal StyleSheet XSLT to accommodate the two cases, prompting
         * for the download of MathPlayer on IE6/7 if it is not already installed.
         * <p>
         * This is served with an XML declaration but no DOCTYPE declaration.
         * <p>
         * The <strong>pref:renderer</strong> attribute on the <tt>html</tt> element will be set
         * to <tt>mathplayer-dl</tt>.
         * <p>
         * You <strong>MUST</strong> also call
         * {@link #setClientSideXSLTStylesheetURLs(String...)}
         * to indicate where the USS is going to be loaded from. This <strong>MUST</strong>
         * be on a server local to the document you are serving from, because IE enforces
         * a "same origin" policy for loading XSLT stylesheets. If you don't do
         * this, your page will not work on IE.
         * 
         * <h2>Notes</h2>
         * 
         * SnuggleTeX ships with a slightly fixed version of the USS that works in IE7
         * that you can use if you like.
         */
        UNIVERSAL_STYLESHEET,
        
        /**
         * HTML + MathML intended for Internet Explorer 6/7 with the MathPlayer plug-in: served
         * as <tt>text/html</tt>.
         * <p>
         * This only works on IE clients with the MathPlayer plug-in preinstalled,
         * but is a good option if that's your target audience.
         * <p>
         * This will display wrongly on IE6/7 if MathPlayer is not installed.
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
    
    public MathMLWebPageOptions() {
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