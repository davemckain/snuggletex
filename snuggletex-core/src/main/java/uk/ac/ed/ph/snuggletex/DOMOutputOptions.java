/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;

import java.util.Properties;

/**
 * This class is used to specify how you want DOM trees to be built when calling relevant methods
 * in {@link SnuggleSession} (e.g. {@link SnuggleSession#buildDOMSubtree(org.w3c.dom.Element)}
 *
 * @author  David McKain
 * @version $Revision$
 */
public class DOMOutputOptions implements Cloneable {
    
    /** Default prefix to use if/when prefixing XHTML elements */
    public static final String DEFAULT_XHTML_PREFIX = "h";
    
    /** Default prefix to use if/when prefixing MathML elements */
    public static final String DEFAULT_MATHML_PREFIX = "m";
    
    /** Default prefix to use if/when prefixing SnuggleTeX XML elements */
    public static final String DEFAULT_SNUGGLETEX_XML_PREFIX = "s";
    
    /**
     * Enumerates the various options for representing {@link InputError}s in the resulting
     * DOM.
     */
    public static enum ErrorOutputOptions {
        
        /**
         * No error information is appended to the DOM. (Clients can still get at error information
         * via {@link SnuggleSession#getErrors()}.)
         */
        NO_OUTPUT,
        
        /**
         * Basic error information is appended to the DOM as an empty XML element containing just the
         * error code.
         */
        XML_SHORT,
        
        /**
         * Full error information is appended to the DOM as a text XML element containing the error
         * code (as an attribute) plus full textual information of the error's location.
         */
        XML_FULL,
        
        /**
         * Each error is marked up as an XHTML <tt>div</tt> element associated to the <tt>error</tt>
         * CSS class.
         * <p>
         * Errors occurring inside MathML expressions will be represented by a <tt>merror</tt>
         * placeholder, with a full XHTML element appended immediately after the MathML.
         */
        XHTML,
        ;
    }
    
    /**
     * Specifies how errors should be represented in the resulting DOM.
     */
    private ErrorOutputOptions errorOutputOptions;
    
    /** Set to true to annotate MathML elements with the original SnuggleTeX source. */
    private boolean addingMathAnnotations;
    
    /** 
     * Set to true to inline CSS styling (i.e. uses <tt>style</tt> attributes). This is useful
     * if your output is going to end up embedded in someone else's page or in a system where you
     * have no control over stylesheets.
     */
    private boolean inliningCSS;
    
    /**
     * If non-null, then the given {@link Properties} will be used to work out CSS styles.
     * If null, the default CSS will be used.
     */
    private Properties inlineCSSProperties;
    
    /**
     * Set to true if you want XHTML element names to be prefixed.
     * If false, then the default namespace is changed when entering an XHTML element scope.
     * <p>
     * Default is false.
     */
    private boolean prefixingXHTML;
    
    /**
     * Prefix to use when prefixing XHTML element names.
     * Only used if {@link #prefixingXHTML} is true.
     * <p>
     * Default is {@link #DEFAULT_XHTML_PREFIX}
     * Must be non-null and a valid NCName.
     */
    private String xhtmlPrefix;

    /**
     * Set to true if you want MathML element names to be prefixed.
     * If false, then the default namespace is changed when entering a MathML element scope.
     * <p>
     * Default is false.
     */
    private boolean prefixingMathML;
    
    /**
     * Prefix to use when prefixing MathML element names.
     * Only used if {@link #prefixingMathML} is true.
     * <p>
     * Default is {@link #DEFAULT_MATHML_PREFIX}
     * Must be non-null and a valid NCName.
     */
    private String mathMLPrefix;
    
    /**
     * Set to true if you want any SnuggleTeX-specific XML elements, such
     * as error messages and certain MathML annotations to be prefixed.
     * <p>
     * If false, then the default namespace is changed when entering the scope of
     * these elements.
     * <p>
     * Default is false.
     */
    private boolean prefixingSnuggleXML;
    
    /**
     * Prefix to use when prefixing SnuggleTeX-specific XML element names.
     * Only used if {@link #prefixingSnuggleXML} is true and ignored if prefix is null.
     * <p>
     * (Note that this currently has no effect on any SnuggleTeX elements generated within
     * MathML elements.)
     * <p>
     * Default is {@link #DEFAULT_SNUGGLETEX_XML_PREFIX}.
     * Must be non-null and a valid NCName.
     */
    private String snuggleXMLPrefix;
    
    /**
     * Set to true to perform automatic mappings of (safe) Unicode characters when applying
     * "stylings" like <tt>\\mathcal</tt> and <tt>\\mathbb</tt>. Doing this can help if you
     * don't have control over CSS and if clients may not have appropriate fonts installed
     * as it forces the mapping of certain characters to glyphs that may be available.
     * <p>
     * (Firefox by default does not change fonts for these cases as it is not clear what
     * font to map to so setting this to true can help with some characters.)
     * 
     * TODO: Revisit how best to do mathvariant with Firefox.
     */
    private boolean mathVariantMapping;
    
