/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.internal.WorkingDocument;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumperOptions;

import java.util.Iterator;
import java.util.List;

/**
 * This token represents the root of a parsed {@link SnuggleInput}
 * 
 * @author  David McKain
 * @version $Revision$
 */
@ObjectDumperOptions(DumpMode.DEEP)
public final class RootToken extends Token implements Iterable<FlowToken> {
    
    private final WorkingDocument workingDocument;
    private final List<FlowToken> contents;
    
    public RootToken(final WorkingDocument workingDocument, final List<FlowToken> contents) {
        super(workingDocument.freezeSlice(0, workingDocument.length()), TokenType.ROOT, LaTeXMode.PARAGRAPH);
        this.workingDocument = workingDocument;
        this.contents = contents;
    }
    
    public WorkingDocument getWorkingDocument() {
        return workingDocument;
    }

    @ObjectDumperOptions(DumpMode.DEEP)
    public List<FlowToken> getContents() {
        return contents;
    }
    
    public Iterator<FlowToken> iterator() {
        return contents.iterator();
    }
}
