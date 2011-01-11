/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.RootToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;

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
        visitToken(rootToken);
    }
    
    //-----------------------------------------
    
    private void visitToken(Token token) {
        /* Then descend as appropriate */
        switch (token.getType()) {
            case ROOT:
                RootToken rootToken = (RootToken) token;
                visitSiblings(rootToken.getContents(), rootToken.getComputedStyle());
                break;
                
            case COMMAND:
                visitCommand((CommandToken) token);
                break;
                
            case ENVIRONMENT:
                visitEnvironment((EnvironmentToken) token);
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
    
    private void visitSiblings(List<FlowToken> content, ComputedStyle inScopeStyle) {
        ComputedStyle currentStyle = inScopeStyle;
        for (int i=0; i<content.size(); i++) {
            FlowToken token = content.get(i);
            if (!token.getComputedStyle().isEquivalentTo(currentStyle)) {
                /* Token is changing style, so let's group this and subsequent tokens in the
                 * same style together
                 */
                List<FlowToken> tokenGroup = new ArrayList<FlowToken>();
                tokenGroup.add(token);
                int endIndex = i+1;
                END_SEARCH: for (; endIndex<content.size(); endIndex++) {
                    FlowToken followingToken = content.get(endIndex);
                    if (token.getComputedStyle().isEquivalentTo(followingToken.getComputedStyle())) {
                        tokenGroup.add(followingToken);
                    }
                    else {
                        break END_SEARCH;
                    }
                }
                ArgumentContainerToken contentToken = ArgumentContainerToken.createFromContiguousTokens(token, token.getLatexMode(), tokenGroup, token.getComputedStyle());
                EnvironmentToken replacement = createStyleEnvironmentToken(token, contentToken);
                content.set(i, replacement);
                content.subList(i+1, endIndex).clear();
            }
            
            /* Record new style for next run */
            currentStyle = token.getComputedStyle();
        }
        
        /* Visit each sub-token */
        for (FlowToken token : content) {
            visitToken(token);
        }
        
        /* Trim underlying ArrayLists once fixed up to use as little memory as possible */
        if (content instanceof ArrayList) {
            ((ArrayList<FlowToken>) content).trimToSize();
        }
    }
    
    private EnvironmentToken createStyleEnvironmentToken(FlowToken source, ArgumentContainerToken contentToken) {
        EnvironmentToken result = new EnvironmentToken(source.getSlice(),
                source.getLatexMode(),
                CorePackageDefinitions.ENV_STYLE,
                contentToken);
        result.setComputedStyle(contentToken.getComputedStyle());
        return result;
    }
    
    private void visitCommand(CommandToken commandToken) {
        ArgumentContainerToken optArgument = commandToken.getOptionalArgument();
        if (optArgument!=null) {
            visitArgumentContainer(optArgument);
        }
        ArgumentContainerToken[] arguments = commandToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitArgumentContainer(argument);
            }
        }
        /* (Currently not doing anything to combiner targets, as I don't think it's needed) */
    }

    private void visitEnvironment(EnvironmentToken environmentToken) {
        ArgumentContainerToken optArgument = environmentToken.getOptionalArgument();
        if (optArgument!=null) {
            visitArgumentContainer(optArgument);
        }
        ArgumentContainerToken[] arguments = environmentToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitArgumentContainer(argument);
            }
        }
        visitArgumentContainer(environmentToken.getContent());
    }
    
    private void visitArgumentContainer(ArgumentContainerToken token) {
        visitSiblings(token.getContents(), token.getComputedStyle());
    }
}
