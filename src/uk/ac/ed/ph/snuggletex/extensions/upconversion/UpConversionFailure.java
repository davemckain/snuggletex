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
 * This is the equivalent of an {@link InputError} for the up-conversion process,
 * indicating that a particular island of Presentation MathML created by SnuggleTeX
 * could not be up-converted to either Content MathML or Maxima input form. This
 * will be due to one of the following reasons:
 * <ul>
 *   <li>
 *     The MathML falls outside the scope of what is supported by the up-conversion
 *     process. (I.e. too complex)
 *   </li>
 *   <li>
 *     The MathML is deemed to make no sense. (This is, of course, subjective as it
 *     assumes the MathML is entered in a particular common form so one might reasonably
 *     argue that this is just a special case of the last reason!)
 *   </li>
 * </ul>
 *   
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConversionFailure implements Serializable {
    
    private static final long serialVersionUID = -7888377912814662998L;
    
    /** Error code */
    private final ErrorCode errorCode;
    
    /** Any additional arguments about the error. These are interpolated into error messages */
    private final Object[] arguments;
    
    /** Context within the MathML that the error occurred, as a serialized DOM subtree */
    private final String context;
    
    public UpConversionFailure(final ErrorCode errorCode, final String context, final Object[] arguments) {
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
