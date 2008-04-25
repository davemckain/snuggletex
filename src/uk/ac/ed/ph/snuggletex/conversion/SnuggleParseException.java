/* $Id: SnuggleParseException.java,v 1.1 2008/04/18 09:44:05 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;

/**
 * Internal Exception thrown by {@link SnuggleTeXSession#registerError(InputError)} when a parsing
 * error occurs and the client has requested immediate failure in such circumstances.
 *
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public final class SnuggleParseException extends Exception {

    private static final long serialVersionUID = -4623002490712268496L;
    
    private final InputError error;
    
    public SnuggleParseException(InputError error) {
        super(error.getErrorCode().name());
        this.error = error;
    }

    public InputError getError() {
        return error;
    }
}
