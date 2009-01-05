/* $Id:InterpretationType.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

/**
 * Defines the different types of interpretations handled by the {@link Interpretation} class
 * hierarchy.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public enum InterpretationType {
    
    /* Text */
    STYLE_DECLARATION,
    
    /* Math */
    MATH_NUMBER,
    MATH_IDENTIFIER,
    MATH_FUNCTION_IDENTIFIER,
    MATH_OPERATOR,
    MATH_RELATION_OPERATOR,
    MATH_BRACKET_OPERATOR,
    
    ;

}
