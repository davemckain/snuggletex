/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.commons.util.ObjectUtilities;

/**
 * Represents a mathematical operator representing a bracket, allowing brackets to be matched.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathBracketOperatorInterpretation implements MathOperatorInterpretation {
    
    public static enum BracketType {
        OPENER,
        CLOSER,
        OPENER_OR_CLOSER
    }
    
    private final MathMLOperator operator;
    private final MathMLOperator partnerOperator;
    private final BracketType bracketType;
    
    public MathBracketOperatorInterpretation(final MathMLOperator operator,
            final MathMLOperator partnerOperator, final BracketType bracketType) {
        this.operator = operator;
        this.partnerOperator = partnerOperator;
        this.bracketType = bracketType;
    }
    
    public MathMLOperator getOperator() {
        return operator;
    }
    
    public MathMLOperator getPartnerOperator() {
        return partnerOperator;
    }
    
    public BracketType getBracketType() {
        return bracketType;
    }

    public InterpretationType getType() {
        return InterpretationType.MATH_BRACKET_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
