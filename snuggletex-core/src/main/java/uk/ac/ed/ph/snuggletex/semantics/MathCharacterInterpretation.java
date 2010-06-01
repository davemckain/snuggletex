/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public class MathCharacterInterpretation implements MathInterpretation {
    
    public static enum CharacterType {
        ACCENT,
        ALPHA,
        BIN,
        CLOSE,
        FENCE,
        OP,
        OPEN,
        ORD,
        PUNCT,
        REL,
    }
    
    public InterpretationType getType() {
        return InterpretationType.MATH_CHARACTER;
    }
    
    private final CharacterType characterType;
    private final int codePoint;
    
    public MathCharacterInterpretation(CharacterType characterType, int codePoint) {
        this.characterType = characterType;
        this.codePoint = codePoint;
    }
    
    public CharacterType getCharacterType() {
        return characterType;
    }
    
    public int getCodePoint() {
        return codePoint;
    }
    
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
