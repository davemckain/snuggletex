/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.SnugglePackage;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumperOptions;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.MathCharacter;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathCharacterInterpretation;

import java.util.EnumMap;

/**
 * Base interface for a parsed SnuggleTeX token.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public abstract class Token {
    
    /** Extent of this entire token */
    protected final FrozenSlice slice;
    
    /** Classifies the token */
    protected final TokenType type;
    
    /** LaTeX Mode that this token was parsed in */
    protected final LaTeXMode latexMode;
    
    /** Interpretation(s) of this token, if it can be readily deduced from the input. May be null. */
    protected final EnumMap<InterpretationType, Interpretation> interpretationMap;
    
    protected Token(final FrozenSlice slice, final TokenType type, final LaTeXMode latexMode,
            final Interpretation... interpretations) {
        this.slice = slice;
        this.type = type;
        this.latexMode = latexMode;
        this.interpretationMap = SnugglePackage.makeInterpretationMap(interpretations);
    }
    
    protected Token(final FrozenSlice slice, final TokenType type, final LaTeXMode latexMode,
            final EnumMap<InterpretationType, Interpretation> interpretationMap) {
        this.slice = slice;
        this.type = type;
        this.latexMode = latexMode;
        this.interpretationMap = interpretationMap;
    }
    
    @ObjectDumperOptions(DumpMode.TO_STRING)
    public FrozenSlice getSlice() {
        return slice;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public LaTeXMode getLatexMode() {
        return latexMode;
    }

    @ObjectDumperOptions(DumpMode.TO_STRING)
    public EnumMap<InterpretationType, Interpretation> getInterpretationMap() {
        return interpretationMap;
    }
    
    public Interpretation getInterpretation(InterpretationType type) {
        return interpretationMap!=null ? interpretationMap.get(type) : null;
    }
    
    //------------------------------------------------------
    // Convenience methods

    public boolean hasInterpretationType(InterpretationType... types) {
        if (interpretationMap==null) {
            return false;
        }
        for (InterpretationType testType : types) {
            if (interpretationMap.containsKey(testType)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCommand(BuiltinCommand command) {
        return this instanceof CommandToken && ((CommandToken) this).getCommand()==command;
    }
    
    public MathCharacter getMathCharacter() {
        if (interpretationMap==null) {
            return null;
        }
        MathCharacterInterpretation mcInterpretation = (MathCharacterInterpretation) interpretationMap.get(InterpretationType.MATH_CHARACTER);
        return mcInterpretation!=null ? mcInterpretation.getMathCharacter() : null;
    }
    
    public int getMathCharacterCodePoint() {
        MathCharacter character = getMathCharacter();
        return character!=null ? character.getCodePoint() : -1;
    }
    
    //------------------------------------------------------

    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
