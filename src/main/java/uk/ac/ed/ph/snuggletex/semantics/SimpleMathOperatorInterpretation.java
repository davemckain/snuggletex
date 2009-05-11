/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.commons.util.ObjectUtilities;

/**
 * Represents a generic Mathematical operator.
 * 
 * @see MathBracketOperatorInterpretation
 * @see MathRelationOperatorInterpretation
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class SimpleMathOperatorInterpretation implements MathOperatorInterpretation {
    
    private final MathMLOperator operator;
    
    public SimpleMathOperatorInterpretation(final MathMLOperator operator) {
        this.operator = operator;
    }
    
    public MathMLOperator getOperator() {
        return operator;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
