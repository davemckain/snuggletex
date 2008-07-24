/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles fenced MathML operators, specified via <tt>\\left</tt> and <tt>\\right</tt> or
 * via inferred fencing of brackets.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MfenceBuilder implements EnvironmentHandler {
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        /* Create <mfenced> element with correct attributes */
        Element mfenced = builder.appendMathMLElement(parentElement, "mfenced");
        MathMLOperator opener = getBracket(token.getArguments()[0]);
        MathMLOperator closer = getBracket(token.getArguments()[1]);
        if (opener==null || closer==null) {
            /* Fence endpoints must be single bracket operators */
            builder.appendOrThrowError(parentElement, token, ErrorCode.TDEM00);
            return;
        }
        mfenced.setAttribute("open", opener.getOutput());
        mfenced.setAttribute("close", closer.getOutput());
        
        /* Now add contents, grouping on comma operators */
        List<FlowToken> groupBuilder = new ArrayList<FlowToken>();
        ArgumentContainerToken contentContainer = token.getContent();
        for (FlowToken contentToken : contentContainer) {
            if (contentToken.isInterpretationType(InterpretationType.MATH_OPERATOR)
                    && ((SimpleMathOperatorInterpretation) contentToken.getInterpretation()).getOperator()==MathMLOperator.COMMA) {
                /* Found a comma, so add Node based on what's been found so far */
                makeFenceGroup(builder, mfenced, groupBuilder);
                groupBuilder.clear();
            }
            else {
                /* Add to group */
                groupBuilder.add(contentToken);
            }
        }
        /* Deal with what's left in the group, if appropriate */
        if (!groupBuilder.isEmpty()) {
            makeFenceGroup(builder, mfenced, groupBuilder);
        }
    }
    
    private void makeFenceGroup(DOMBuilder builder, Element mfenced, List<FlowToken> groupContents)
            throws SnuggleParseException {
        builder.handleMathTokensAsSingleElement(mfenced, groupContents);
    }
    
    private MathMLOperator getBracket(ArgumentContainerToken argumentContainerToken) {
        List<FlowToken> contents = argumentContainerToken.getContents();
        if (contents.size()==1) {
            FlowToken bracketToken = contents.get(0);
            if (bracketToken.isInterpretationType(InterpretationType.MATH_BRACKET_OPERATOR)) {
                return ((MathBracketOperatorInterpretation) bracketToken.getInterpretation()).getOperator();
            }
        }
        return null;
    }
}
