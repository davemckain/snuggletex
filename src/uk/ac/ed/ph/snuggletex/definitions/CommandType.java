/* $Id: CommandType.java,v 1.2 2008/01/16 16:59:29 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

/**
 * FIXME: Document this type!
 * 
 * @author  David McKain
 * @version $Revision: 1.2 $
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