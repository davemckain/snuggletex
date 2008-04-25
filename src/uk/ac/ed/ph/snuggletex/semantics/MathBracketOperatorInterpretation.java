/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * Special Math operator representing a bracket. This allows brackets to be matched
 * if required.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MathBracketOperatorInterpretation extends MathOperatorInterpretation {
    
    private final MathMLOperator partnerOperator;
    private final boolean isOpener;
    
    public MathBracketOperatorInterpretation(MathMLOperator operator, MathMLOperator partnerOperator, boolean isOpener) {
        super(operator);
        this.partnerOperator = partnerOperator;
        this.isOpener = isOpener;
    }
    
    public MathMLOperator getPartnerOperator() {
        return partnerOperator;
    }
    
    public boolean isOpener() {
        return isOpener;
    }

    @Override
    public InterpretationType getType() {
        return InterpretationType.MATH_BRACKET_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
