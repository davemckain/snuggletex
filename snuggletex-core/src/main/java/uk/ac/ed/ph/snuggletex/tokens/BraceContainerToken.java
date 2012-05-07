/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
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
 * The {@link TokenFixer} strips these out when in PARAGRAPH mode as they're only used to
 * delimit stylings. They are kept in for MATH and LR mode since they are used as implicit
 * brackets.
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
