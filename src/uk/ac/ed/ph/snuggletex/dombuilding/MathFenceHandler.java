/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.TokenFixer;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationOrBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles matched parentheses encapsulated within {@link GlobalBuiltins#ENV_BRACKETED}
 * environments. These have either been explicitly specified with <tt>\\left</tt> and
 * <tt>\\right</tt> or have been inferred by the {@link TokenFixer}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathFenceHandler implements EnvironmentHandler {
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        /* Create <mfenced> element with correct attributes */
        ArgumentContainerToken contentContainer = token.getContent();
        String opener = getBracket(token.getArguments()[0]);
        String closer = getBracket(token.getArguments()[1]);
        if (opener!=null && closer!=null) {
            /* Have explicit opener and closer so can make a proper <mfenced/> */
            makeMfenced(builder, parentElement, contentContainer, opener, closer);
        }
        else {
            /* Spec says we've not to do <mfenced/> if missing an opener or closer so we
             * revert to long form here.
             */
            makeBracketed(builder, parentElement, contentContainer, opener, closer);
        }
    }
    
    private void makeMfenced(DOMBuilder builder, Element parentElement,
            ArgumentContainerToken contentContainer, String opener, String closer)
            throws SnuggleParseException {
        /* Create <mfenced/> operator */
        Element mfenced = builder.appendMathMLElement(parentElement, "mfenced");
        mfenced.setAttribute("open", StringUtilities.emptyIfNull(opener));
        mfenced.setAttribute("close", StringUtilities.emptyIfNull(closer));
        
        /* Now add contents, grouping on comma operators */
        List<FlowToken> groupBuilder = new ArrayList<FlowToken>();
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
    
    private void makeBracketed(DOMBuilder builder, Element parentElement,
            ArgumentContainerToken contentContainer, String opener, String closer)
            throws SnuggleParseException {
        /* We'll be putting everything in an <mrow/> here */
        Element mrow = builder.appendMathMLElement(parentElement, "mrow");
        
        /* Maybe do an open bracket */
        if (opener!=null) {
            builder.appendMathMLOperatorElement(mrow, opener);
        }
        /* Then add contents as-is */
        for (FlowToken contentToken : contentContainer) {
            builder.handleToken(mrow, contentToken);
        }
        /* Finally, maybe do a close bracket */
        if (closer!=null) {
            builder.appendMathMLOperatorElement(mrow, closer);
        }
    }
    
    private void makeFenceGroup(DOMBuilder builder, Element mfenced, List<FlowToken> groupContents)
            throws SnuggleParseException {
        builder.handleMathTokensAsSingleElement(mfenced, groupContents);
    }
    
    private String getBracket(ArgumentContainerToken argumentContainerToken) {
        /* Ensure fence endpoint is either nothing or a single bracket */
        List<FlowToken> contents = argumentContainerToken.getContents();
        if (!contents.isEmpty()) {
            FlowToken bracketToken = contents.get(0);
            if (bracketToken.isInterpretationType(InterpretationType.MATH_BRACKET_OPERATOR)) {
                return ((MathBracketOperatorInterpretation) bracketToken.getInterpretation()).getOperator().getOutput();
            }
            else if (bracketToken.isInterpretationType(InterpretationType.MATH_RELATION_OR_BRACKET_OPERATOR)) {
                return ((MathRelationOrBracketOperatorInterpretation) bracketToken.getInterpretation()).getOperator().getOutput();
            }
            /* (Since this token is created only during token fixing, the following is a logic
             * failure rather than a client error.)
             */
            throw new SnuggleLogicException("Expected to find a single bracket operator, or empty container");
        }
        return null;
    }
}
