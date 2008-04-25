/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.tokens;

/**
 * @author  David McKain
 * @version $Revision$
 */
public enum TokenType {
    
    /** Main types, correspond to a subclass of {@link Token} */
    ARGUMENT_CONTAINER,
    BRACE_CONTAINER,
    ENVIRONMENT,
    COMMAND,
    ERROR,

    /* Simple tokens */
    COMMENT,
    NEW_PARAGRAPH, /* (This is stripped out during fixing) */
    TAB_CHARACTER, /* (a.k.a. &, as used in tables and arrays) */
    TEXT_MODE_TEXT,
    LR_MODE_NEW_PARAGRAPH, /* (This is substituted in during fixing - not entered directly) */
    VERBATIM_MODE_TEXT,
    MATH_NUMBER,
    SINGLE_CHARACTER_MATH_IDENTIFIER,
    SINGLE_CHARACTER_MATH_SPECIAL,
    
    ;

}
