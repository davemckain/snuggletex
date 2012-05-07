/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;

import java.util.EnumMap;

/**
 * Represents a "simple" LaTeX token. See {@link TokenType} for
 * the sorts of things this covers.
 * 
 * @see TokenType
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class SimpleToken extends FlowToken {
    
    public SimpleToken(final FrozenSlice slice, final TokenType tokenType, final LaTeXMode latexMode,
            final TextFlowContext context, final Interpretation... interpretations) {
        super(slice, tokenType, latexMode, context, interpretations);
    }

    public SimpleToken(final FrozenSlice slice, final TokenType tokenType, final LaTeXMode latexMode,
            final TextFlowContext context, final EnumMap<InterpretationType, Interpretation> interpretations) {
        super(slice, tokenType, latexMode, context, interpretations);
    }
}
