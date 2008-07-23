/* $Id: SnuggleTeXConfiguration.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.util.Properties;

/**
 * This class is used to specify how you want DOM trees to be built when calling relevant methods
 * in {@link SnuggleTeXSession} (e.g. {@link SnuggleTeXSession#buildDOMSubtree(org.w3c.dom.Element)}
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public class DOMBuilderOptions implements Cloneable {
    
    /**
     * Enumerates the various options for representing {@link InputError}s in the resulting
     * DOM.
     */
    public static enum ErrorOutputOptions {
        
        /**
         * No error information is appended to the DOM. (Clients can still get at error information
         * via {@link SnuggleTeXSession#getErrors()}.)
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
    
    /** Set to true to include TeX comments in the output */
    private boolean includingComments;
    
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
     * Set to true if you want MathML element names to be prefixed. If false, then the default
     * namespace is changed on each MathML element.
     */
    private boolean prefixingMathML;
    
    /**
     * Prefix to use when prefixing MathML element names.
     */
    private String mathMLPrefix;
    
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
     * Set to true to attempt to "down-convert" simple MathML expressions into (X)HTML equivalents.
     * For example, simple linear expressions and simple sub/superscripts can often be converted
     * to acceptable XHTML alternatives.
     * <p>
     * Any expressions deemed too complex to be down-converted are kept as MathML.
     * 
     * <strong>NOTE:</strong> This is an experimental new feature. Feedback welcome!!
     */
    private boolean downConverting;
    
    public DOMBuilderOptions() {
        this.errorOutputOptions = ErrorOutputOptions.NO_OUTPUT;
        this.inliningCSS = false;
        this.includingComments = false;
        this.addingMathAnnotations = false;
        this.inlineCSSProperties = null;
        this.mathMLPrefix = "m";
        this.prefixingMathML = false;
        this.mathVariantMapping = false;
        this.downConverting = false;
    }
    
    public ErrorOutputOptions getErrorOptions() {
        return errorOutputOptions;
    }
    
    public void setErrorOptions(ErrorOutputOptions errorOptions) {
        if (errorOptions==null) {
            throw new IllegalArgumentException("ErrorOutputOptions must not be null");
        }
        this.errorOutputOptions = errorOptions;
    }
    
    
    public boolean isIncludingComments() {
        return includingComments;
    }
    
    public void setIncludingComments(boolean includingComments) {
        this.includingComments = includingComments;
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
    
    
    public boolean isPrefixingMathML() {
        return prefixingMathML;
    }
    
    public void setPrefixingMathML(boolean prefixingMathML) {
        this.prefixingMathML = prefixingMathML;
    }


    public String getMathMLPrefix() {
        return mathMLPrefix;
    }

    /**
     * FIXME: This doesn't currently check that the prefix is a valid NCName!
     * 
     * @param mathMLPrefix
     */
    public void setMathMLPrefix(String mathMLPrefix) {
        if (mathMLPrefix==null || mathMLPrefix.length()==0) {
            throw new IllegalArgumentException("MathML prefix must be at least 1 character long");
        }
        this.mathMLPrefix = mathMLPrefix;
    }
    

    public boolean isMathVariantMapping() {
        return mathVariantMapping;
    }

    public void setMathVariantMapping(boolean mathVariantMapping) {
        this.mathVariantMapping = mathVariantMapping;
    }
    
    
	public boolean isDownConverting() {
		return downConverting;
	}

	public void setDownConverting(boolean downConverting) {
		this.downConverting = downConverting;
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
