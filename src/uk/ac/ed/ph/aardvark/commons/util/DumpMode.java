/* $Id: DumpMode.java,v 1.1 2008/01/14 10:54:06 dmckain Exp $
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

/**
 * Enumerates the different options that can be used when specifying how to dump
 * out properties or types in {@link ObjectDumper}.
 * <p>
 * They are listed in order of verbosity.
 * 
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public enum DumpMode {
    
    /**
     * Ignores the given property.
     */
    IGNORE,

    /**
     * Calls {@link Object#toString()} on the given type or property.
     */
    TO_STRING,
    
    /**
     * Uses {@link ObjectDumper} to do a deep dump of the given type or property.
     */
    DEEP,
    
    ;
}
