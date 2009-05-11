/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation.BracketType;

/**
 * Special variant of {@link MathRelationOperatorInterpretation} for the 'less than' and
 * 'greater than' symbols, which may also act as brackets!
 * 
 * FIXME: This smells like a mixin... perhaps it's worth refactoring these classes a bit?
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathRelationOrBracketOperatorInterpretation implements MathOperatorInterpretation {
    
    private final MathMLOperator operator;
    private final MathMLOperator partnerOperator;
    private final MathMLOperator notOperator;
    
    private final BracketType bracketType;
    
    public MathRelationOrBracketOperatorInterpretation(final MathMLOperator operator,
            final MathMLOperator partnerOperator, final MathMLOperator notOperator,
            final BracketType bracketType) {
        this.operator = operator;
        this.partnerOperator = partnerOperator;
        this.notOperator = notOperator;
        this.bracketType = bracketType;
    }
    
    public MathMLOperator getOperator() {
        return operator;
    }
    
    public MathMLOperator getPartnerOperator() {
        return partnerOperator;
    }
    
    public MathMLOperator getNotOperator() {
        return notOperator;
    }

    public BracketType getBracketType() {
        return bracketType;
    }

    public InterpretationType getType() {
        return InterpretationType.MATH_RELATION_OR_BRACKET_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
