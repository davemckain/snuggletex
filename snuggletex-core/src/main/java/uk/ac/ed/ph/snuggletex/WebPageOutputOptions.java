/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import static uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities.concat;

import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;

import javax.xml.transform.Transformer;

/**
 * Builds on {@link XMLStringOutputOptions} to add in options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleSession}
 * (e.g. {@link SnuggleSession#buildWebPage(WebPageOutputOptions)}).
 * <p>
 * As of SnuggleTeX 1.3, you will generally want to use the various static methods in
 * {@link WebPageOutputOptionsBuilder} to create instances of this class that you can tweak
 * slightly.
 * <p>
 * The older {@link WebPageOutputOptionsTemplates} is now deprecated and will be removed in
 * SnuggleTeX 1.4.
 * 
 * @see DOMOutputOptions
 * @see XMLStringOutputOptions
 * @see WebPageOutputOptionsBuilder
 * @see WebPageOutputOptionsTemplates
 *
 * @author  David McKain
 * @version $Revision$
 */
@SuppressWarnings("javadoc")
public class WebPageOutputOptions extends XMLStringOutputOptions {
    
    /** Default content type */
    public static final String DEFAULT_CONTENT_TYPE = "application/xhtml+xml";
    
    /** Default language */
    public static final String DEFAULT_LANG = "en";
    
    /**
     * Enumerates the different web page "types" supported. This is used to tweak certain parts of
     * the page generation process. You should avoid setting this explicitly unless you know what
     * you're doing - use {@link WebPageOutputOptionsBuilder} instead. 
     * 
     * @deprecated Use {@link WebPageOutputOptionsBuilder} to generate different types of web pages.
     *   More properties have been added to this class to control some of the finer aspects of this.
     */
    @Deprecated
    public static enum WebPageType {
        
        /** 
         * Mozilla-compatible output. XHTML + MathML; no XML declaration; no DOCTYPE.
         * <p>
         * This is intended to be served as <tt>application/xhtml+xml</tt> with
         * encoding declared via HTTP header and <tt>meta</tt> element.
         * <p>
         * This is the best option for serving content exclusively on Mozilla-based browsers.
         * <p>
         * This will display as an XML tree on IE, which is not useful.
         * 
         * @deprecated Use {@link WebPageOutputOptionsBuilder#createMozillaSpecificOptions()} if
         *   you really need this, otherwise consider some of the MathJax outputs for better
         *   cross-browser impact.
         */
        @Deprecated
        MOZILLA,
        
        /**
         * "Cross-browser" XHTML + MathML; has XML declaration and DOCTYPE declaration
         * consisting of the Public identifier defined in {@link W3CConstants#XHTML_11_MATHML_20_PUBLIC_IDENTIFIER}
         * and System identifier defined in {@link W3CConstants#XHTML_11_MATHML_20_SYSTEM_IDENTIFIER}.
         * The <tt>charset</tt> is declared only in the <tt>meta</tt> element in order
         * to appease MathPlayer.
         * <p>
         * Intended to be served as <tt>application/xhtml+xml</tt>
         * <p>
         * Works on both Mozilla and IE6/7 (<strong>provided</strong> MathPlayer has been installed).
         * This will display wrongly on IE6/7 if MathPlayer is not installed.
         * <p>
         * The main issue with this is that IE will want to download the relevant DTD, which
         * hinders performance slightly.
         * 
         * @deprecated Use {@link WebPageOutputOptionsBuilder#createLegacyCrossBrowserOptions()}
         *   if you really need this, otherwise consider some of the MathJax outputs for better
         *   cross-browser impact.
         */
        @Deprecated
        CROSS_BROWSER_XHTML,
        
        /**
         * HTML + MathML intended for Internet Explorer 6/7 with the MathPlayer plug-in.
         * <p>
         * Intended to be served as <tt>text/html</tt>.
         * <p>
         * This only works on IE clients with the MathPlayer plug-in preinstalled,
         * but is a good option if that's your target audience.
         * <p>
         * This will display wrongly on IE6/7 if MathPlayer is not installed.
         * 
         * @deprecated See {@link WebPageOutputOptionsBuilder#createIEMathPlayerSpecificOptions()}
         *   if you really need this, otherwise consider some of the MathJax outputs for better
         *   cross-browser impact.
         */
        @Deprecated
        MATHPLAYER_HTML,
        
