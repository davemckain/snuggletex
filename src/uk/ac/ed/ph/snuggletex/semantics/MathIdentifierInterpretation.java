/* $Id: MathIdentifierInterpretation.java,v 1.1 2008/01/14 10:54:06 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.aardvark.commons.util.ObjectUtilities;

/**
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public class MathIdentifierInterpretation implements MathInterpretation {
    
    private final String name;
    
    public MathIdentifierInterpretation(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_IDENTIFIER;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
