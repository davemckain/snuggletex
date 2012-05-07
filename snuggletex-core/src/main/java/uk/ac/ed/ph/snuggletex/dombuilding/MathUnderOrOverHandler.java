/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Handles the <tt>\\stackrel</tt>, <tt>\\overset</tt> and <tt>\\underset</tt> commands.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathUnderOrOverHandler implements CommandHandler {
    
    private final String mathmlContainerName;
    
    public MathUnderOrOverHandler(String mathmlContainerName) {
        this.mathmlContainerName = mathmlContainerName;
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        Element result = builder.appendMathMLElement(parentElement, mathmlContainerName);

        /* Extract the top and bottom tokens */
        ArgumentContainerToken topToken = token.getArguments()[0];
        ArgumentContainerToken bottomToken = token.getArguments()[1];
        
        /* Generate MathML content (note the change of order!) */
        builder.handleMathTokensAsSingleElement(result, bottomToken);
        builder.handleMathTokensAsSingleElement(result, topToken);
    }
}
