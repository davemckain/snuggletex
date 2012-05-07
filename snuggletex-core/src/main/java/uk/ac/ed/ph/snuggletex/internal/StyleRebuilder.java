/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.BraceContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.RootToken;

import java.util.ArrayList;
import java.util.List;

/**
 * This runs after {@link TokenFixer} and groups style changes together using the
 * pseudo-environment {@link CorePackageDefinitions#ENV_STYLE}, which makes DOM building
 * much easier.
 * 
 * @see LaTeXTokeniser
 * @see StyleEvaluator
 * @see TokenFixer
 * @see SnuggleSession#parseInput(uk.ac.ed.ph.snuggletex.SnuggleInput)
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class StyleRebuilder {
    
    @SuppressWarnings("unused")
    private final SessionContext sessionContext;
    
    public StyleRebuilder(final SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
    
    //-----------------------------------------

    public void rebuildStyles(RootToken rootToken) {
        visitSiblings(rootToken.getContents(), rootToken.getComputedStyle());
    }
    
    //-----------------------------------------
    
    private void visitSiblings(List<FlowToken> content, ComputedStyle inScopeStyle) {
        ComputedStyle currentStyle = inScopeStyle;
        for (int i=0; i<content.size(); i++) {
            FlowToken token;
            token = content.get(i);
            if (token.hasInterpretationType(InterpretationType.STYLE_SENTINEL)) {
                /* This token has been tagged with a special STYLE_SENTINEL interpretation.
                 * Examples are tabular, <tr> and <td> and list items.
                 * 
                 * In this case, we ignore the style of the token and descend downwards, keeping
                 * the current style.
                 * 
                 * This means that on style changes, we end up with a tree of the form
                 * 
                 * tr
                 * + td
                 *   + style
                 *   
                 * instead of
                 * 
                 * style
                 * + tr
                 *   + td
                 *   
                 * which makes DOM building easier.
                 */
                visitToken(token, inScopeStyle);
            }
            else {
                if (!token.getComputedStyle().isEquivalentTo(currentStyle)) {
                    /* Token is changing style, so let's group this and subsequent tokens together,
                     * stopping when we get to a style change or a STYLE_SENTINEL.
                     */
                    List<FlowToken> tokenGroup = new ArrayList<FlowToken>();
                    tokenGroup.add(token);
                    int endIndex = i+1;
                    END_SEARCH: for (; endIndex<content.size(); endIndex++) {
                        FlowToken followingToken = content.get(endIndex);
                        if (!followingToken.hasInterpretationType(InterpretationType.STYLE_SENTINEL)
                                && token.getComputedStyle().isEquivalentTo(followingToken.getComputedStyle())) {
                            tokenGroup.add(followingToken);
                        }
                        else {
                            break END_SEARCH;
                        }
                    }
                    EnvironmentToken replacement = createStyleEnvironmentToken(token, tokenGroup);
                    content.set(i, replacement);
                    content.subList(i+1, endIndex).clear();
                    token = replacement;
                }
                
                /* Record new style for next token */
                currentStyle = token.getComputedStyle();
                
                /* Now visit token (and into its descendants) using its computed style */
                visitToken(token, currentStyle);
            }
        }
        
        /* Trim underlying ArrayLists once fixed up to use as little memory as possible */
        if (content instanceof ArrayList) {
            ((ArrayList<FlowToken>) content).trimToSize();
        }
    }
    
    private void visitToken(FlowToken token, ComputedStyle currentStyle) {
        /* Then descend as appropriate */
        switch (token.getType()) {
            case COMMAND:
                visitCommand((CommandToken) token, currentStyle);
                break;
                
            case ENVIRONMENT:
                visitEnvironment((EnvironmentToken) token, currentStyle);
                break;
                
            case BRACE_CONTAINER:
                visitSiblings(((BraceContainerToken) token).getContents(), currentStyle);
                break;
                
            case TEXT_MODE_TEXT:
            case VERBATIM_MODE_TEXT:
            case LR_MODE_NEW_PARAGRAPH:
            case MATH_NUMBER:
            case MATH_CHARACTER:
            case ERROR:
            case TAB_CHARACTER:
            case NEW_PARAGRAPH:
                /* Nothing to do here */
                break;
                
            default:
                throw new SnuggleLogicException("Unhandled/unexpected TokenType " + token.getType());
        }
    }
    
    private EnvironmentToken createStyleEnvironmentToken(FlowToken source, List<FlowToken> tokenGroup) {
        ArgumentContainerToken contentToken = ArgumentContainerToken.createFromContiguousTokens(source, source.getLatexMode(), tokenGroup, source.getComputedStyle());
        return createStyleEnvironmentToken(source, contentToken);
    }
    
    private EnvironmentToken createStyleEnvironmentToken(FlowToken source, ArgumentContainerToken contentToken) {
        EnvironmentToken result = new EnvironmentToken(source.getSlice(),
                source.getLatexMode(),
                CorePackageDefinitions.ENV_STYLE,
                contentToken);
        result.setComputedStyle(contentToken.getComputedStyle());
        return result;
    }
    
    private void visitCommand(CommandToken commandToken, ComputedStyle currentStyle) {
        ArgumentContainerToken optArgument = commandToken.getOptionalArgument();
        if (optArgument!=null) {
            visitArgumentContainer(commandToken, optArgument, currentStyle);
        }
        ArgumentContainerToken[] arguments = commandToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitArgumentContainer(commandToken, argument, currentStyle);
            }
        }
        /* (Currently not doing anything to combiner targets, as I don't think it's needed) */
    }

    private void visitEnvironment(EnvironmentToken environmentToken, ComputedStyle currentStyle) {
        ArgumentContainerToken optArgument = environmentToken.getOptionalArgument();
        if (optArgument!=null) {
            visitArgumentContainer(environmentToken, optArgument, currentStyle);
        }
        ArgumentContainerToken[] arguments = environmentToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitArgumentContainer(environmentToken, argument, currentStyle);
            }
        }
        visitArgumentContainer(environmentToken, environmentToken.getContent(), currentStyle);
    }
    
    private void visitArgumentContainer(FlowToken owner, ArgumentContainerToken token, ComputedStyle currentStyle) {
        List<FlowToken> contents = token.getContents();
        if (owner.hasInterpretationType(InterpretationType.STYLE_SENTINEL)) {
            visitSiblings(contents, currentStyle);
        }
        else {
            visitSiblings(contents, token.getComputedStyle());
        }
    }
}
