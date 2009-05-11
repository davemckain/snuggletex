/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Handler for the custom <tt>\\literal</tt> command, which allows literal input to be
 * entered, similar to <tt>\\verb</tt> with output without changing style.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class LiteralHandler implements CommandHandler {

    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        builder.handleTokens(parentElement, token.getArguments()[0], false);
    }
}
