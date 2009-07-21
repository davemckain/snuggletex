/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

/**
 * Interface representing a code for an Error caused by client input.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public interface ErrorCode {
    
    /** Short name for this code, used to format error messages. */
    String getName();

    /** {@link SnugglePackage} defining this code */
    SnugglePackage getPackage();

}