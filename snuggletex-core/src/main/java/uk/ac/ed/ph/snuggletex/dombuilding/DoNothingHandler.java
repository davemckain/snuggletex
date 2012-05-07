/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.Element;

/**
 * Trivial "do nothing" handler for commands and environments that don't add anything to
 * the resulting DOM. (E.g. <tt>\\newcommand</tt> doesn't make any visible output.)
 *
 * @author  David McKain
 * @version $Revision$
 */
public class DoNothingHandler implements CommandHandler, EnvironmentHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token) {
        /* Do nothing */
    }
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token) {
        /* Do nothing */
    }
}
