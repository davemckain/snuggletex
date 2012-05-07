/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Trivial "meta"-builder that delegates to a {@link CommandHandler} depending on whether we
 * are in MATH or TEXT mode.
 * <p>
 * This is useful for things like <tt>\\underline</tt>
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class ModeDelegatingHandler implements CommandHandler {

    private final CommandHandler textModeBuilder;
    private final CommandHandler mathModeBuilder;
    
    public ModeDelegatingHandler(final CommandHandler textModeBuilder, final CommandHandler mathModeBuilder) {
        this.textModeBuilder = textModeBuilder;
        this.mathModeBuilder = mathModeBuilder;
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        if (builder.isBuildingMathMLIsland()) {
            mathModeBuilder.handleCommand(builder, parentElement, token);
        }
        else {
            textModeBuilder.handleCommand(builder, parentElement, token);
        }
    }
}