        //----------------------------------------------------------
        // The following require further configuration
        
        /**
         * "Cross-browser" XHTML + MathML suitable for Mozilla and Internet Explorer 6/7.
         * Intended to be used in conjunction with the client-side Universal StyleSheet XSLT
         * to accommodate the two cases, prompting
         * for the download of MathPlayer on IE6/7 if it is not already installed.
         * <p>
         * Page is created with an XML declaration but no DOCTYPE declaration.
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
         * The SnuggleTeX source distribution contains a slightly fixed version of the
         * USS that works in IE7 that you can use if you like.
         * 
         * @deprecated Use {@link WebPageOutputOptionsBuilder#createUniversalStylesheetOptions(String)}
         *   if you really need this, otherwise consider some of the MathJax outputs for better
         *   cross-browser impact.
         */
        @Deprecated
        UNIVERSAL_STYLESHEET,
        
        /**
         * XHTML + MathML containing one or more processing instructions designed to invoke
         * client-side XSLT. No XML declaration and no DOCTYPE.
         * <p>
         * Intended to be served as <tt>application/xhtml+xml</tt>.
         * <p>
         * Combining this with the Universal Math Stylesheet or something similar can give
         * good cross-browser results.
         * 
         * @deprecated Use {@link WebPageOutputOptionsBuilder#createUniversalStylesheetOptions(String)}
         *   if you really need this, otherwise consider some of the MathJax outputs for better
         *   cross-browser impact.
         */
        @Deprecated
        CLIENT_SIDE_XSLT_STYLESHEET,
        
        /**
         * HTML deemed suitable for use by any User Agent. 
         * <p>
         * Intended to be served as <tt>text/html</tt>.
         * <p>
         * You will have to use a suitable {@link DOMPostProcessor} to convert any MathML islands
         * into other forms. (E.g. replace by an applet, replace by images, ...)
         * 
         * @deprecated Use {@link WebPageOutputOptionsBuilder#createHTML4Options()}
         */
        @Deprecated
        PROCESSED_HTML,
        
        ;
    }
    
    /** 
     * Desired "type" of web page to be constructed.
     * 
     * @deprecated As of SnuggleTeX 1.3.0, this property no longer controls anything. This will be
     *   removed in SnuggleTeX 1.4
     * 
     * @see WebPageOutputOptionsBuilder
     */
    @SuppressWarnings("unused")
    @Deprecated
    private WebPageType webPageType;
    
    /** 
     * MIME type for the resulting page.
     * <p>
     * Default is {@link #DEFAULT_CONTENT_TYPE}.
     * <p>
     * This must not be null.
     */
    private String contentType;
    
    /** 
     * Language code for the resulting page.
     * <p>
     * Default is {@link #DEFAULT_LANG}.
     * <p>
     * This may be set to null
     */
    private String lang;
    
    /** 
     * Title for the resulting page.
     * Default is null.
     * If null, then a boilerplate title is added.
     */
    private String title;
    
    /**
     * Indicates whether page title should be inserted at the start of the web page
     * body as an XHTML <tt>h1</tt> element. This has no effect if title is null.
     * <p>
     * Default is false.
     */
    private boolean addingTitleHeading;
    
    /**
     * Indicates whether to include the necessary processing instruction and <tt>object</tt>
     * element required to explicitly trigger the MathPlayer plugin.
     * <p>
     * The default is false.
     * 
     * @since 1.3.0
     */
    private boolean addingMathPlayerImport;
    
    /**
     * Value of the optional <tt>pref:renderer</tt> attribute 
     * (in the {@link W3CConstants#MATHML_PREF_NAMESPACE})
     * that can be added to the HTML root element to control certain aspects of the
     * client-side 
     * <a href="http://www.w3.org/Math/XSL/Overview-tech.html Universal StyleSheets for MathML</a>
     * <p>
     * The default is null, indicating that no attribute will be added
     * 
     * @since 1.3.0
     */
    private String mathPrefRenderer;
    
    /**
     * Set to include SnuggleTeX-related CSS as a <tt>style</tt> element within the resulting
     * page. If you choose not to do this, you probably want to put <tt>snuggletex.css</tt>
     * somewhere accessible and pass its location in {@link #clientSideXSLTStylesheetURLs}.
     * <p>
     * Default is true, as that's the simplest way of getting up to speed quickly.
     */
    private boolean includingStyleElement;
    
