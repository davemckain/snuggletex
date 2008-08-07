/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * This builder calls back directly to the {@link DOMBuilder} to handle mathematical
 * tokens with certain types of simple semantic interpretations.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class InterpretableSimpleMathBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token) {
        builder.appendSimpleMathElement(parentElement, token);
    }
}
