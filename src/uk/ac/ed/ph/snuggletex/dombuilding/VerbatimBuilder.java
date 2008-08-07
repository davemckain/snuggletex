/* $Id:VerbatimBuilder.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.Element;

/**
 * Handles the <tt>verbatim</tt> environment and <tt>\\verb<//tt> command.
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class VerbatimBuilder implements CommandHandler, EnvironmentHandler {
    
    /** Set to handled 'starred' variants, e.g <tt>\\verb*</tt> */
    private final boolean starred;
    
    public VerbatimBuilder(final boolean starred) {
        this.starred = starred;
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token) {
        String verbContent = token.getArguments()[0].getSlice().extract().toString();
        if (starred) {
            verbContent = verbContent.replace(' ', '\u2423'); /* Convert spaces to open boxes */
        }
        else {
            verbContent = verbContent.replace(' ', '\u00a0'); /* Convert spaces to non-breaking spaces */
        }
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
