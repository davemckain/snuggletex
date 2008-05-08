/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * Represents a mathematical operator representing a bracket, allowing brackets to be matched.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathBracketOperatorInterpretation implements MathOperatorInterpretation {
    
    private final MathMLOperator operator;
    private final MathMLOperator partnerOperator;
    private final boolean isOpener;
    
    public MathBracketOperatorInterpretation(final MathMLOperator operator,
            final MathMLOperator partnerOperator, final boolean isOpener) {
        this.operator = operator;
        this.partnerOperator = partnerOperator;
        this.isOpener = isOpener;
    }
    
    public MathMLOperator getOperator() {
        return operator;
    }
    
    public MathMLOperator getPartnerOperator() {
        return partnerOperator;
    }
    
    public boolean isOpener() {
        return isOpener;
    }

    public InterpretationType getType() {
        return InterpretationType.MATH_BRACKET_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