    /**
     * List of optional {@link DOMPostProcessor} that will be called in turn to "fix up" or
     * modify the raw DOM produced by SnuggleTeX immediately after it has been built.
     * <p>
     * One use of this is by registering a {@link DownConvertingPostProcessor}, which will
     * attempt to "down-convert" simple MathML expressions into (X)HTML equivalents.
     * For example, simple linear expressions and simple sub/superscripts can often be converted
     * to acceptable XHTML alternatives.
     * Any expressions deemed too complex to be down-converted are kept as MathML.
     * <p>
     * The SnuggleTeX Up-Conversion module also includes an "UpConvertingPostProcessor" that
     * may be used here as well.
     * 
     * @see DownConvertingPostProcessor
     */
    private DOMPostProcessor[] domPostProcessors;
    
    /**
     * Optional helper to "resolve" (i.e. munge) any XHTML links found during page creation.
     * This may be useful if generating a set of hyperlinked pages.
     */
    private LinkResolver linkResolver;
    
    public DOMOutputOptions() {
        this.errorOutputOptions = ErrorOutputOptions.NO_OUTPUT;
        this.domPostProcessors = null;
        this.inliningCSS = false;
        this.addingMathAnnotations = false;
        this.inlineCSSProperties = null;
        this.prefixingXHTML = false;
        this.prefixingMathML = false;
        this.prefixingSnuggleXML = false;
        this.xhtmlPrefix = DEFAULT_XHTML_PREFIX;
        this.mathMLPrefix = DEFAULT_MATHML_PREFIX;
        this.snuggleXMLPrefix = DEFAULT_SNUGGLETEX_XML_PREFIX;

        this.mathVariantMapping = false;
        this.linkResolver = null;
    }
    
    public ErrorOutputOptions getErrorOutputOptions() {
        return errorOutputOptions;
    }
    
    public void setErrorOutputOptions(ErrorOutputOptions errorOptions) {
        if (errorOptions==null) {
            throw new IllegalArgumentException("ErrorOutputOptions must not be null");
        }
        this.errorOutputOptions = errorOptions;
    }
    
    
    public boolean isInliningCSS() {
        return inliningCSS;
    }
    
    public void setInliningCSS(boolean inliningCSS) {
        this.inliningCSS = inliningCSS;
    }
    
    
    public Properties getInlineCSSProperties() {
        return inlineCSSProperties;
    }
    
    public void setInlineCSSProperties(Properties inlineCSSProperties) {
        this.inlineCSSProperties = inlineCSSProperties;
    }


    public boolean isAddingMathAnnotations() {
        return addingMathAnnotations;
    }
    
    public void setAddingMathAnnotations(boolean addingMathAnnotations) {
        this.addingMathAnnotations = addingMathAnnotations;
    }
    
    
    public boolean isPrefixingXHTML() {
        return prefixingXHTML;
    }
    
    public void setPrefixingXHTML(boolean prefixingXHTML) {
        this.prefixingXHTML = prefixingXHTML;
    }

    
    public String getXHTMLPrefix() {
        return xhtmlPrefix;
    }

    
    public void setXHTMLPrefix(String xhtmlPrefix) {
        if (!XMLUtilities.isXMLNCName(xhtmlPrefix)) {
            throw new IllegalArgumentException("XHTML prefix must be a valid NCName");
        }
        this.xhtmlPrefix = xhtmlPrefix;
    }

    public boolean isPrefixingMathML() {
        return prefixingMathML;
    }
    

    public void setPrefixingMathML(boolean prefixingMathML) {
        this.prefixingMathML = prefixingMathML;
    }


    public String getMathMLPrefix() {
        return mathMLPrefix;
    }

    public void setMathMLPrefix(String mathMLPrefix) {
        if (!XMLUtilities.isXMLNCName(mathMLPrefix)) {
            throw new IllegalArgumentException("MathML prefix must be a valid NCName");
        }
        this.mathMLPrefix = mathMLPrefix;
    }
    
    
    public boolean isPrefixingSnuggleXML() {
        return prefixingSnuggleXML;
    }
    
    public void setPrefixingSnuggleXML(boolean prefixingSnuggleXML) {
        this.prefixingSnuggleXML = prefixingSnuggleXML;
    }

    
    public String getSnuggleXMLPrefix() {
        return snuggleXMLPrefix;
    }

    public void setSnuggleXMLPrefix(String snuggleXMLPrefix) {
        if (!XMLUtilities.isXMLNCName(snuggleXMLPrefix)) {
            throw new IllegalArgumentException("SnuggleTeX XML prefix must be a valid NCName");
        }
        this.snuggleXMLPrefix = snuggleXMLPrefix;
    }


    public boolean isMathVariantMapping() {
        return mathVariantMapping;
    }

    public void setMathVariantMapping(boolean mathVariantMapping) {
        this.mathVariantMapping = mathVariantMapping;
    }
    
    
    public DOMPostProcessor[] getDOMPostProcessors() {
        return domPostProcessors;
    }
    
    public void setDOMPostProcessors(DOMPostProcessor... domPostProcessors) {
        this.domPostProcessors = domPostProcessors;
    }
    
    public void addDOMPostProcessors(DOMPostProcessor... domPostProcessors) {
        this.domPostProcessors = ObjectUtilities.concat(this.domPostProcessors, domPostProcessors, DOMPostProcessor.class);
    }


    public LinkResolver getLinkResolver() {
        return linkResolver;
    }

    public void setLinkResolver(LinkResolver linkResolver) {
        this.linkResolver = linkResolver;
    }


    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new SnuggleLogicException(e);
        }
    }
}
