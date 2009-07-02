/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Supplementary interpretation for an operator which may be negated. This provides information
 * about the corresponding negation operator.
 * <p>
 * You will want to assign a {@link MathOperatorInterpretation} as well to specify the "positive"
 * operator.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathNegatableInterpretation implements MathInterpretation {
    
    private final String mathmlNegatedOperatorContent;
    
    public MathNegatableInterpretation(final String mathmlNegatedOperatorContent) {
        this.mathmlNegatedOperatorContent = mathmlNegatedOperatorContent;
    }
    
    public String getMathMLNegatedOperatorContent() {
        return mathmlNegatedOperatorContent;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_NEGATABLE;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
