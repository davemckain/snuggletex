/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.commons.util.ObjectUtilities;

/**
 * Semantic interpretation for a mathematical identifier.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathIdentifierInterpretation implements MathInterpretation {
    
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