    /** 
     * Array of relative URLs specifying client-side CSS stylesheets to be specified in the
     * resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * The caller can use this to specify the location of <tt>snuggletex.css</tt>, as well
     * as any other required stylesheets.
     */
    private String[] cssStylesheetURLs;
    
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
    
    /**
     * Optional JAXP {@link Transformer}s representing XSLT stylesheet(s) that
     * will be applied to the resulting web page once it has been built but
     * before it is serialised. This can be useful if you want to add in headers
     * and footers to the resulting XHTML web page.
     * <p>
     * Remember that the XHTML is all in its correct namespace so you will need
     * to write your stylesheet appropriately. Ensure that any further XHTML you
     * generate is also in the correct namespace; it will later be converted to
     * no-namespace HTML if required by the serialisation process.
     * <p>
     * <strong>NOTE:</strong> Source documents may contain Processing
     * Instructions (e.g. to invoke MathPlayer) so these must be handled as
     * appropriate.
     * <p>
     * If null or empty, then no stylesheet is applied.
     */
    private Transformer[] stylesheets;
    
    /**
     * Determines whether to generate HTML (or XHTML 5) output, which uses a "charset" meta
     * attribute and a different DOCTYPE declaration.
     * <p>
     * Note that if set to true, then this will override whatever is returned by
     * {@link #getDoctypePublic()} and {@link #getDoctypeSystem()}.
     * <p>
     * The default is false.
     * 
     * @since 1.3.0
     */
    private boolean html5;
    
    /**
     * Determines whether to omit character set details in the Content-Type HTTP header when
     * streaming web pages.
     * <p>
     * MathPlayer can only handle application/xhtml+xml without a "charset" clause, so this should
     * be used in those cases.
     * 
     * @since 1.3.0
     */
    private boolean noCharsetInContentTypeHeader;
    
    /**
     * Determines whether to add the required JavaScript to invoke MathJax to render the
     * resulting web page.
     * <p>
     * The default is false.
     * 
     * @since 1.3.0
     */
    private boolean mathJax;
    
    /**
     * Specifies a custom MathJax URL to use when invoking MathJax. Use this if you want to use
     * a local installation of MathJax, or want to specify custom configuration directives.
     * 
     * See the MathJax <a href="http://www.mathjax.org/docs/2.0/start.html#mathjax-cdn">Getting Started</a>
     * documentation for more details.
     * <p>
     * The default is null, which will use the MathJax CDN with a reasonable default configuration
     * if {@link #isMathJax()} returns true.
     * 
     * @since 1.3.0
     */
    private String customMathJaxUrl;

    
    public WebPageOutputOptions() {
        super();
        this.webPageType = WebPageType.MOZILLA;
        this.contentType = DEFAULT_CONTENT_TYPE;
        this.lang = DEFAULT_LANG;
        this.title = null;
        this.addingTitleHeading = false;
        this.addingMathPlayerImport = false;
        this.mathPrefRenderer = null;
        this.includingStyleElement = true;
        this.cssStylesheetURLs = null;
        this.clientSideXSLTStylesheetURLs = null;
        this.stylesheets = null;
        this.html5 = false;
        this.noCharsetInContentTypeHeader = false;
        this.mathJax = false;
        this.customMathJaxUrl = null;
    }
    
    
    /**
     * Before SnuggleTeX 1.3.0, this returned the desired "type" of web page to be constructed.
     * <p>
     * From SnuggleTeX 1.3.0, this property no longer controls anything.
     * 
     * @deprecated Use various methods in {@link WebPageOutputOptionsBuilder} to generate
     *   suitable options for various types of pages, which you can then tweak.
     */
    @Deprecated
    public WebPageType getWebPageType() {
        return WebPageType.MOZILLA;
    }
    
