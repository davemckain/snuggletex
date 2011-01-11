/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.CoreErrorCode;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.TokenType;

import java.util.List;

import org.w3c.dom.Element;

/**
 * This handles LaTeX list environments (i.e. <tt>itemize</tt> and <tt>enumerate</tt>).
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class ListEnvironmentHandler implements EnvironmentHandler, CommandHandler {
    
    /**
     * Builds the actual List environment
     */
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        String listElementName = null;
        BuiltinEnvironment environment = token.getEnvironment();
        if (environment==CorePackageDefinitions.ENV_ITEMIZE) {
            listElementName = "ul";
        }
        else if (environment==CorePackageDefinitions.ENV_ENUMERATE) {
            listElementName = "ol";
        }
        else {
            throw new SnuggleLogicException("No logic to handle list environment " + environment.getTeXName());
        }
        Element listElement = builder.appendXHTMLElement(parentElement, listElementName);
        handleListContent(builder, parentElement, listElement, token.getContent().getContents(), null);
    }
    
    private void handleListContent(DOMBuilder builder, Element parentElement, Element listElement,
            List<FlowToken> content, ComputedStyle style)
            throws SnuggleParseException {
        for (FlowToken token : content) {
            if (token.isCommand(CorePackageDefinitions.CMD_LIST_ITEM)) {
                /* Good list item */
                handleListItem(builder, listElement, (CommandToken) token, style);
            }
            else if (token.isEnvironment(CorePackageDefinitions.ENV_STYLE)) {
                /* Style element, which will wrap up one or more list items */
                EnvironmentToken styleToken = (EnvironmentToken) token;
                ComputedStyle innerStyle = styleToken.getComputedStyle();
                handleListContent(builder, parentElement, listElement, styleToken.getContent().getContents(), innerStyle);
            }
            else if (token.getType()==TokenType.ERROR) {
                /* We'll append errors immediately *after* the list element */
                builder.handleToken(parentElement, token);
            }
            else {
                /* List environments should only contain list items. This should have
                 * been sorted at token fixing so we've got a logic fault if we get here!
                 */
                throw new SnuggleLogicException("List environments can only contain list items - this should have been handled earlier");
            }
        }
    }
    
    private void handleListItem(DOMBuilder builder, Element listElement, CommandToken itemToken,
            ComputedStyle overriddenStyle) throws SnuggleParseException {
        Element listItemElement = builder.appendXHTMLElement(listElement, "li");
        Element listContentContainer = listItemElement;
        if (overriddenStyle!=null) {
            listContentContainer = builder.openStyle(listItemElement, overriddenStyle, true, false);
        }
        builder.handleTokens(listContentContainer, itemToken.getArguments()[0], true);
        if (overriddenStyle!=null) {
            builder.closeStyle();
        }
    }
    
    /* (List items are handled above, so anything matching here is an error.) */
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken itemToken)
            throws SnuggleParseException {
        if (itemToken.isCommand(CorePackageDefinitions.CMD_LIST_ITEM)) {
            throw new SnuggleLogicException("List item outside environment - this should not have occurred");
        }
        else if (itemToken.isCommand(CorePackageDefinitions.CMD_ITEM)) {
            /* This is a standard LaTeX \item. This would have been substituted if it was used
             * in a legal position so we must conclude that it cannot be used here.
             */
            builder.appendOrThrowError(parentElement, itemToken, CoreErrorCode.TDEL00);
        }
    }
}
