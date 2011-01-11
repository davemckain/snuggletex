/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;
import uk.ac.ed.ph.snuggletex.internal.TokenFixer;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumperOptions;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a literal braced section in the original LaTeX document, i.e. something like
 * <tt>{ ... }</tt>.
 * <p>
 * All instances of these tokens will have disappeared once the {@link TokenFixer} has run.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class BraceContainerToken extends FlowToken implements Iterable<FlowToken>  {
    
    private final List<FlowToken> contents;

    public BraceContainerToken(final FrozenSlice slice, final LaTeXMode latexMode,
            final List<FlowToken> contents) {
        super(slice, TokenType.BRACE_CONTAINER, latexMode, null);
        this.contents = contents;
    }

    @ObjectDumperOptions(DumpMode.DEEP)
    public List<FlowToken> getContents() {
        return contents;
    }
    
    public Iterator<FlowToken> iterator() {
        return contents.iterator();
    }
}
