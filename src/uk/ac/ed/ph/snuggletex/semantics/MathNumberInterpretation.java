/* $Id: MathNumberInterpretation.java,v 1.2 2008/03/18 10:34:43 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public class MathNumberInterpretation implements MathInterpretation {
    
    private final CharSequence number;
    
    public MathNumberInterpretation(final CharSequence number) {
        this.number = number;
    }
    
    public CharSequence getNumber() {
        return number;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_NUMBER;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
