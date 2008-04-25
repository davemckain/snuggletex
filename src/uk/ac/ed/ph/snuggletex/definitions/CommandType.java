/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * FIXME: Document this type!
 * 
 * @author  David McKain
 * @version $Revision$
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