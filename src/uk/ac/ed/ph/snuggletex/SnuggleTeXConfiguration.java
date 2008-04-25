/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;


import java.util.Properties;

/**
 * FIXME: Document this type!
 * 
 * FIXME: Will need to be able to clone this if we allows run-time
 * options for overriding the configuration as we're allowing configs
 * to be reused for different sessions.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleTeXConfiguration implements Cloneable {
    
    public static enum ErrorOptions {
        FAIL_FAST,
        LIST,
        LIST_AND_XML_SHORT,
        LIST_AND_XML_FULL
    }
    
    private boolean inferringMathStructure;
    private boolean addingMathAnnotations;
    private boolean includingComments;
    private boolean inliningCSS;
    private Properties inlineCSSProperties;
    private ErrorOptions errorOptions;
    
    /**
     * Prefix for MathML Qualified Names. Null or empty is treated as "no prefix" (default)
     */
    private String mathMLPrefix;
    
    public SnuggleTeXConfiguration() {
        this.errorOptions = ErrorOptions.LIST_AND_XML_FULL;
        this.inferringMathStructure = false;
        this.inliningCSS = false;
        this.includingComments = false;
        this.addingMathAnnotations = false;
        this.inlineCSSProperties = null;
        this.mathMLPrefix = null;
    }
    
    public ErrorOptions getErrorOptions() {
        return errorOptions;
    }
    
    public void setErrorOptions(ErrorOptions errorOptions) {
        this.errorOptions = errorOptions;
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


    public boolean isInferringMathStructure() {
        return inferringMathStructure;
    }
    
    public void setInferringMathStructure(boolean inferringMathStructure) {
        this.inferringMathStructure = inferringMathStructure;
    }

    
    public boolean isAddingMathAnnotations() {
        return addingMathAnnotations;
    }
    
    public void setAddingMathAnnotations(boolean addingMathAnnotations) {
        this.addingMathAnnotations = addingMathAnnotations;
    }
    

    public String getMathMLPrefix() {
        return mathMLPrefix;
    }
    
    public void setMathMLPrefix(String mathMLPrefix) {
        this.mathMLPrefix = mathMLPrefix;
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
