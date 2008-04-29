/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.CSSUtilities;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions;
import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.MessageFormatter;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnuggleTeX;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnvironmentHandler;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathNumberInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathRelationOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.BraceContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.ErrorToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.SimpleToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;

import java.util.List;
import java.util.Properties;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This takes a {@link List} of (fixed) {@link Token}s and builds a DOM tree branch from them.
 * 
 * <h2>Usage</h2>
 * 
 * Clients should not normally have to use this class directly -
 * call {@link SnuggleTeXSession#buildDOMSubtree(Element, DOMBuilderOptions)} and friends.
 * <p>
 * An instance of this class is intended to be used once and then discarded.
 * <p>
 * This contains a number of callbacks that {@link CommandHandler}s and {@link EnvironmentHandler}s
 * can use to do their magic.
 * 
 * @see TokenFixer
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class DOMBuilder {
    
    private final SessionContext sessionContext;
    private final DOMBuilderOptions options;
    private final Document document;
    private final Element buildRootElement;
    private Properties currentInlineCSSProperties;
    
    public DOMBuilder(final SessionContext sessionContext, final Element buildRootElement,
            final DOMBuilderOptions options) {
        this.buildRootElement = buildRootElement;
        this.document = buildRootElement.getOwnerDocument();
        this.options = options!=null ? (DOMBuilderOptions) options.clone() : new DOMBuilderOptions();
        this.sessionContext = sessionContext;
        this.currentInlineCSSProperties = null;
    }
    
    //-------------------------------------------
    // Entry points
    
    public void buildDOMSubtree(final List<FlowToken> fixedTokens)
            throws DOMException, SnuggleParseException {
        handleTokens(buildRootElement, fixedTokens, true);
    }

    //-------------------------------------------
    // Callbacks
    
    public SessionContext getSessionContext() {
        return sessionContext;
    }
    
    public DOMBuilderOptions getOptions() {
        return options;
    }

    /**
     * @param trimWhitespace removes leading whitespace from the first resulting Node if it is a Text Node and
     *   removes trailing whitespace from the last Node if it is a Text Node.
     */
    public void handleTokens(Element parentElement, ArgumentContainerToken containerToken, boolean trimWhitespace)
            throws DOMException, SnuggleParseException {
        handleTokens(parentElement, containerToken.getContents(), trimWhitespace);
    }

    /**
     * @param trimWhitespace removes leading whitespace from the first resulting Node if it is a Text Node and
     *   removes trailing whitespace from the last Node if it is a Text Node.
     */
    public void handleTokens(Element parentElement, List<FlowToken> siblingTokens, boolean trimWhitespace)
            throws DOMException, SnuggleParseException {
        int childCountBefore = parentElement.getChildNodes().getLength();
        for (FlowToken content : siblingTokens) {
            handleToken(parentElement, content);
        }
        if (trimWhitespace) {
            NodeList childList = parentElement.getChildNodes();
            int childCountAfter = childList.getLength();
            int addedChildCount = childCountAfter - childCountBefore;
            if (addedChildCount>0) {
                int firstAddedChildIndex = childCountAfter - addedChildCount;
                int lastAddedChildIndex = childCountAfter - 1;
                
                Node firstAddedChildNode = childList.item(firstAddedChildIndex);
                if (firstAddedChildNode.getNodeType()==Node.TEXT_NODE) {
                    firstAddedChildNode.setNodeValue(firstAddedChildNode.getNodeValue().replaceFirst("^\\s+", ""));
                }
                Node lastAddedChildNode = childList.item(lastAddedChildIndex);
                if (lastAddedChildNode.getNodeType()==Node.TEXT_NODE) {
                    lastAddedChildNode.setNodeValue(lastAddedChildNode.getNodeValue().replaceFirst("\\s+$", ""));
                }
            }
        }
    }
    
    /**
     * Creates a single Node from the given token. If the token is a container then it will
     * be wrapped into a container element if required, depending on context.
     *
     * Example:
     * 
     * \msub{1+2}{3-\mrow{4*5}} -> <msup><mrow>...</mrow><mrow>...</mrow></msup>
     * @param parentElement
     * @param token
     * @throws DOMException
     * @throws SnuggleParseException
     */
    public void handleToken(Element parentElement, FlowToken token)
            throws DOMException, SnuggleParseException {
        switch (token.getType()) {
            
            /* Complex Tokens */
            
            case BRACE_CONTAINER:
                /* Create an appropriate content container */
                BraceContainerToken braceToken = (BraceContainerToken) token;
                ArgumentContainerToken content = braceToken.getBraceContent();
                Element container = null;
                if (braceToken.getLatexMode()==LaTeXMode.MATH) {
                    container = appendMathMLElement(parentElement, "mrow");
                }
                else {
                    container = appendXHTMLElement(parentElement, "span");
                }
                
                /* Handle the children of this token */
                handleTokens(container, content, true);
                
                /* If we only added one child, we'll remove from the container and add directly
                 * to the tree instead.
                 */
                NodeList childNodes = container.getChildNodes();
                if (childNodes.getLength()==1) {
                    Node singleNode = childNodes.item(0);
                    parentElement.removeChild(container);
                    parentElement.appendChild(singleNode);
                }
                break;
                
            case COMMAND:
                /* Each command has its own builder instance to do the work here */
                CommandToken commandToken = (CommandToken) token;
                CommandHandler commandNodeBuilder = commandToken.getCommand().getNodeBuilder();
                if (commandNodeBuilder==null) {
                    throw new SnuggleLogicException("No builder registered for Command " + commandToken.getCommand());
                }
                commandNodeBuilder.handleCommand(this, parentElement, commandToken);
                break;
                
            case ENVIRONMENT:
                /* This is essentially the same as COMMAND but I need to verify the details */
                EnvironmentToken envToken = (EnvironmentToken) token;
                EnvironmentHandler envNodeBuilder = envToken.getEnvironment().getNodeBuilder();
                if (envNodeBuilder==null) {
                    throw new SnuggleLogicException("No builder registered for Environment " + envToken.getEnvironment());
                }
                envNodeBuilder.handleEnvironment(this, parentElement, envToken);
                break;
                
            case ERROR:
                appendErrorElement(parentElement, (ErrorToken) token);
                break;
                
            /* Simple Tokens */
                
            case TEXT_MODE_TEXT:
                String textContent = token.getSlice().extract().toString()
                    .replace("\r", "")
                    .replace('~', '\u00a0')
                    .replace('<', '\u00a1')
                    .replace('>', '\u00bf')
                    .replace("``", "\u201c")
                    .replace("''", "\u201d")
                    .replace('`', '\u2018')
                    .replace('\'', '\u2019');
                if (isBuildingMathMLIsland(parentElement)) {
                    /* Need to wrap in an <mtext>...</mtext> */
                    appendMathMLTextElement(parentElement, "mtext", textContent, false);
                }
                else {
                    appendTextNode(parentElement, textContent, false);
                }
                break;
                
            case LR_MODE_NEW_PARAGRAPH:
                /* This is a special token to indicate that a "new paragraph" is required in LR mode,
                 * which is not really feasible so we just generate a space instead.
                 */
                if (isBuildingMathMLIsland(parentElement)) {
                    appendMathMLElement(parentElement, "mspace");
                }
                else {
                    appendTextNode(parentElement, " ", false);
                }
                break;
            
            case COMMENT:
                if (options.isIncludingComments()) {
                    SimpleToken commentToken = (SimpleToken) token;
                    Element commentElement = appendSnuggleElement(parentElement, "comment");
                    appendTextNode(commentElement, commentToken.getSlice().extract().toString(), true);
                }
                break;
                
            /* Math tokens (it's assumed that these all have Interpretations attached) */
                
            case MATH_NUMBER:
            case SINGLE_CHARACTER_MATH_IDENTIFIER:
            case SINGLE_CHARACTER_MATH_SPECIAL:
                /* First check we are in a suitable mode */
                if (isBuildingMathMLIsland(parentElement)) {
                    appendSimpleMathElement(parentElement, token);
                }
                break;
                
            case TAB_CHARACTER:
                /* If used appropriately, this will have been dealt with during fixing, so we have
                 * Error: alignment used in the wrong place.
                 */
                appendOrThrowError(parentElement, token, ErrorCode.TDEG00);
                break;
                
            default:
                throw new SnuggleLogicException("Unhandled switch case " + token.getType());
        }
    }

    public void appendSimpleMathElement(Element parentElement, Token token) {
        Interpretation interpretation = token.getInterpretation();
        if (interpretation==null) {
            throw new SnuggleLogicException("Simple Math token has null interpretation");
        }
        switch (interpretation.getType()) {
            case MATH_IDENTIFIER:
                MathIdentifierInterpretation identifierInterp = (MathIdentifierInterpretation) interpretation;
                appendMathMLIdentifierElement(parentElement, identifierInterp.getName());
                break;
                
            case MATH_FUNCTION_IDENTIFIER:
                MathFunctionIdentifierInterpretation functionInterp = (MathFunctionIdentifierInterpretation) interpretation;
                appendMathMLIdentifierElement(parentElement, functionInterp.getName());
                break;
                
            case MATH_NUMBER:
                MathNumberInterpretation numberInterp = (MathNumberInterpretation) interpretation;
                appendMathMLNumberElement(parentElement, numberInterp.getNumber().toString());
                break;
                
            case MATH_OPERATOR:
                MathOperatorInterpretation operatorInterp = (MathOperatorInterpretation) interpretation;
                appendMathMLOperatorElement(parentElement, operatorInterp.getOperator());
                break;
                
            case MATH_RELATION_OPERATOR:
                MathRelationOperatorInterpretation relationInterp = (MathRelationOperatorInterpretation) interpretation;
                appendMathMLOperatorElement(parentElement, relationInterp.getOperator());
                break;
                
            case MATH_BRACKET_OPERATOR:
                MathBracketOperatorInterpretation bracketInterp = (MathBracketOperatorInterpretation) interpretation;
                appendMathMLOperatorElement(parentElement, bracketInterp.getOperator());
                break;
                
            default:
                throw new SnuggleLogicException("Unexpected switch case " + interpretation.getType());
        }
    }

    //-------------------------------------------
    // Helpers
    
    public Document getDocument() {
        return document;
    }

    public Node appendTextNode(Element parentElement, String content, boolean trim) {
        String toAppend = trim ? content.trim() : content;
        
        /* We'll coalesce adjacent text Nodes */
        Node lastChild = parentElement.getLastChild();
        if (lastChild!=null && lastChild.getNodeType()==Node.TEXT_NODE) {
            lastChild.setNodeValue(lastChild.getNodeValue() + toAppend);
        }
        else {
            /* Previous sibling is not text Node so create new one */
            lastChild = document.createTextNode(toAppend);
            parentElement.appendChild(lastChild);
        }
        return lastChild;
    }
    
    public Element appendSnuggleElement(Element parentElement, String elementName) {
        Element element = document.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, elementName);
        parentElement.appendChild(element);
        return element;
    }
    
    public Element appendXHTMLElement(Element parentElement, String elementName) {
        Element xhtmlElement = document.createElementNS(Globals.XHTML_NAMESPACE, elementName);
        parentElement.appendChild(xhtmlElement);
        return xhtmlElement;
    }
    
    public Element appendMathMLElement(Element parentElement, String elementLocalName) {
        String qName;
        if (options.isPrefixingMathML()) {
            qName = options.getMathMLPrefix() + ":" + elementLocalName;
        }
        else {
            qName = elementLocalName;
        }
        Element mathMLElement = document.createElementNS(Globals.MATHML_NAMESPACE, qName);
        parentElement.appendChild(mathMLElement);
        return mathMLElement;
    }
    
    public Element appendMathMLTextElement(Element parentElement, String elementName, String content, boolean trim) {
        Element mathMLElement = appendMathMLElement(parentElement, elementName);
        appendTextNode(mathMLElement, content, trim);
        return mathMLElement;
    }
    
    public Element appendMathMLOperatorElement(Element parentElement, MathMLOperator operator) {
        String output = operator.getOutput();
        if (output!=null) {
            return appendMathMLOperatorElement(parentElement, output);
        }
        throw new SnuggleLogicException("null operator " + operator
                + " (probably placeholder) should have been sorted out earlier");   
    }
    
    public Element appendMathMLOperatorElement(Element parentElement, String content) {
        return appendMathMLTextElement(parentElement, "mo", content, true);
    }
    
    public Element appendMathMLNumberElement(Element parentElement, String number) {
        return appendMathMLTextElement(parentElement, "mn", number, true);
    }
    
    public Element appendMathMLIdentifierElement(Element parentElement, String name) {
        return appendMathMLTextElement(parentElement, "mi", name, true);
    }
    
    public void handleMathTokensAsSingleElement(Element parentElement, ArgumentContainerToken containerToken)
            throws DOMException, SnuggleParseException {
        handleMathTokensAsSingleElement(parentElement, containerToken.getContents());
    }
    
    public void handleMathTokensAsSingleElement(Element parentElement, List<FlowToken> tokens)
            throws DOMException, SnuggleParseException {
        /* We wrap in an <mrow/> */
        Element mrow = appendMathMLElement(parentElement, "mrow");
        handleTokens(mrow, tokens, true);
        NodeList addedNodes = mrow.getChildNodes();
        if (addedNodes.getLength()==1) {
            /* Only resulted in 1 Element being added so no need for <mrow/> */
            Node addedNode = addedNodes.item(0);
            parentElement.removeChild(mrow);
            parentElement.appendChild(addedNode);
        }
    }
    
    //-------------------------------------------
    
    /**
     * This "converts" the given token to a String by performing
     * {{@link #handleTokens(Element, ArgumentContainerToken, boolean)}}
     * with a fake root element and then extracting the String value of the resulting Text Node(s).
     */
    public String extractStringValue(ArgumentContainerToken token)
            throws DOMException, SnuggleParseException {
        Element dummyContainer = document.createElement("dummy");
        handleTokens(dummyContainer, token, true);
        
        StringBuilder resultBuilder = new StringBuilder();
        buildStringValue(resultBuilder, dummyContainer.getChildNodes());
        
        return resultBuilder.toString();
    }
    
    private void buildStringValue(StringBuilder resultBuilder, NodeList nodes) {
        for (int i=0, size=nodes.getLength(); i<size; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()==Node.TEXT_NODE) {
                resultBuilder.append(node.getNodeValue());
            }
            else if (node.getNodeType()==Node.ELEMENT_NODE) {
                buildStringValue(resultBuilder, node.getChildNodes());
            }
        }
    }
    
    //-------------------------------------------
    
    public void applyCSSStyle(Element xhtmlElement, String cssClassName) {
        if (options.isInliningCSS()) {
            /* Look for a 'localName.className' declaration in the properties file. If not
             * found, look up a generic '.className' declaration.
             */
            Properties inlineCSSProperties = getCurrentInlineCSSProperties();
            String property = inlineCSSProperties.getProperty(xhtmlElement.getLocalName() + "." + cssClassName);
            if (property==null) {
                property = inlineCSSProperties.getProperty("." + cssClassName);
            }
            /* If we found a declaration, set style attribute accordingly. Otherwise, we'll ignore. */
            if (property!=null) {
                xhtmlElement.setAttribute("style", property);
            }
        }
        else {
            /* Just set 'class' attribute */
            xhtmlElement.setAttribute("class", cssClassName);
        }
    }
    
    private Properties getCurrentInlineCSSProperties() {
        if (currentInlineCSSProperties==null) {
            currentInlineCSSProperties = CSSUtilities.readInlineCSSProperties(options);
        }
        return currentInlineCSSProperties;
    }
    
    //-------------------------------------------
    
    /**
     * Returns whether or not we're building a MathML island by checking the namespace of the
     * parent element (and any of its ancestors if required) to ensure that we are within a
     * MathML and custom (i.e. non-XHTML) output tree.
     * 
     * @param parentElement
     */
    public boolean isBuildingMathMLIsland(Element parentElement) {
        Element currentElement = parentElement;
        Node parentNode;
        while (true) {
            String namespaceURI = currentElement.getNamespaceURI();
            if (Globals.MATHML_NAMESPACE.equals(namespaceURI)) {
                return true;
            }
            else if (Globals.XHTML_NAMESPACE.equals(namespaceURI)) {
                return false;
            }
            else  {
                parentNode = currentElement.getParentNode();
                if (parentNode==null || parentNode.getNodeType()!=Node.ELEMENT_NODE) {
                    break;
                }
                currentElement = (Element) parentNode;
            }
        }
        return false;
    }
    
    public boolean isParentElement(final Element parentElement, final String requiredNamespaceUri,
            final String... requiredLocalNames) {
        if (!requiredNamespaceUri.equals(parentElement.getNamespaceURI())) {
            return false;
        }
        if (requiredLocalNames.length==0) {
            return true;
        }
        /* Ensure we match one of the given local names */
        String localName = parentElement.getLocalName();
        for (String allowedLocalName : requiredLocalNames) {
            if (localName.equals(allowedLocalName)) {
                return true;
            }
        }
        return false;
    }
    
    //-------------------------------------------

    public Element appendOrThrowError(final Element parentElement, final Token token,
            final ErrorCode errorCode, final Object... arguments)
            throws SnuggleParseException {
        InputError error = new InputError(errorCode, token.getSlice(), arguments);
        sessionContext.registerError(error);
        return appendErrorElement(parentElement, new ErrorToken(error, token.getLatexMode()));
    }
    
    private Element appendErrorElement(final Element parentElement, final ErrorToken errorToken) {
        ErrorOutputOptions errorOptions = options.getErrorOptions();
        Element errorElement;
        switch (errorOptions) {
            case NO_OUTPUT:
                /* Add nothing */
                break;
                
            case XML_FULL:
            case XML_SHORT:
                /* Output XML at the current point in the DOM */
                errorElement = MessageFormatter.formatErrorAsXML(document,
                        errorToken.getError(),
                        errorOptions==ErrorOutputOptions.XML_FULL);
                parentElement.appendChild(errorElement);
                break;
                
            case XHTML:
                /* If we're in the middle of a MathML island,
                 * add a MathML <merror/> element at the current point */
                if (isBuildingMathMLIsland(parentElement)) {
                    Element merror = appendMathMLElement(parentElement, "merror");
                    appendMathMLTextElement(merror, "mtext",
                            errorToken.getError().getErrorCode().toString(), false);
                }
                
                /* Output full XHTML fragment as a child of the nearest XHTML ancestor-or-self */
                errorElement = MessageFormatter.formatErrorAsXHTML(document, errorToken.getError());
                Element ancestorElement = parentElement;
                Node ancestorNode;
                while (!ancestorElement.getNamespaceURI().equals(Globals.XHTML_NAMESPACE)) {
                    ancestorNode = parentElement.getParentNode();
                    if (ancestorNode==null || ancestorNode.getNodeType()!=Node.ELEMENT_NODE) {
                        throw new SnuggleLogicException("Could not find an XHTML ancestor to add error element to");
                    }
                    ancestorElement = (Element) ancestorNode;
                }
                ancestorElement.appendChild(errorElement);
                break;
                
            default:
                throw new SnuggleLogicException("Unexpected switch case " + errorOptions);
        }
        return parentElement;
    }
}
