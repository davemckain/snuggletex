/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * @author  David McKain
 * @version $Revision$
 */
public class MathOperatorInterpretation implements MathInterpretation {
    
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