    /**
     * Before SnuggleTeX 1.3.0, this returned the desired "type" of web page to be constructed.
     * <p>
     * From SnuggleTeX 1.3.0, this property no longer controls anything.
     * 
     * @deprecated Use various methods in {@link WebPageOutputOptionsBuilder} to generate
     *   suitable options for various types of pages, which you can then tweak.
     */
    @Deprecated
    public void setWebPageType(WebPageType webPageType) {
        ConstraintUtilities.ensureNotNull(webPageType, "webPageType");
        this.webPageType = webPageType;
    }
    
    
    /** 
     * Returns the MIME type for the resulting page.
     * <p>
     * Defaults to {@link #DEFAULT_CONTENT_TYPE}.
     */
    public String getContentType() {
        return contentType;
    }
    
    /** 
     * Sets the MIME type for the resulting page.
     * 
     * @param contentType desired contentType, which must not be null.
     */
    public void setContentType(String contentType) {
        ConstraintUtilities.ensureNotNull(contentType, "contentType");
        this.contentType = contentType;
    }


    /**
     * Returns the language of the resulting page, null if not set.
     * <p>
     * Defaults to {@link #DEFAULT_LANG}.
     */
    public String getLang() {
        return lang;
    }
    
    /**
     * Sets the language of the resulting page.
     * 
     * @param lang desired language, which may be null.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    
    /**
     * Returns the title for the resulting page, null if not set.
     * <p>
     * Default is null.
     * <p>
     * This is used to generate a <tt>title</tt> and possible a <tt>h1</tt>
     * header if {@link #isAddingTitleHeading()} returns true.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title for the resulting page.
     * <p>
     * This is used to generate a <tt>title</tt> and possible a <tt>h1</tt>
     * header if {@link #isAddingTitleHeading()} returns true.
     * 
     * @param title title for the required page, which may be null to indicate
     *   that no title should be included. 
     */
    public void setTitle(String title) {
        this.title = title;
    }

    
    /**
     * Returns whether page title should be inserted at the start of the web page
     * body as an XHTML <tt>h1</tt> element.
     * <p>
     * Default is false.
     * <p>
     * This has no effect if {@link #getTitle()} returns null.
     */
    public boolean isAddingTitleHeading() {
        return addingTitleHeading;
    }
    
    /**
     * Sets whether page title should be inserted at the start of the web page
     * body as an XHTML <tt>h1</tt> element.
     * <p>
     * This has no effect if {@link #getTitle()} returns null.
     * 
     * @param addingTitleHeading true to add a title header if a title has been set, false otherwise.
     */
    public void setAddingTitleHeading(boolean addingTitleHeading) {
        this.addingTitleHeading = addingTitleHeading;
    }
    
    
    /**
     * Returns whether to include the necessary processing instruction and <tt>object</tt>
     * element required to explicitly trigger the MathPlayer plugin.
     * 
     * @since 1.3.0
     * 
     * @return true if adding MathPlayer import gubbins, false otherwise.
     */
    public boolean isAddingMathPlayerImport() {
        return addingMathPlayerImport;
    }


    /**
     * Sets whether to include the necessary processing instruction and <tt>object</tt>
     * element required to explicitly trigger the MathPlayer plugin.
     *
     * @since 1.3.0
     * 
     * @param addingMathPlayerImport true to add gubbins for importing MathPlayer, false otherwise.
     */
    public void setAddingMathPlayerImport(boolean addingMathPlayerImport) {
        this.addingMathPlayerImport = addingMathPlayerImport;
    }
    
    
    /**
     * Gets the value of the optional <tt>pref:renderer</tt> attribute 
     * (in the {@link W3CConstants#MATHML_PREF_NAMESPACE})
     * that can be added to the HTML root element to control certain aspects of the
     * client-side 
     * <a href="http://www.w3.org/Math/XSL/Overview-tech.html Universal StyleSheets for MathML</a>
     * <p>
     * The default is null, indicating that no attribute will be added
     * 
     * @since 1.3.0
     */
    public String getMathPrefRenderer() {
        return mathPrefRenderer;
    }
    
    /**
     * Sets the value of the optional <tt>pref:renderer</tt> attribute 
     * (in the {@link W3CConstants#MATHML_PREF_NAMESPACE})
     * that can be added to the HTML root element to control certain aspects of the
     * client-side 
     * <a href="http://www.w3.org/Math/XSL/Overview-tech.html Universal StyleSheets for MathML</a>
     * <p>
     * A null value is allowed, which prevents such an attribute being added.
     * 
     * @since 1.3.0
     */
    public void setMathPrefRenderer(String mathPrefRenderer) {
        this.mathPrefRenderer = mathPrefRenderer;
    }


