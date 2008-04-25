/* $Id$
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
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public class MrowBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException, SnuggleParseException {
        Element mrow = builder.appendMathMLElement(parentElement, "mrow");
        builder.handleTokens(mrow, token.getArguments()[0], false);
    }
}
