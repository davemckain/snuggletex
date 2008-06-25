/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.aardvark.commons.util.ArrayListStack;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.MathVariantMap;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * This handles changes to the current "mathvariant" caused by things like <tt>\\mathcal</tt>
 * and friends.
 * <p>
 * Because these commands' argument may consist of multiple tokens, we manage the application
 * of these "styles" within {@link DOMBuilder} so all this handler does is push/pop a variant
 * to the appropriate Stack.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class MathVariantMapHandler implements CommandHandler {
	
    /** Resulting {@link MathVariantMap} */
	private final MathVariantMap characterMap;
	
	public MathVariantMapHandler(final MathVariantMap characterMap) {
		this.characterMap = characterMap;
	}
	
	public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
			throws SnuggleParseException {
		ArrayListStack<MathVariantMap> mathVariantMapStack = builder.getMathVariantMapStack();
		
		mathVariantMapStack.push(characterMap);
		builder.handleTokens(parentElement, token.getArguments()[0], true);
		mathVariantMapStack.pop();
	}

}
