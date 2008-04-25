/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;

/**
 * Special token encapsulating an error in parsing.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ErrorToken extends FlowToken {
    
    private final InputError error;

    public ErrorToken(final InputError error, final LaTeXMode latexMode) {
        super(error.getSlice(), TokenType.ERROR, latexMode, TextFlowContext.START_NEW_XHTML_BLOCK);
        this.error = error;
    }

    public InputError getError() {
        return error;
    }
}
