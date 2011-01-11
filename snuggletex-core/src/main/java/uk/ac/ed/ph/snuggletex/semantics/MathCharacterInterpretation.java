/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.definitions.MathCharacter;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * Represents a mathematical character
 *
 * @author  David McKain
 * @version $Revision$
 */
public class MathCharacterInterpretation implements MathInterpretation {
    
    public InterpretationType getType() {
        return InterpretationType.MATH_CHARACTER;
    }
    
    private final MathCharacter mathCharacter;
    
    public MathCharacterInterpretation(MathCharacter mathCharacter) {
        this.mathCharacter = mathCharacter;
    }
    
    public MathCharacter getMathCharacter() {
        return mathCharacter;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
