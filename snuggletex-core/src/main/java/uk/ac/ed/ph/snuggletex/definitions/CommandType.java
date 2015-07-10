/* Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * Enumerates different types of LaTeX commands.
 * 
 * @author  David McKain
 */
public enum CommandType {
    
    /** Takes no arguments */
    SIMPLE,
    
    /** Takes no arguments, absorbs the following token */
    COMBINER,
    
    /** Takes arguments */
    COMPLEX,
    
    ;
}