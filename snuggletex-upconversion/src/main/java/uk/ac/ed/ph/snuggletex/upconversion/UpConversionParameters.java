/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

/**
 * Parameters controlling the up-conversion process in {@link MathMLUpConverter}.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public interface UpConversionParameters {
    
    /**
     * Set to {@link Boolean#TRUE} to add an annotation to each MathML element showing
     * what assumptions were being made at that particular point.
     * <p>
     * Default: {@link Boolean#FALSE}.
     */
    String SHOW_ASSUMPTIONS = "show-assumptions";
    
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
     * round brackets and containing 2 or more child elements to be treated as a vector.
     * <p>
     * Default: {@link Boolean#TRUE}.
     */
    String ASSUME_BRACKETS_VECTOR = "assume-brackets-vector";
    
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
    
    /**
     * Specifies the name of a Maxima function to use when faced with unapplied operators.
     * <p>
     * Default: operator
     */
    String MAXIMA_OPERATOR_FUNCTION = "maxima-operator-function";
    
    /**
     * Specifies the name of a Maxima function to use when representing units.
     * <p>
     * Default: operator
     */
    String MAXIMA_UNITS_FUNCTION = "maxima-units-function";
}
