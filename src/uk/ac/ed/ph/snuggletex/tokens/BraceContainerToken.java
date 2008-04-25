/* $Id: BraceContainerToken.java,v 1.4 2008/04/14 10:48:25 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.conversion.FrozenSlice;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.4 $
 */
public final class BraceContainerToken extends FlowToken {
    
    private final ArgumentContainerToken braceContent;

    public BraceContainerToken(FrozenSlice slice, LaTeXMode latexMode, ArgumentContainerToken braceContent) {
        super(slice, TokenType.BRACE_CONTAINER, latexMode, null);
        this.braceContent = braceContent;
    }

    public ArgumentContainerToken getBraceContent() {
        return braceContent;
    }
}
