/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Represents a generic Mathematical operator.
 * 
 * @see MathBracketInterpretation
 * @see MathRelationInterpretation
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathOperatorInterpretation implements MathInterpretation {
    
    private final MathMLOperator operator;
    
    public MathOperatorInterpretation(final MathMLOperator operator) {
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
