/* $Id: TextSafeInterpretableMathIdentifierBuilder.java,v 1.1 2008/03/20 11:10:04 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

/**
 * Needed for things like <tt>\ldots</tt>, which works in both Math and Text modes
 *
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public class TextSafeInterpretableMathIdentifierBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws DOMException {
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
