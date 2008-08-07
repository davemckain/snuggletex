/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Needed for things like <tt>\ldots</tt>, which works in both Math and Text modes
 *
 * @author  David McKain
 * @version $Revision$
 */
public class TextSafeInterpretableMathIdentifierBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token) {
        if (token.getLatexMode()==LaTeXMode.MATH) {
            /* Do normal MathML thing */
            builder.appendSimpleMathElement(parentElement, token);
        }
        else {
            /* Just append what would have been the <mi/> content */
            MathIdentifierInterpretation interpretation = (MathIdentifierInterpretation) token.getInterpretation();
            builder.appendTextNode(parentElement, interpretation.getName(), false);
        }
    }


}
