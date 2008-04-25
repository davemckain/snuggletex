/* $Id$
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

/**
 * Enumerates the different options that can be used for outputting property values in
 * {@link ObjectUtilities#beanToString(Object)}.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public enum PropertyOptions {

    /** Property should be left out completely from toString() results */
    IGNORE_PROPERTY,
    
    /** 
     * Value of property will be omitted, but property will be shown in toString() if non-null
     * to indicate its presence.
     */
    HIDE_VALUE,
    
    /**
     * The default option, this shows the property value in full glory, expanding Arrays and
     * calling {@link Object#toString()} on Objects.
     */
    SHOW_FULL,
    
    ;
}
