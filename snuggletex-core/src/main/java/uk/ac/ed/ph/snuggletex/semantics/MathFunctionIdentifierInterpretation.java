/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Represents a mathematical function like sin, cos, etc. These are considered
 * identifiers in MathML but need to be considered separately from other
 * identifiers.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public class MathFunctionIdentifierInterpretation implements MathInterpretation {
    
    private final String name;
    
    public MathFunctionIdentifierInterpretation(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_FUNCTION_IDENTIFIER;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
