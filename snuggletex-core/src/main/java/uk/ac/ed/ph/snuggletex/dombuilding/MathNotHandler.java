/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathNegatableInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles the <tt>\\not</tt> combiner command.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathNotHandler implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken notToken) {
        List<FlowToken> combinerContent = notToken.getCombinerTarget().getContents();
        if (combinerContent.size()!=1) {
            throw new SnuggleLogicException("Expeted combiner of \\not to contain exactly 1 Token");
        }
        MathNegatableInterpretation combinerInterpretation = (MathNegatableInterpretation) combinerContent
            .get(0)
            .getInterpretation(InterpretationType.MATH_NEGATABLE);
        if (combinerInterpretation==null) {
            throw new SnuggleLogicException("Expeted combiner of \\not to have a " + InterpretationType.MATH_NEGATABLE + " Interpretation");
        }
        builder.appendMathCharacter(parentElement, combinerInterpretation.getNegatedCharacter());
    }
}
