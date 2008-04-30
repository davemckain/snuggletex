/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.FrozenSlice;
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
        FrozenSlice verbContentSlice = token.getArguments()[0].getSlice();
        Element verbatimElement = builder.appendXHTMLElement(parentElement, "tt");
        verbatimElement.setAttribute("class", "verb");
        builder.appendTextNode(verbatimElement, verbContentSlice.extract().toString(), false);
    }
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token) {
        FrozenSlice verbatimContentSlice = token.getContent().getSlice();
        Element verbatimElement = builder.appendXHTMLElement(parentElement, "pre");
        verbatimElement.setAttribute("class", "verbatim");
        builder.appendTextNode(verbatimElement, verbatimContentSlice.extract().toString(), false);
    }
}
