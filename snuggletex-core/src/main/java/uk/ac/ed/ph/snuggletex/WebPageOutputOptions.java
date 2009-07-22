/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import static uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities.concat;

import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

/**
 * Builds on {@link XMLOutputOptions} to add in options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleSession}
 * (e.g. {@link SnuggleSession#createWebPage(WebPageOutputOptions)}).
 * <p>
 * You will generally want to use
 * {@link WebPageOutputOptionsTemplates#createWebPageOptions(WebPageOutputOptions.WebPageType)}
 * to create pre-configured instances of these Objects, which can then be tweaked as desired.
 * But you can also create and configure {@link WebPageOutputOptions} from scratch if you
 * know exactly what you want to do.
 * 
 * @see DOMOutputOptions
 * @see XMLOutputOptions
 * @see WebPageOutputOptionsTemplates
 *
 * @author  David McKain
 * @version $Revision$
 */
public class WebPageOutputOptions extends XMLOutputOptions {
    
    /**
     * Enumerates the different web page "types" supported. This is used both by
     * {@link WebPageOutputOptionsTemplates} to help generate suitable instances
     * of {@link WebPageOutputOptions}, and also to tweak certain parts of the page generation
     * process.
     */
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
         */
        MOZILLA,
        
        /**
         * "Cross-browser" XHTML + MathML; has XML declaration and DOCTYPE declaration
         * consisting of the Public identifier defined in {@link W3CConstants#XHTML_11_MATHML_20_PUBLIC_DTD}
         * and System identifier defined in {@link W3CConstants#XHTML_11_MATHML_20_SYSTEM_DTD}.
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
         */
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
         */
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
         */
        UNIVERSAL_STYLESHEET,
        
        /**
         * XHTML + MathML containing one or more processing instructions designed to invoke
         * client-side XSLT. No XML declaration and no DOCTYPE.
         * <p>
         * Intended to be served as <tt>application/xhtml+xml</tt>.
         * <p>
         * Combining this with the Universal Math Stylesheet or something similar can give
         * good cross-browser results.
         */
        CLIENT_SIDE_XSLT_STYLESHEET,
        
        /**
         * HTML deemed suitable for use by any User Agent. 
         * <p>
         * Intended to be served as <tt>text/html</tt>.
         * <p>
         * You will have to use a suitable {@link DOMPostProcessor} to convert any MathML islands
         * into other forms. (E.g. replace by an applet, replace by images, ...)
         * <p>
         * This is what the SnuggleTeX JEuclid extension hooks into to do its magic. 
         */
        PROCESSED_HTML,
        
