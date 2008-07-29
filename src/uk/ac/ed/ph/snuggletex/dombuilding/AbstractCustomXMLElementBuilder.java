/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.SimpleToken;
import uk.ac.ed.ph.snuggletex.tokens.TokenType;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Provides base functionality for the commands and environments that build custom
 * XML elements.
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class AbstractCustomXMLElementBuilder implements EnvironmentHandler, CommandHandler {
    
    /**
     * Subclasses should fill in to return true if they are building a block element or
     * false if they are building an inline element.
     */
    protected abstract boolean isBlock();
    
    public void handleEnvironment(final DOMBuilder builder, final Element parentElement,
            final EnvironmentToken token)
            throws SnuggleParseException {
        buildCustomElement(builder, parentElement, token.getOptionalArgument(), token.getArguments()[0],
                token.getArguments()[1], token.getContent());
    }
    
    public void handleCommand(final DOMBuilder builder, final Element parentElement,
            final CommandToken token)
            throws SnuggleParseException {
        buildCustomElement(builder, parentElement, token.getOptionalArgument(), token.getArguments()[0],
                token.getArguments()[1], token.getArguments()[2]);
    }
    
    protected void buildCustomElement(final DOMBuilder builder, final Element parentElement,
            final ArgumentContainerToken attrsToken, final ArgumentContainerToken namespaceToken,
            final ArgumentContainerToken qNameToken, final ArgumentContainerToken contentToken)
            throws SnuggleParseException {
        /* Create and append target element */
        
        String namespaceUri = builder.extractStringValue(namespaceToken);
        String qName = builder.extractStringValue(qNameToken);
        Element resultElement;
        try {
            resultElement = builder.getDocument().createElementNS(namespaceUri, qName);
        }
        catch (DOMException e) {
            if (e.code==DOMException.INVALID_CHARACTER_ERR || e.code==DOMException.NAMESPACE_ERR) {
                /* Bad QName or Namespace, but let's put the blame on the QName anyway as it's
                 * not easy to tell which one was problematic and the QName is most likely! */
                builder.appendOrThrowError(parentElement, qNameToken, ErrorCode.TDEX01,
                        qName, namespaceUri);
                return;
            }
            throw e;
        }
        parentElement.appendChild(resultElement);
        
        /* Add attributes, if provided */
        if (attrsToken!=null) {
            extractAttributes(builder, parentElement, resultElement, attrsToken);
        }
        
        /* Handle content */
        builder.handleTokens(resultElement, contentToken, isBlock());
    }
    
    private void extractAttributes(final DOMBuilder builder, final Element parentElement,
            final Element resultElement, final ArgumentContainerToken attrsToken)
            throws SnuggleParseException {
        CommandToken resolvedAttrToken;
        for (FlowToken rawAttrToken : attrsToken) {
            if (rawAttrToken.isCommand(GlobalBuiltins.CMD_XML_ATTR)) {
                resolvedAttrToken = (CommandToken) rawAttrToken;
                String namespace = builder.extractStringValue(resolvedAttrToken.getArguments()[0]);
                String qName = builder.extractStringValue(resolvedAttrToken.getArguments()[1]);
                String value = builder.extractStringValue(resolvedAttrToken.getArguments()[2]);
                resultElement.setAttributeNS(namespace, qName, value);
            }
            else if (rawAttrToken.getType()==TokenType.COMMENT) {
                /* Ignore */
            }
            else if (rawAttrToken.getType()==TokenType.ERROR) {
                /* Keep this */
                builder.handleToken(parentElement, rawAttrToken);
            }
            else if (rawAttrToken.getType()==TokenType.TEXT_MODE_TEXT && ((SimpleToken) rawAttrToken).getSlice().isWhitespace()) {
                /* Whitespace can be ignored */
            }
            else {
                /* Error: Expected a CoreEngineConfigurer.XML_ATTR token here */
                builder.appendOrThrowError(parentElement, rawAttrToken, ErrorCode.TDEX00);
            }
        }
    }
}
