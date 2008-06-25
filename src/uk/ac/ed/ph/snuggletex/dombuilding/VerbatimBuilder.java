/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.Element;

/**
 * Handles the <tt>verbatim</tt> environment and <tt>\\verb<//tt> command.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class VerbatimBuilder implements CommandHandler, EnvironmentHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token) {
        String verbContent = token.getArguments()[0].getSlice().extract().toString()
            .replace(' ', '\u00a0'); /* Convert spaces to non-breaking spaces */
        Element verbatimElement = builder.appendXHTMLElement(parentElement, "tt");
        verbatimElement.setAttribute("class", "verb");
        builder.appendTextNode(verbatimElement, verbContent, false);
    }
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token) {
        String verbatimContent = token.getContent().getSlice().extract().toString();
        Element verbatimElement = builder.appendXHTMLElement(parentElement, "pre");
        verbatimElement.setAttribute("class", "verbatim");
        builder.appendTextNode(verbatimElement, verbatimContent, false);
    }
}
