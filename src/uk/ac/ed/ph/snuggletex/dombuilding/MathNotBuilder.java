/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.NottableMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Handles the <tt>\\not</tt> combiner command.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathNotBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken notToken) {
        NottableMathOperatorInterpretation combinerRelation = (NottableMathOperatorInterpretation) notToken.getCombinerTarget().getInterpretation();
        builder.appendMathMLOperatorElement(parentElement, combinerRelation.getNotOperator());
    }

}
