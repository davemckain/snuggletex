/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.WorkingDocument;

/**
 * Read-only view of the current document being parsed, which will change shape as user-defined
 * macros and environments are being expanded.
 * <p>
 * This is used by helper classes like {@link NumberMatcher} that let you change the way certain
 * things get parsed.
 * <p>
 * The actual implementation of this is the (internal and very complicated) {@link WorkingDocument}
 * class.
 * 
 * @since 1.3.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface InputContext {

    /** Accesses the underlying {@link SnuggleInput} */
    SnuggleInput getInput();

    /** Current length of this input */
    int length();

    /** 
     * Returns the character at the given index in the input document,
     * or -1 if the index is not contained within the document.
     */
    int charAt(final int index);

    /**
     * Tests whether the character at the given index is equal to that specified.
     */
    boolean matchesAt(final int index, final char c);

    /**
     * Tests whether the characters at the given index are equal to the String specified.
     */
    boolean matchesAt(final int index, final String s);

    /**
     * Returns the first index of the given character, starting the search at the given
     * index. Returns -1 if not found. 
     */
    int indexOf(final int startSearchIndex, final char c);

    /**
     * Returns the first index of the given String, starting the search at the given
     * index. Returns -1 if not found. 
     */
    int indexOf(final int startSearchIndex, final String s);

    /**
     * Tests whether all characters in the document with index satisfying
     * startIndex <= index < endIndex are whitespace.
     */
    boolean isRegionWhitespace(final int startIndex, final int endIndex);

    /**
     * Extracts a copy of the current entire content of the input as a {@link CharSequence} 
     */
    CharSequence extract();

    /**
     * Extracts a copy of a slice of the current entire content of the input as a {@link CharSequence},
     * including all characters with startIndex <= index < endIndex. 
     */
    CharSequence extract(final int startIndex, final int endIndex);

}