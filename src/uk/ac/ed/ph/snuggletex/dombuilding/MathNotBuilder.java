/* $Id: MathNotBuilder.java,v 1.2 2008/01/16 16:59:29 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public final class MathNotBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken notToken)
            throws DOMException {
        MathRelationOperatorInterpretation combinerRelation = (MathRelationOperatorInterpretation) notToken.getCombinerTarget().getInterpretation();
        builder.appendMathMLOperatorElement(parentElement, combinerRelation.getNotOperator());
    }

}