        ;
    }
    
    /**
     * Trivial enumeration of the 3 serialization methods we support.
     * <p>
     * <strong>NOTE:</strong> XHTML is only supported if you are using an XSLT 2.0
     * processor. If not supported, you will get XML output.
     */
    public static enum SerializationMethod {
        
        XML("xml"),
        XHTML("xhtml"),
        HTML("html"),
        ;
        
        private final String name;
        
        private SerializationMethod(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    
    /** Desired "type" of web page to be constructed. Must not be null. */
    private WebPageType webPageType;
    
    /**
     * {@link SerializationMethod} to use when generating the final output.
     * Default is {@link SerializationMethod#XML}.
     * This must not be null.
     * <p>
     * Note that {@link SerializationMethod#XHTML} is only supported properly if you are using
     * an XSLT 2.0 processor; otherwise it reverts to {@link SerializationMethod#XML}
     */
    private SerializationMethod serializationMethod;
    
    /**
     * Public identifier for resulting document type declaration,
     * as described in {@link OutputKeys#DOCTYPE_PUBLIC}.
     */
    private String doctypePublic;
    
    /**
     * System identifier for resulting document type declaration,
     * as described in {@link OutputKeys#DOCTYPE_SYSTEM}.
     */
    private String doctypeSystem;
    
    /** 
     * MIME type for the resulting page.
     * Defaults to {@link WebPageOutputOptionsTemplates#DEFAULT_CONTENT_TYPE}.
     * This must not be null.
     */
    private String contentType;
    
    /** 
     * Language code for the resulting page.
     * Default is <tt>en</tt>.
     * May be set to null
     */
    private String lang;
    
    /** 
     * Title for the resulting page.
     * Default is null.
     * If null, then no title is added.
     */
    private String title;
    
    /**
     * Indicates whether page title should be inserted at the start of the web page
     * body as an XHTML <tt>h1</tt> element. This has no effect if title is null.
     */
    private boolean addingTitleHeading;
    
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
    
    public WebPageOutputOptions() {
        super();
        this.contentType = WebPageOutputOptionsTemplates.DEFAULT_CONTENT_TYPE;
        this.webPageType = WebPageType.MOZILLA;
        this.serializationMethod = SerializationMethod.XML;
        this.lang = WebPageOutputOptionsTemplates.DEFAULT_LANG;
        this.includingStyleElement = true;
    }
    
    
    public WebPageType getWebPageType() {
        return webPageType;
    }
    
    public void setWebPageType(WebPageType webPageType) {
        this.webPageType = webPageType;
    }
    
    
    public SerializationMethod getSerializationMethod() {
        return serializationMethod;
    }
    
    public void setSerializationMethod(SerializationMethod serializationMethod) {
        this.serializationMethod = serializationMethod;
    }
    
    
    public String getDoctypePublic() {
        return doctypePublic;
    }
    
    public void setDoctypePublic(String doctypePublic) {
        this.doctypePublic = doctypePublic;
    }

    
    public String getDoctypeSystem() {
        return doctypeSystem;
    }
    
    public void setDoctypeSystem(String doctypeSystem) {
        this.doctypeSystem = doctypeSystem;
    }


    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


    public String getLang() {
        return lang;
    }
    
    public void setLang(String lang) {
        this.lang = lang;
    }


    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    
    public boolean isAddingTitleHeading() {
        return addingTitleHeading;
    }
    
    public void setAddingTitleHeading(boolean addingTitleHeading) {
        this.addingTitleHeading = addingTitleHeading;
    }

    
    public boolean isIncludingStyleElement() {
        return includingStyleElement;
    }
    
    public void setIncludingStyleElement(boolean includingStyleElement) {
        this.includingStyleElement = includingStyleElement;
    }


    public String[] getCSSStylesheetURLs() {
        return cssStylesheetURLs;
    }
    
    public void setCSSStylesheetURLs(String... cssStylesheetURLs) {
        this.cssStylesheetURLs = cssStylesheetURLs;
    }
    
    public void addCSSStylesheetURLs(String... cssStylesheetURLs) {
        this.cssStylesheetURLs = concat(this.cssStylesheetURLs, cssStylesheetURLs, String.class);
    }
    
    
    public String[] getClientSideXSLTStylesheetURLs() {
        return clientSideXSLTStylesheetURLs;
    }
    
    public void setClientSideXSLTStylesheetURLs(String... clientSideXSLTStylesheetURLs) {
        this.clientSideXSLTStylesheetURLs = clientSideXSLTStylesheetURLs;
    }
    
    public void addClientSideXSLTStylesheetURLs(String... clientSideXSLTStylesheetURLs) {
        this.clientSideXSLTStylesheetURLs = concat(this.clientSideXSLTStylesheetURLs, clientSideXSLTStylesheetURLs, String.class);
    }


    public Transformer[] getStylesheets() {
        return stylesheets;
    }
    
    public void setStylesheets(Transformer... stylesheets) {
        this.stylesheets = stylesheets;
    }
    
    public void addStylesheets(Transformer... stylesheets) {
        this.stylesheets = concat(this.stylesheets, stylesheets, Transformer.class);
    }
}
