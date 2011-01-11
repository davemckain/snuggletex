/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import org.w3c.dom.Element;

/**
 * Handles things like the <tt>\\underline</tt> command, which are simpler than the classic
 * LaTeX style controls.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class TextClassHandler implements CommandHandler {
    
    private final String cssClassName;
    
    public TextClassHandler(final String cssClassName) {
        this.cssClassName = cssClassName;
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        if (builder.isBuildingMathMLIsland()) {
            throw new SnuggleLogicException("This handler is not intended for use in MathML islands");
        }
        
        /* Is the content block or inline? */
        ArgumentContainerToken contentContainerToken = token.getArguments()[0];
        boolean hasBlockContent = false;
        for (FlowToken contentToken : contentContainerToken) {
            if (contentToken.getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK) {
                hasBlockContent = true;
            }
        }
        
        Element result = builder.appendXHTMLElement(parentElement, hasBlockContent ? "div" : "span");
        builder.applyCSSStyle(result, cssClassName);
        builder.handleTokens(result, contentContainerToken, false);
    }
}
