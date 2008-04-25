/* $Id: DoNothingHandler.java,v 1.2 2008/04/15 10:34:11 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Trivial "do nothing" handler for commands and environments that don't add anything to
 * the resulting DOM. (E.g. <tt>\\newcommand</tt> doesn't make any visible output.)
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public class DoNothingHandler implements CommandHandler, EnvironmentHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException {
        /* Do nothing */
    }
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException {
        /* Do nothing */
    }
}
