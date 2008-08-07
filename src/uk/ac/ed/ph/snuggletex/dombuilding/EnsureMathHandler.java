/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * This handles the <tt>\\ensuremath</tt> command. This is implemented by simply delegating
 * to {@link MathEnvironmentBuilder} to start a new (inline) MathML element if we're not
 * already inside MathML.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class EnsureMathHandler implements CommandHandler {
    
    private final MathEnvironmentBuilder mathEnvironmentBuilder;
    
    public EnsureMathHandler() {
        this.mathEnvironmentBuilder = new MathEnvironmentBuilder();
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        ArgumentContainerToken contentToken = token.getArguments()[0];
        if (builder.isBuildingMathMLIsland()) {
            /* Already doing MathML so we just descend as normal */
            builder.handleTokens(parentElement, contentToken, false);
        }
        else {
            /* Not doing MathML yet so need to open a <math/>.
             * 
             * To do this, we simply delegate to MathEnvironmentBuilder
             */
            mathEnvironmentBuilder.buildMathElement(builder, parentElement, token, contentToken, false);
        }
    }

}
