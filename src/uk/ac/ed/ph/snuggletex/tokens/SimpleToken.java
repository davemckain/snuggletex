/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.conversion.FrozenSlice;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;

/**
 * FIXME: Document this!
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class SimpleToken extends FlowToken {
    
    public SimpleToken(final FrozenSlice slice, final TokenType tokenType,
            final LaTeXMode latexMode, final TextFlowContext context) {
        super(slice, tokenType, latexMode, context);
    }

    public SimpleToken(final FrozenSlice slice, final TokenType tokenType, final LaTeXMode latexMode,
            final Interpretation interpretation, final TextFlowContext context) {
        super(slice, tokenType, latexMode, interpretation, context);
    }
}
