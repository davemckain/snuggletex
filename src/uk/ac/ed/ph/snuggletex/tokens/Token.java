/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.commons.util.DumpMode;
import uk.ac.ed.ph.commons.util.ObjectDumperOptions;
import uk.ac.ed.ph.commons.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;

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
    
    /** Interpretation of this token, if it can be readily deduced from the input */
    protected final Interpretation interpretation;
    
    protected Token(final FrozenSlice slice, final TokenType type, final LaTeXMode latexMode) {
        this(slice, type, latexMode, null);
    }
    
    protected Token(final FrozenSlice slice, final TokenType type,
            final LaTeXMode latexMode, final Interpretation interpretation) {
        this.slice = slice;
        this.type = type;
        this.latexMode = latexMode;
        this.interpretation = interpretation;
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

    public Interpretation getInterpretation() {
        return interpretation;
    }
    
    public InterpretationType getInterpretationType() {
        return interpretation!=null ? interpretation.getType() : null;
    }

    
    //------------------------------------------------------
    // Convenience

    public boolean isInterpretationType(InterpretationType... types) {
        if (interpretation==null) {
            return false;
        }
        InterpretationType interpretationType = interpretation.getType();
        for (InterpretationType testType : types) {
            if (testType==interpretationType) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isCommand(BuiltinCommand command) {
        return this instanceof CommandToken && ((CommandToken) this).getCommand()==command;
    }

    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
