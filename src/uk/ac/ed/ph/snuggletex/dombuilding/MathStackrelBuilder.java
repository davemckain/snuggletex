/* $Id: MathComplexCommandBuilder.java 12 2008-05-06 21:17:06Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles the <tt>\\stackrel</tt> command.
 *
 * @author  David McKain
 * @version $Revision: 12 $
 */
public final class MathStackrelBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException, SnuggleParseException {
        Element result = builder.appendMathMLElement(parentElement, "mover");

        /* Extract the top and bottom tokens */
        ArgumentContainerToken topToken = token.getArguments()[0];
        ArgumentContainerToken bottomToken = token.getArguments()[1];
        
        /* Generate MathML content (note the change of order!) */
        builder.handleMathTokensAsSingleElement(result, bottomToken);
        builder.handleMathTokensAsSingleElement(result, topToken);
    }
}
