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