    /**
     * Returns whether to include SnuggleTeX-related CSS as a <tt>style</tt> element within the
     * resulting page. If you choose not to do this, you probably want to put <tt>snuggletex.css</tt>
     * somewhere accessible and pass its location in via {@link #setClientSideXSLTStylesheetURLs(String...)}.
     * <p>
     * As of SnuggleTeX 1.2.3, this option is ignored if {@link #isInliningCSS()} returns true as its
     * effect is clearly redundant in this case.
     * <p>
     * Default is true, as that's the simplest way of getting up to speed quickly.
     */
    public boolean isIncludingStyleElement() {
        return includingStyleElement;
    }
    
    /**
     * Sets whether to include SnuggleTeX-related CSS as a <tt>style</tt> element within the
     * resulting page. If you choose not to do this, you probably want to put <tt>snuggletex.css</tt>
     * somewhere accessible and pass its location in via {@link #setClientSideXSLTStylesheetURLs(String...)}.
     * <p>
     * As of SnuggleTeX 1.2.3, this option is ignored if {@link #isInliningCSS()} returns true as its
     * effect is clearly redundant in this case.
     * 
     * @param includingStyleElement set to true to include a <tt>style</tt> element, false otherwise.
     */
    public void setIncludingStyleElement(boolean includingStyleElement) {
        this.includingStyleElement = includingStyleElement;
    }


    /** 
     * Returns specified array of relative URLs specifying client-side CSS stylesheets to be
     * referenced in the resulting page.
     * <p>
     * Default is null.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * The caller can use this to specify the location of <tt>snuggletex.css</tt>, as well
     * as any other required stylesheets.
     */
    public String[] getCSSStylesheetURLs() {
        return cssStylesheetURLs;
    }
    
    /** 
     * Specifies an array of relative URLs specifying client-side CSS stylesheets to be
     * referenced in the resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * The caller can use this to specify the location of <tt>snuggletex.css</tt>, as well
     * as any other required stylesheets.
     * 
     * @param cssStylesheetURLs array of CSS stylesheet URLs, which may be empty
     */
    public void setCSSStylesheetURLs(String... cssStylesheetURLs) {
        this.cssStylesheetURLs = cssStylesheetURLs;
    }
    
    /** 
     * Appends to existing array of relative URLs specifying client-side CSS stylesheets to be
     * referenced in the resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * The caller can use this to specify the location of <tt>snuggletex.css</tt>, as well
     * as any other required stylesheets.
     * 
     * @param cssStylesheetURLs array of CSS stylesheet URLs to add, which may be empty
     */
    public void addCSSStylesheetURLs(String... cssStylesheetURLs) {
        this.cssStylesheetURLs = concat(this.cssStylesheetURLs, cssStylesheetURLs, String.class);
    }
    
    
    /** 
     * Returns specified array of relative URLs specifying client-side XSLT stylesheets to be
     * referenced in the resulting page.
     * <p>
     * Default is null
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * This is ignored for {@link WebPageType#MATHPLAYER_HTML}. Also, if nothing is set
     * here for a {@link WebPageType#CLIENT_SIDE_XSLT_STYLESHEET} then {@link WebPageType#MOZILLA}
     * will be used as a template instead.
     */
    public String[] getClientSideXSLTStylesheetURLs() {
        return clientSideXSLTStylesheetURLs;
    }
    
    /** 
     * Sets an array of relative URLs specifying client-side XSLT stylesheets to be
     * referenced in the resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * This is ignored for {@link WebPageType#MATHPLAYER_HTML}. Also, if nothing is set
     * here for a {@link WebPageType#CLIENT_SIDE_XSLT_STYLESHEET} then {@link WebPageType#MOZILLA}
     * will be used as a template instead.
     * 
     * @param clientSideXSLTStylesheetURLs array of URLs to use, which may be empty.
     */
    public void setClientSideXSLTStylesheetURLs(String... clientSideXSLTStylesheetURLs) {
        this.clientSideXSLTStylesheetURLs = clientSideXSLTStylesheetURLs;
    }
    
