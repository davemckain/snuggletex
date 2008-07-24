/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.aardvark.commons.util.ArrayListStack;
import uk.ac.ed.ph.aardvark.commons.util.StringUtilities;
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
import uk.ac.ed.ph.snuggletex.definitions.MathVariantMap;
import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.dombuilding.EnvironmentHandler;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathFunctionIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathNumberInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.NottableMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;
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
 * This takes a {@link List} of (fixed) {@link Token}s and builds a XHTML + MathML
 * DOM tree branch from them.
 * 
 * <h2>Usage</h2>
 * 
 * Clients should not normally have to use this class directly -
 * call {@link SnuggleTeXSession#buildDOMSubtree(Element, DOMBuilderOptions)} and friends.
 * <p>
 * An instance of this class is intended to be used once and then discarded so can be stateful.
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
    
    //-------------------------------------------
    // Building State
    
    /** 
     * Trivial enumeration to keep track of where we are in the outgoing DOM. This makes life
     * a bit easier when handling certain types of tokens. Use the {@link DOMBuilder#getOutputContext()}
     * and {@link DOMBuilder#setOutputContext(OutputContext)} to manage the current status.
     */
    public static enum OutputContext {
        XHTML,
        MATHML_BLOCK,
        MATHML_INLINE,
        ;
    }
    
    private OutputContext outputContext;
    private ArrayListStack<MathVariantMap> mathVariantMapStack;
    
    //-------------------------------------------
    
    public DOMBuilder(final SessionContext sessionContext, final Element buildRootElement,
            final DOMBuilderOptions options) {
        this.buildRootElement = buildRootElement;
        this.document = buildRootElement.getOwnerDocument();
        this.sessionContext = sessionContext;
        this.options = options;
        this.currentInlineCSSProperties = null;
        
        this.outputContext = null;
        this.mathVariantMapStack = new ArrayListStack<MathVariantMap>();
    }
    
    //-------------------------------------------
    // External entry point
    
    /**
     * @throws SnuggleParseException
     * @throws DOMException
     */
    public void buildDOMSubtree(final List<FlowToken> fixedTokens) throws SnuggleParseException {
    	this.outputContext = OutputContext.XHTML;
    	this.mathVariantMapStack.clear();
        handleTokens(buildRootElement, fixedTokens, true);
    }
    
    //-------------------------------------------
    // Usual Accessors
    
    public SessionContext getSessionContext() {
        return sessionContext;
    }
    
    public DOMBuilderOptions getOptions() {
        return options;
    }
    
    public Document getDocument() {
        return document;
    }
    
    //-------------------------------------------
    // State Accessors
    
    public ArrayListStack<MathVariantMap> getMathVariantMapStack() {
        return mathVariantMapStack;
    }
    
    //-------------------------------------------
    // Output context mutators - records whether we're doing XHTML or MathML content,
    // which is often useful.
    
    public OutputContext getOutputContext() {
        return outputContext;
    }

    public void setOutputContext(OutputContext outputContext) {
        this.outputContext = outputContext;
    }  
    
    /**
     * Returns whether or not we're building a MathML island by checking the current
     * {@link OutputContext}.
     */
    public boolean isBuildingMathMLIsland() {
        return outputContext==OutputContext.MATHML_BLOCK || outputContext==OutputContext.MATHML_INLINE;
    }
    
    //-------------------------------------------
    // Callbacks Start Below
    //
    // Note that all methods may throw the unchecked DOMException !

    /**
     * @param trimWhitespace removes leading whitespace from the first resulting Node if it is a Text Node and
     *   removes trailing whitespace from the last Node if it is a Text Node.
     */
    public void handleTokens(Element parentElement, ArgumentContainerToken containerToken, boolean trimWhitespace)
            throws SnuggleParseException {
        handleTokens(parentElement, containerToken.getContents(), trimWhitespace);
    }

    /**
     * @param trimWhitespace removes leading whitespace from the first resulting Node if it is a Text Node and
     *   removes trailing whitespace from the last Node if it is a Text Node.
     */
    public void handleTokens(Element parentElement, List<FlowToken> siblingTokens, boolean trimWhitespace)
            throws SnuggleParseException {
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
     * 
     * @throws DOMException
     * @throws SnuggleParseException
     */
    public void handleToken(Element parentElement, FlowToken token) throws SnuggleParseException {
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
            	handleTextToken(parentElement, (SimpleToken) token);
                break;
                
            case LR_MODE_NEW_PARAGRAPH:
                /* This is a special token to indicate that a "new paragraph" is required in LR mode,
                 * which is not really feasible so we just generate a space instead.
                 */
                if (isBuildingMathMLIsland()) {
                    appendMathMLSpace(parentElement, "1ex");
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
                if (isBuildingMathMLIsland()) {
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
    
    public void handleTextToken(Element parentElement, SimpleToken textToken) {
    	CharSequence rawText = textToken.getSlice().extract();
    	StringBuilder resultBuilder = new StringBuilder();
    	char c;
    	for (int i=0, length=rawText.length(); i<length; i++) {
    		c = rawText.charAt(i);
    		switch (c) {
    			case '\r':
    				break;
    				
    			case '~':
    				resultBuilder.append('\u00a0');
    				break;
    				
    			case '<':
    				resultBuilder.append('\u00a1');
    				break;
    				
    			case '>':
    				resultBuilder.append('\u00bf');
    				break;
    				
    			case '`':
    				if (i+1<length && rawText.charAt(i+1)=='`') {
    					/* Double open quote */
    					resultBuilder.append('\u201c');
    					i++;
    				}
    				else {
    					/* Single open quote */
    					resultBuilder.append('\u2018');
    				}
    				break;
    				
    			case '\'':
    				if (i+1<length && rawText.charAt(i+1)=='\'') {
    					/* Double close quote */
    					resultBuilder.append('\u201d');
    					i++;
    				}
    				else {
    					/* Single close quote */
    					resultBuilder.append('\u2019');
    				}
    				break;
    				
    			case '-':
    				if (i+1<length && rawText.charAt(i+1)=='-') {
        				if (i+2<length && rawText.charAt(i+2)=='-') {
        					/* --- is a long dash */
        					resultBuilder.append('\u2014');
        					i += 2;
        				}
        				else {
        					/* -- is a medium dash */
        					resultBuilder.append('\u2013');
        					i++;
        				}
    				}
    				else {
    					/* Just do a normal hyphen */
    					resultBuilder.append('-');
    				}
    				break;
    				
    			default:
    				resultBuilder.append(c);
    				break;
    		}
    	}
    	String resultString = resultBuilder.toString();
	    if (isBuildingMathMLIsland()) {
	        /* Need to wrap in an <mtext>...</mtext>.
	         * Note that leading and trailing whitespace is ignored in <mtext/> elements so, if
	         * whitespace is asked for, it must be added via a <mspace/>
	         */
	        if (Character.isWhitespace(resultString.charAt(0))) {
	            appendMathMLSpace(parentElement, "1ex");
	        }
	        appendMathMLTextElement(parentElement, "mtext", resultString, true);
	        if (Character.isWhitespace(resultString.charAt(resultString.length()-1))) {
               appendMathMLSpace(parentElement, "1ex");
	        }
	    }
	    else {
	        appendTextNode(parentElement, resultString, false);
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
                SimpleMathOperatorInterpretation operatorInterp = (SimpleMathOperatorInterpretation) interpretation;
                appendMathMLOperatorElement(parentElement, operatorInterp.getOperator());
                break;
                
            case MATH_RELATION_OPERATOR:
                NottableMathOperatorInterpretation relationInterp = (NottableMathOperatorInterpretation) interpretation;
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
    
    public Element appendMathMLSpace(Element parentElement, String width) {
        Element mspaceElement = appendMathMLElement(parentElement, "mspace");
        mspaceElement.setAttribute("width", width);
        return mspaceElement;
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
    
    /**
     * Creates a MathML identifier element, which may either be a single character or multiple
     * characters (e.g. "sin"). The single-character case is interesting if we are currently
     * applying a "mathvariant" in that it may or may not then be mapped into a corresponding
     * target character.
     * 
     * @see DOMBuilderOptions#isMathVariantMapping()
     * 
     * @param parentElement
     * @param name
     */
    public Element appendMathMLIdentifierElement(Element parentElement, String name) {
    	String mappedIdentifier = name;
    	String mathVariant = null;
    	if (name.length()==1 && !mathVariantMapStack.isEmpty()) {
    	    /* If we have a single character identifier and something like \\mathcal is in force,
    	     * then we will apply a "mathvariant" attribute.
    	     */
    		MathVariantMap currentMathCharacterMap = mathVariantMapStack.peek();
    		mathVariant = currentMathCharacterMap.getMathVariantName();
    		if (options.isMathVariantMapping()) {
    		    /* Client has asked to try to map this character to a suitable target Unicode
    		     * character, so let's try this.
    		     */
	            char mappedChar = currentMathCharacterMap.getAccentedChar(name.charAt(0));
	            if (mappedChar!=0) {
	                mappedIdentifier = Character.toString(mappedChar);
	            }
    		}
    	}
        Element result = appendMathMLTextElement(parentElement, "mi", mappedIdentifier, true);
        if (mathVariant!=null) {
        	result.setAttribute("mathvariant", mathVariant);
        }
        return result;
    }
    
    public void handleMathTokensAsSingleElement(Element parentElement, ArgumentContainerToken containerToken)
            throws SnuggleParseException {
        handleMathTokensAsSingleElement(parentElement, containerToken.getContents());
    }
    
    public void handleMathTokensAsSingleElement(Element parentElement, List<FlowToken> tokens)
            throws SnuggleParseException {
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
     * 
     * @throws DOMException
     * @throws {@link SnuggleParseException}
     */
    public String extractStringValue(ArgumentContainerToken token) throws SnuggleParseException {
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
    
    public void applyCSSStyle(Element xhtmlElement, String... cssClassNames) {
        if (options.isInliningCSS()) {
            /* We'll go through each requested class in order and pull in the appropriate CSS styles.
             * NOTE: We're not going to try to eliminate any duplicates here!
             */
            Properties inlineCSSProperties = getCurrentInlineCSSProperties();
            StringBuilder styleBuilder = new StringBuilder();
            String property;
            boolean needsSemiColon = false;
            for (String cssClassName : cssClassNames) {
                /* Look for a 'localName.className' declaration in the properties file. If not
                 * found, look up a generic '.className' declaration.
                 */
                property = inlineCSSProperties.getProperty(xhtmlElement.getLocalName() + "." + cssClassName);
                if (property==null) {
                    property = inlineCSSProperties.getProperty("." + cssClassName);
                }
                if (needsSemiColon) {
                    styleBuilder.append("; ");
                }
                property = property.trim();
                styleBuilder.append(property);
                needsSemiColon = property.endsWith(";");
            }

            /* If we found declarations, set style attribute accordingly. Otherwise, we'll ignore. */
            if (styleBuilder.length()>0) {
                xhtmlElement.setAttribute("style", styleBuilder.toString());
            }
        }
        else {
            /* Just set 'class' attribute */
            xhtmlElement.setAttribute("class", StringUtilities.join(cssClassNames, " "));
        }
    }
    
    private Properties getCurrentInlineCSSProperties() {
        if (currentInlineCSSProperties==null) {
            currentInlineCSSProperties = CSSUtilities.readInlineCSSProperties(options);
        }
        return currentInlineCSSProperties;
    }
    
    //-------------------------------------------

    public Element findNearestXHTMLAncestorOrSelf(final Element element) {
        Element currentElement = element;
        Node parentNode;
        while (true) {
            if (currentElement==buildRootElement) {
                /* We're at the root of our tree, so stop */
                return currentElement;
            }
            else if (Globals.XHTML_NAMESPACE.equals(currentElement.getNamespaceURI())) {
                /* We're at an XHTML element, so stop */
                return currentElement;
            }
            else  {
                /* Go up */
                parentNode = currentElement.getParentNode();
                if (parentNode==null || parentNode.getNodeType()!=Node.ELEMENT_NODE) {
                    throw new SnuggleLogicException("Traversed up DOM tree and never found our root Element!");
                }
                currentElement = (Element) parentNode;
            }
        }
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
    
    public Element appendErrorElement(final Element parentElement, final ErrorToken errorToken) {
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
                if (isBuildingMathMLIsland()) {
                    Element merror = appendMathMLElement(parentElement, "merror");
                    appendMathMLTextElement(merror, "mtext",
                            errorToken.getError().getErrorCode().toString(), false);
                }
                
                /* Output full XHTML fragment as a child of the nearest non-MathML ancestor-or-self */
                Element ancestorElement = findNearestXHTMLAncestorOrSelf(parentElement);
                errorElement = MessageFormatter.formatErrorAsXHTML(document, errorToken.getError());
                ancestorElement.appendChild(errorElement);
                break;
                
            default:
                throw new SnuggleLogicException("Unexpected switch case " + errorOptions);
        }
        return parentElement;
    }
}
