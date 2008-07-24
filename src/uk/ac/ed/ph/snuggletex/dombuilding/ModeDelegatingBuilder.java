/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
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
public final class ModeDelegatingBuilder implements CommandHandler {

    private final CommandHandler textModeBuilder;
    private final CommandHandler mathModeBuilder;
    
    public ModeDelegatingBuilder(final CommandHandler textModeBuilder, final CommandHandler mathModeBuilder) {
        this.textModeBuilder = textModeBuilder;
        this.mathModeBuilder = mathModeBuilder;
    }
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        if (token.getLatexMode()==LaTeXMode.MATH) {
            mathModeBuilder.handleCommand(builder, parentElement, token);
        }
        else {
            textModeBuilder.handleCommand(builder, parentElement, token);
        }
    }
}
