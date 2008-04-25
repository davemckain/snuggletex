/* $Id: MboxBuilder.java,v 1.6 2008/04/23 11:23:36 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles <tt>\mbox</tt>, <tt>\fbox</tt> and friends.
 *
 * @author  David McKain
 * @version $Revision: 1.6 $
 */
public final class BoxBuilder implements CommandHandler {
    
    private final String xhtmlClassName;
    
    public BoxBuilder(final String xhtmlClassName) {
        this.xhtmlClassName = xhtmlClassName;
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException, SnuggleParseException {
        /* We just descend into contents - \mbox doesn't actually "do" anything to the output
         * though its children will output different things because of a combination of being
         * in LR mode and the XML application the parent element belongs.
         */
        Element containerElement;
        if (builder.isBuildingMathMLIsland(parentElement)) {
            containerElement = builder.appendMathMLElement(parentElement, "mrow");
        }
        else {
            containerElement = builder.appendXHTMLElement(parentElement, "span");
            builder.applyCSSStyle(containerElement, xhtmlClassName);
        }
        builder.handleTokens(containerElement, token.getArguments()[0], true);
    }

}
