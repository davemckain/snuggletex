/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationInterpretation;
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
        MathRelationInterpretation combinerInterpretation = (MathRelationInterpretation) notToken.getCombinerTarget().getInterpretation(InterpretationType.MATH_RELATION);
        if (combinerInterpretation==null) {
            throw new SnuggleLogicException("Expeted combiner of \\not to have a " + InterpretationType.MATH_RELATION + " Interpretation");
        }
        MathMLOperator notOperator = combinerInterpretation.getNotOperator();
        builder.appendMathMLOperatorElement(parentElement, notOperator);
    }

}
