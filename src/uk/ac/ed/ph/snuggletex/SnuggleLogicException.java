/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;


/**
 * This {@link RuntimeException} is thrown when something unexpected happens that is <em>not</em>
 * the fault of the client.
 * 
 * <h2>Developer Note</h2>
 * 
 * Throw this Exception when your code does silly things. (E.g. unexpected switch case, impossible
 * state.) Raise an error using {@link SnuggleTeXSession#registerError(InputError)} if the error
 * is down to bad client input.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleLogicException extends RuntimeException {

    private static final long serialVersionUID = -8544806081557772449L;

    public SnuggleLogicException() {
        super();
    }

    public SnuggleLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnuggleLogicException(String message) {
        super(message);
    }

    public SnuggleLogicException(Throwable cause) {
        super(cause);
    }
}
