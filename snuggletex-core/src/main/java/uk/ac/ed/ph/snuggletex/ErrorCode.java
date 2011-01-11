/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.CoreErrorCode;

/**
 * Interface representing a code for an Error caused by client input.
 * <p>
 * (This is an interface rather than a class as it's convenient for "providers"
 * of error codes to create an enumeration that implements this interface.
 * This is how {@link CoreErrorCode} works in the SnuggleTeX Core module.)
 * 
 * @see CoreErrorCode
 * @see ErrorGroup
 * @see SnugglePackage
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface ErrorCode {
    
    /** 
     * Short name for this code, used as a key when formatting error messages via
     * {@link SnugglePackage#getErrorMessageBundle()}
     */
    String getName();
    
    /** {@link ErrorGroup} owning this code */
    ErrorGroup getErrorGroup();

}