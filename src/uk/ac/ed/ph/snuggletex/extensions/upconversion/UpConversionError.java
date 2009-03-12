/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.InputError;

import java.io.Serializable;

/**
 * This is the equivalent of an {@link InputError} for the up-conversion process.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConversionError implements Serializable {
    
    private static final long serialVersionUID = -7888377912814662998L;
    
    /** Error code */
    private final ErrorCode errorCode;
    
    /** Any additional arguments about the error. These are interpolated into error messages */
    private final Object[] arguments;
    
    /** Context within the MathML that the error occurred, as a serialized DOM subtree */
    private final String context;
    
    public UpConversionError(final ErrorCode errorCode, final String context, final Object[] arguments) {
        this.errorCode = errorCode;
        this.context = context;
        this.arguments = arguments;
    }

    
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public String getContext() {
        return context;
    }
}