    /** 
     * Appends to existing array of relative URLs specifying client-side XSLT stylesheets to be
     * referenced in the resulting page.
     * <p>
     * The URLs are used as-is; the caller should have ensured they make sense in advance!
     * <p>
     * This is ignored for {@link WebPageType#MATHPLAYER_HTML}. Also, if nothing is set
     * here for a {@link WebPageType#CLIENT_SIDE_XSLT_STYLESHEET} then {@link WebPageType#MOZILLA}
     * will be used as a template instead.
     * 
     * @param clientSideXSLTStylesheetURLs array of URLs to append, which may be empty.
     */
    public void addClientSideXSLTStylesheetURLs(String... clientSideXSLTStylesheetURLs) {
        this.clientSideXSLTStylesheetURLs = concat(this.clientSideXSLTStylesheetURLs, clientSideXSLTStylesheetURLs, String.class);
    }


    /**
     * Returns an array of specified JAXP {@link Transformer}s representing XSLT stylesheet(s)
     * that will be applied to the resulting web page once it has been built but
     * before it is serialised. This can be useful if you want to add in headers
     * and footers to the resulting XHTML web page.
     * <p>
     * Default is null.
     * <p>
     * Remember that the XHTML is all in its correct namespace so you will need
     * to write your stylesheet appropriately. Ensure that any further XHTML you
     * generate is also in the correct namespace; it will later be converted to
     * no-namespace HTML if required by the serialisation process.
     * <p>
     * <strong>NOTE:</strong> Source documents may contain Processing
     * Instructions (e.g. to invoke MathPlayer) so these must be handled as
     * appropriate.
     */
    public Transformer[] getStylesheets() {
        return stylesheets;
    }
    
    /**
     * Sets an array of JAXP {@link Transformer}s representing XSLT stylesheet(s)
     * that will be applied to the resulting web page once it has been built but
     * before it is serialised. This can be useful if you want to add in headers
     * and footers to the resulting XHTML web page.
     * <p>
     * Remember that the XHTML is all in its correct namespace so you will need
     * to write your stylesheet appropriately. Ensure that any further XHTML you
     * generate is also in the correct namespace; it will later be converted to
     * no-namespace HTML if required by the serialisation process.
     * <p>
     * <strong>NOTE:</strong> Source documents may contain Processing
     * Instructions (e.g. to invoke MathPlayer) so these must be handled as
     * appropriate.
     * 
     * @param stylesheets array of XSLT stylesheets to apply, which may be null. They
     *   are applied in the order specified.
     */
    public void setStylesheets(Transformer... stylesheets) {
        this.stylesheets = stylesheets;
    }
    
    /**
     * Appends to existing array of JAXP {@link Transformer}s representing XSLT stylesheet(s)
     * that will be applied to the resulting web page once it has been built but
     * before it is serialised. This can be useful if you want to add in headers
     * and footers to the resulting XHTML web page.
     * <p>
     * Remember that the XHTML is all in its correct namespace so you will need
     * to write your stylesheet appropriately. Ensure that any further XHTML you
     * generate is also in the correct namespace; it will later be converted to
     * no-namespace HTML if required by the serialisation process.
     * <p>
     * <strong>NOTE:</strong> Source documents may contain Processing
     * Instructions (e.g. to invoke MathPlayer) so these must be handled as
     * appropriate.
     * 
     * @param stylesheets array of additional XSLT stylesheets to apply, which may be null. They
     *   are applied in the order specified.
     */
    public void addStylesheets(Transformer... stylesheets) {
        this.stylesheets = concat(this.stylesheets, stylesheets, Transformer.class);
    }

    public boolean isHtml5() {
        return html5;
    }
    
    public void setHtml5(boolean html5) {
        this.html5 = html5;
    }
    
    
    
    public boolean isNoCharsetInContentTypeHeader() {
        return noCharsetInContentTypeHeader;
    }
    
    public void setNoCharsetInContentTypeHeader(boolean noCharsetInContentTypeHeader) {
        this.noCharsetInContentTypeHeader = noCharsetInContentTypeHeader;
    }
    
    
    public boolean isMathJax() {
        return mathJax;
    }
    
    public void setMathJax(boolean mathJax) {
        this.mathJax = mathJax;
    }


    public String getCustomMathJaxUrl() {
        return customMathJaxUrl;
    }
    
    public void setCustomMathJaxUrl(String mathJaxPath) {
        this.customMathJaxUrl = mathJaxPath;
    }
}
