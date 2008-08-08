/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles the mathematical "limit" tokens like {@link GlobalBuiltins#CMD_MSUB_OR_MUNDER}, generating
 * either <tt>msub</tt> or <tt>munder</tt> as appropriate, with analogous results for
 * {@link GlobalBuiltins#CMD_MSUP_OR_MOVER} and {@link GlobalBuiltins#CMD_MSUBSUP_OR_MUNDEROVER}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathLimitsHandler implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* Get the token to which the limit is being applied to, which is precisely the
         * first argument
         */
        List<FlowToken> limitand = token.getArguments()[0].getContents(); /* Ha! */
        
        /* Decide whether we should do a munder/mover in preference to the more common
         * msub/msup. This decision is made on whether the "limitand" is a single token
         * comprising a certain type of Math operator.
         */ 
        boolean isUnderOver = builder.getOutputContext()==OutputContext.MATHML_BLOCK
            && limitand.size()==1
            && limitand.get(0).isInterpretationType(InterpretationType.MATH_OPERATOR)
            && ((MathOperatorInterpretation) limitand.get(0).getInterpretation()).getOperator().isLimitsUnderOrOver();
        BuiltinCommand command = token.getCommand();
        String elementName;
        if (command.equals(GlobalBuiltins.CMD_MSUB_OR_MUNDER)) {
            elementName = isUnderOver ? "munder" : "msub";
        }
        else if (command.equals(GlobalBuiltins.CMD_MSUP_OR_MOVER)) {
            elementName = isUnderOver ? "mover" : "msup";
        }
        else if (command.equals(GlobalBuiltins.CMD_MSUBSUP_OR_MUNDEROVER)) {
            elementName = isUnderOver ? "munderover" : "msubsup";
        }
        else {
            throw new SnuggleLogicException("Unexpected limit command " + command);
        }
        /* And we can now just build up the MathML quite trivially */
        Element result = builder.appendMathMLElement(parentElement, elementName);
        for (ArgumentContainerToken argument : token.getArguments()) {
            builder.handleMathTokensAsSingleElement(result, argument);
        }
    }
}
