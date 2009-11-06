/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

/**
 * FIXME: Document this type!
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class IllegalUpconversionOptionException extends IllegalArgumentException {
    
    private static final long serialVersionUID = 784979608771641983L;

    private final UpConversionErrorCode errorCode;
    
    private final String[] arguments;

    public IllegalUpconversionOptionException(final UpConversionErrorCode errorCode, final String... arguments) {
        super(MessageFormatter.getErrorMessage(errorCode, (Object[]) arguments));
        this.errorCode = errorCode;
        this.arguments = arguments;
    }

    public UpConversionErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String[] getArguments() {
        return arguments;
    }
}
