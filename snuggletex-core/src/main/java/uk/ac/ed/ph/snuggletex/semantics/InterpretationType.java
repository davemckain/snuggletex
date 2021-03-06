/* Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.definitions.CommandOrEnvironment;
import uk.ac.ed.ph.snuggletex.tokens.Token;

/**
 * Defines the different types of interpretations handled by the {@link Interpretation} class
 * hierarchy.
 * <p>
 * Note that a {@link Token} or {@link CommandOrEnvironment} may have more than one interpretation,
 * but no more than one of each type listed here.
 * 
 * @author  David McKain
 */
public enum InterpretationType {
    
    /* Text interpretations */
    STYLE_DECLARATION,
    
    /* Principal Math Interpretations. These ones are mutually exclusive */
    MATH_NUMBER,
    MATH_IDENTIFIER,
    MATH_FUNCTION,
    MATH_OPERATOR,
    
    /* The next ones are "supplementary" in that they generally accompany one of the above
     * Math Interpretations as well.
     */
    MATH_NEGATABLE,
    MATH_BRACKET,
    MATH_BIG_LIMIT_OWNER,
    
    /* These can be applied to both Text and Math stuff. (Generally only environments) */
    TABULAR
    
    ;
}
