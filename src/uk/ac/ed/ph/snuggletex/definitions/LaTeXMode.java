/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * Enumerates the various modes of LaTeX input.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public enum LaTeXMode {
    
    /** 
     * The default LaTeX PARAGRAPH Mode, representing free-flowing textual content.
     * Parsing always starts in this mode.
     */
    PARAGRAPH,
    
    /** 
     * LaTeX LR (left-to-right) Mode, used in things like <tt>\\mbox</tt> and friends
     */
    LR,
    
    /** 
     * LaTeX MATH Mode, entered from PARAGRAPH or LR mode via commands such as <tt>\[</tt>
     * or by certain explicit MATH environments.
     */
    MATH,

    /**
     * This isn't really a proper LaTeX parsing mode, but we use it here whenever we need to
     * pull in raw text until a given terminator is found. The content may be multi-line;
     * the <tt>\\verb</tt> command does not allow multi-line text so does its own further checks on
     * the content.
     */
    VERBATIM,
    
    ;
}
