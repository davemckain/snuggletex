/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;

/**
 * Base class for {@link Token}s which represent the main "flow" of content, rather than things
 * like command arguments.
 *
 * @author  David McKain
 * @version $Revision$
 */
public abstract class FlowToken extends Token {
    
    protected final TextFlowContext textFlowContext;

    public FlowToken(final FrozenSlice slice, final TokenType type, final LaTeXMode latexMode,
            final TextFlowContext textFlowContext) {
        super(slice, type, latexMode);
        this.textFlowContext = textFlowContext;
    }
    
    public FlowToken(final FrozenSlice slice, final TokenType type, final LaTeXMode latexMode,
            final Interpretation interpretation, final TextFlowContext textFlowContext) {
        super(slice, type, latexMode, interpretation);
        this.textFlowContext = textFlowContext;
    }
    
    /**
     * For {@link Token}s appearing inside TEXT flow, this indicates how this Token should flow against
     * its siblings.
     * <p>
     * For other types of {@link Token}, this returns null.
     */
    public TextFlowContext getTextFlowContext() {
        return textFlowContext;
    }
}
