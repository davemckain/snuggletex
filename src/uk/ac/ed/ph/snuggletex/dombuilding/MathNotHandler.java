/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationOrBracketOperatorInterpretation;
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
        Interpretation combinerInterpretation = notToken.getCombinerTarget().getInterpretation();
        InterpretationType combinerType = combinerInterpretation.getType();
        MathMLOperator notOperator;
        if (combinerType==InterpretationType.MATH_RELATION_OPERATOR) {
            notOperator = ((MathRelationOperatorInterpretation) combinerInterpretation).getNotOperator();
        }
        else if (combinerType==InterpretationType.MATH_RELATION_OR_BRACKET_OPERATOR) {
            notOperator = ((MathRelationOrBracketOperatorInterpretation) combinerInterpretation).getNotOperator();
        }
        else {
            throw new SnuggleLogicException("Unexpected logic branch - combinerType is " + combinerType);
        }
        builder.appendMathMLOperatorElement(parentElement, notOperator);
    }

}
