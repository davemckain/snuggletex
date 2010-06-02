/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.MathCharacter;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;

/**
 * Represents a character in math mode.
 * 
 * @see TokenType
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathCharacterToken extends FlowToken {
    
    private final MathCharacter mathCharacter;
    
    public MathCharacterToken(final FrozenSlice slice, final MathCharacter mathCharacter) {
        super(slice, TokenType.MATH_CHARACTER, LaTeXMode.MATH, null, mathCharacter.getInterpretationMap());
        this.mathCharacter = mathCharacter;
    }
    
    @Override
    public MathCharacter getMathCharacter() {
        return mathCharacter;
    }
}
