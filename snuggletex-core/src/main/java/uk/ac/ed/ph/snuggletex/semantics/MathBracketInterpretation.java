/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Represents a mathematical bracket, specifying additional information on how it
 * can be paired up.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathBracketInterpretation implements MathInterpretation {
    
    public static enum BracketType {
        OPENER,
        CLOSER,
        OPENER_OR_CLOSER
    }
    
    private final MathMLOperator operator;
    private final MathMLOperator partnerOperator;
    private final BracketType bracketType;
    
    /**
     * Flag denoting whether to allow pairs of brackets of this type to be inferred during
     * Token Fixing. This is not always safe for things like angle brackets, which also
     * mean less than or greater than.
     */
    private final boolean pairingInferencePossible;
    
    public MathBracketInterpretation(final MathMLOperator operator,
            final MathMLOperator partnerOperator, final BracketType bracketType,
            final boolean pairingInferencePossible) {
        this.operator = operator;
        this.partnerOperator = partnerOperator;
        this.bracketType = bracketType;
        this.pairingInferencePossible = pairingInferencePossible;
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
    
    public boolean isPairingInferencePossible() {
        return pairingInferencePossible;
    }

    public InterpretationType getType() {
        return InterpretationType.MATH_BRACKET;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
