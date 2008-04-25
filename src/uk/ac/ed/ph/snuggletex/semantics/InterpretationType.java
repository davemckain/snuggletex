/* $Id: InterpretationType.java,v 1.2 2008/01/17 13:58:00 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

/**
 * Defines the different types of interpretations handled by the {@link Interpretation} class
 * hierarchy.
 * 
 * @author  David McKain
 * @version $Revision: 1.2 $
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
