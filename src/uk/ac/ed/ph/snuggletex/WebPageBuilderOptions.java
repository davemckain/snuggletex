/* $Id: SnuggleTeXConfiguration.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;

import javax.xml.transform.Transformer;

/**
 * Builds on {@link DOMBuilderOptions} to add in options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleTeXSession}
 * (e.g. {@link SnuggleTeXSession#createWebPage(WebPageBuilderOptions)}).
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
public final class WebPageBuilderOptions extends DOMBuilderOptions {
    
    /**
     * Enumerates the different web page "templates" supported.
     */
    public static enum WebPageType {
        
        /** 
         * "Default" Mozilla-compatible output. XHTML + MathML; no XML declaration; no DOCTYPE.
         * This is intended to be served as <tt>application/xhtml+xml</tt> with
         * encoding declared via HTTP header and <tt>meta</tt> element.
         * <p>
         * This is the best option for serving content exclusively on Mozilla-based browsers.
         */
        DEFAULT,
        
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
         * HTML intended for Internet Explorer 6/7 with the MathPlayer plug-in: served
         * as <tt>text/html</tt>.
         * <p>
         * This only works on IE + MathPlayer but is a good option if that's your target
         * audience. 
         */
        MATHPLAYER_HTML,
        
        /**
         * XHTML + MathML containing one or more processing instructions designed to invoke
         * client-side XSLT. Served as <tt>application/xhtml+xml</tt> with no XML declaration
         * and non DOCTYPE.
         * <p>
         * Combining this with the Universal Maths Stylesheet or something similar can give
         * good cross-browser results.
         */
        CLIENT_SIDE_XSLT_STYLESHEET,
        ;
    }
    
    /** Desired "type" of web page to be constructed. */
    private WebPageType pageType;
    
    /**
     * JAXP {@link Transformer} Object of an optional XSLT stylesheet that will be applied to the 
     * resulting web page before it is serialised. This can be useful if you need to add in
     * headers and footers to the resulting XHTML web page. Remember that the XHTML is all in
     * its correct namespace so you will need to write your stylesheet appropriately!
     * <p>
     * Certain properties will be set on this Object to ensure the correct output type etc.
     * <p>
     * If null, then no stylesheet is applied.
     */
    private Transformer stylesheet;
    
    /** 
     * Array of relative URLs specifying client-side XSLT stylesheets to be specified in the
     * resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * This is ignored for {@link WebPageType#MATHPLAYER_HTML}. Also, if nothing is set
     * here for a {@link WebPageType#CLIENT_SIDE_XSLT_STYLESHEET} then {@link WebPageType#DEFAULT}
     * will be used as a template instead.
     */
    private String[] clientSideXSLTStylesheetURLs;
    
    /** 
     * Array of relative URLs specifying client-side CSS stylesheets to be specified in the
     * resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * If used, the caller should normally ensure that one of these URLs corresponds to the
     * stylesheet for SnuggleTeX.
     * <p>
     * If not used, then CSS will be specified within a <tt>style</tt> element in the resulting
     * page.
     */
    private String[] cssStylesheetURLs;
    
    /** Title for the resulting page. If null, then no title is output. */
    private String title;
    
    /** Language code for the resulting page. Default is <tt>en</tt> */
    private String language;
    
    /** Encoding for the resulting page. Default is <tt>UTF-8</tt> */
    private String encoding;
    
    /** 
     * MIME type for the resulting page. This is generally ignored and set by SnuggleTeX
     * as appropriate for the type of page being generated.
     */
    private String contentType;
    
    /**
     * Indicates whether page title should be inserted at the start of the web page
     * body as an XHTML <tt>h1</tt> element. This has no effect if title is null.
     */
    private boolean addingTitleHeading;
    
    /**
     * Whether to indent the resulting web page or not. (This depends on how clever the underlying
     * XSLT engine will be!)
     */
    private boolean indenting;
    

    
    public WebPageBuilderOptions() {
        super();
        this.pageType = WebPageType.DEFAULT;
        this.encoding = "UTF-8";
        this.language = "en";
        this.cssStylesheetURLs = StringUtilities.EMPTY_STRING_ARRAY;
        this.clientSideXSLTStylesheetURLs = StringUtilities.EMPTY_STRING_ARRAY;
    }

    
    public WebPageType getPageType() {
        return pageType;
    }
    
    public void setPageType(WebPageType type) {
        this.pageType = type;
    }
    
    
    public Transformer getStylesheet() {
        return stylesheet;
    }
    
    public void setStylesheet(Transformer stylesheet) {
        this.stylesheet = stylesheet;
    }


    public String[] getClientSideXSLTStylesheetURLs() {
        return clientSideXSLTStylesheetURLs;
    }
    
    public void setClientSideXSLTStylesheetURLs(String... clientSideXSLTStylesheetURLs) {
        this.clientSideXSLTStylesheetURLs = clientSideXSLTStylesheetURLs;
    }

    
    public String[] getCSSStylesheetURLs() {
        return cssStylesheetURLs;
    }
    
    public void setCSSStylesheetURLs(String... cssStylesheetURLs) {
        this.cssStylesheetURLs = cssStylesheetURLs;
    }
    
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }


    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    
    public boolean isAddingTitleHeading() {
        return addingTitleHeading;
    }
    
    public void setAddingTitleHeading(boolean addingTitleHeading) {
        this.addingTitleHeading = addingTitleHeading;
    }

    
    public boolean isIndenting() {
        return indenting;
    }
    
    public void setIndenting(boolean identing) {
        this.indenting = identing;
    }
}
