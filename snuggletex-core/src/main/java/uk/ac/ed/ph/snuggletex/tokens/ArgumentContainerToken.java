/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.definitions.Command;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.Environment;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.internal.FrozenSlice;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumperOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This token is used as a container for the arguments specified for a particular
 * {@link Command} or {@link Environment}.
 * 
 * @author  David McKain
 * @version $Revision$
 */
@ObjectDumperOptions(DumpMode.DEEP)
public final class ArgumentContainerToken extends Token implements Iterable<FlowToken> {
    
    public static final ArgumentContainerToken[] EMPTY_ARRAY = new ArgumentContainerToken[0];
    
    private final List<FlowToken> contents;
    
    public ArgumentContainerToken(final FrozenSlice slice, final LaTeXMode latexMode,
            final List<FlowToken> contents) {
        super(slice, TokenType.ARGUMENT_CONTAINER, latexMode);
        this.contents = contents;
    }
    
    public ArgumentContainerToken(final FrozenSlice slice, final LaTeXMode latexMode,
            final List<FlowToken> contents, final ComputedStyle computedStyle) {
        super(slice, TokenType.ARGUMENT_CONTAINER, latexMode);
        this.contents = contents;
        this.computedStyle = computedStyle;
    }
    
    public static ArgumentContainerToken createFromSingleToken(final LaTeXMode latexMode,
            final FlowToken content) {
        return createFromSingleToken(latexMode, content, content.getComputedStyle());
    }
    
    public static ArgumentContainerToken createFromSingleToken(final LaTeXMode latexMode,
            final FlowToken content, final ComputedStyle computedStyle) {
        List<FlowToken> contentList = new ArrayList<FlowToken>();
        contentList.add(content);
        return new ArgumentContainerToken(content.getSlice(), latexMode, contentList, computedStyle);
    }
    
    public static ArgumentContainerToken createFromContiguousTokens(final Token parentToken,
            final LaTeXMode latexMode, final List<? extends FlowToken> contents,
            final ComputedStyle computedStyle) {
        return createFromContiguousTokens(parentToken, latexMode, contents, 0, contents.size(), computedStyle);
    }
    
    public static ArgumentContainerToken createFromContiguousTokens(final Token parentToken,
            final LaTeXMode latexMode, final List<? extends FlowToken> contents,
            final int startIndex, final int endIndex, final ComputedStyle computedStyle) {
        if (startIndex>endIndex) {
            throw new IllegalArgumentException("startIndex must be <= endIndex");
        }
        ArgumentContainerToken result;
        if (contents.size()>0) {
            FrozenSlice startSlice = contents.get(startIndex).getSlice();
            FrozenSlice endSlice = contents.get(endIndex-1).getSlice();
            FrozenSlice resultSlice = startSlice.rightOuterSpan(endSlice);
            
            result = new ArgumentContainerToken(resultSlice, latexMode,
                    new ArrayList<FlowToken>(contents.subList(startIndex, endIndex)),
                    computedStyle);
        }
        else {
            result = createEmptyContainer(parentToken, latexMode, computedStyle);
        }
        return result;
    }
    
    public static ArgumentContainerToken createEmptyContainer(final Token parentToken, 
            final LaTeXMode latexMode, final ComputedStyle computedStyle) {
        List<FlowToken> emptyTokens = Collections.emptyList();
        return new ArgumentContainerToken(parentToken.getSlice(), latexMode, emptyTokens, computedStyle);
    }
    
    @ObjectDumperOptions(DumpMode.DEEP)
    public List<FlowToken> getContents() {
        return contents;
    }
    
    public Iterator<FlowToken> iterator() {
        return contents.iterator();
    }
}
