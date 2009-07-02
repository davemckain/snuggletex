/* $Id: MathOperatorInterpretation.java 435 2009-07-02 10:29:17Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Represents a generic Mathematical operator, associating it with the content of 
 * the resulting MathML <tt>mo</tt> element.
 * 
 * @see MathBracketInterpretation
 * @see MathNegatableInterpretation
 * 
 * @author  David McKain
 * @version $Revision: 435 $
 */
public final class MathOperatorInterpretation implements MathInterpretation {
    
    private final String mathmlOperatorContent;
    
    public MathOperatorInterpretation(final String mathmlOperatorContent) {
        this.mathmlOperatorContent = mathmlOperatorContent;
    }
    
    public String getMathMLOperatorContent() {
        return mathmlOperatorContent;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_OPERATOR;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
