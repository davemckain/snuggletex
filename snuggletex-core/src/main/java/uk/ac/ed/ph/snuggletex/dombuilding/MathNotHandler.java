/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathNegatableInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Handles the <tt>\\not</tt> combiner command.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathNotHandler implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken notToken) {
        MathNegatableInterpretation combinerInterpretation = (MathNegatableInterpretation) notToken.getCombinerTarget().getInterpretation(InterpretationType.MATH_NEGATABLE);
        if (combinerInterpretation==null) {
            throw new SnuggleLogicException("Expeted combiner of \\not to have a " + InterpretationType.MATH_NEGATABLE + " Interpretation");
        }
        builder.appendMathMLOperatorElement(parentElement, combinerInterpretation.getMathMLNegatedOperatorContent());
    }

}
