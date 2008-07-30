/* $Id: SnuggleTeXConfiguration.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageBuilderOptions;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;

import javax.xml.transform.Transformer;

/**
 * Builds on {@link DOMBuilderOptions} to add in basic options for configuring how to build a
 * web page using the relevant methods in {@link SnuggleTeXSession}
 * (e.g. {@link SnuggleTeXSession#createWebPage(AbstractWebPageBuilderOptions)}).
 * <p>
 * Concrete web page building processes will subclass this to add in extra features relevant
 * to the type of pages they support.
 * 
 * <h2>Note</h2>
 * 
 * There are some dependencies between various properties here due to real-world difficulties
 * in serving up Mathematical web content. Rather than failing, you may find that properties
 * you set here get changed by SnuggleTeX to make them more sane.
 * 
 * @see MathMLWebPageBuilderOptions
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public abstract class AbstractWebPageBuilderOptions extends DOMBuilderOptions {
    
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
    
    /**
     * JAXP {@link Transformer} representing an optional XSLT stylesheet that
     * will be applied to the resulting web page once it has been built but
     * before it is serialised. This can be useful if you want to add in headers
     * and footers to the resulting XHTML web page.
     * <p>
     * Remember that the XHTML is all in its correct namespace so you will need
     * to write your stylesheet appropriately. Ensure that any XHTML you
     * generate is also in the correct namespace; it will later be converted to
     * no-namespace HTML if required by the serialisation process.
     * <p>
     * <strong>NOTE:</strong> Source documents may contain Processing
     * Instructions (e.g. to invoke MathPlayer) so these must be handled as
     * appropriate.
     * <p>
     * If null, then no stylesheet is applied.
     */
    private Transformer stylesheet;
    
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
    
    /**
     * Sets the {@link SerializationMethod} to use when generating the final output.
     * <p>
     * Note that {@link SerializationMethod#XHTML} is only supported properly if you are using
     * an XSLT 2.0 processor; otherwise it reverts to {@link SerializationMethod#XML}
     */
    private SerializationMethod serializationMethod;
    
    public AbstractWebPageBuilderOptions() {
        super();
        this.encoding = "UTF-8";
        this.language = "en";
        this.cssStylesheetURLs = StringUtilities.EMPTY_STRING_ARRAY;
        this.serializationMethod = SerializationMethod.XML;
    }

    
    public Transformer getStylesheet() {
        return stylesheet;
    }
    
    public void setStylesheet(Transformer stylesheet) {
        this.stylesheet = stylesheet;
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


    public SerializationMethod getSerializationMethod() {
        return serializationMethod;
    }
    
    public void setSerializationMethod(SerializationMethod serializationMethod) {
        this.serializationMethod = serializationMethod;
    }
}
