/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

/**
 * Parameters controlling the up-conversion process in {@link MathMLUpConverter}.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public interface UpConversionParameters {
    
    /**
     * Set to {@link Boolean#TRUE} if you want to convert to Content MathML,
     * {@link Boolean#FALSE} otherwise.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String DO_CONTENT_MATHML = "do-content-mathml";
    
    /**
     * Set to {@link Boolean#TRUE} if you want to convert to Maxima code,
     * {@link Boolean#FALSE} otherwise.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String DO_MAXIMA = "do-maxima";
    
    /**
     * Set to {@link Boolean#TRUE} if you want the MathML identifier 'e' to be
     * treated as the exponential number e.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String ASSUME_EXPONENTIAL_E = "assume-exponential-e";
    
    /**
     * Set to {@link Boolean#TRUE} if you want the MathML identifier 'i' to correspond
     * to the imaginary number i.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String ASSUME_IMAGINARY_I = "assume-imaginary-i";
    
    /**
     * Set to {@link Boolean#TRUE} if you want the MathML identifier corresponding to the
     * Unicode character for 'pi' to be treated as the usual number.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String ASSUME_CONSTANT_PI = "assume-constant-pi";
    
    /**
     * Set to {@link Boolean#TRUE} if you want a comma-separated fence bound with matching
     * braces to be treated as a set.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String ASSUME_BRACES_SET = "assume-braces-set";
    
    /**
     * Set to {@link Boolean#TRUE} if you want a comma-separated fence bound with matching
     * square brackets to be treated as a list.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String ASSUME_SQUARE_LIST = "assume-square-list";
}
