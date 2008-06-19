/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.MATHML_NAMESPACE;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

/**
 * Builds LaTeX math environments. Note that this might be inside an mbox inside
 * an existing math environment.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathEnvironmentBuilder implements EnvironmentHandler {
	
	public static final String SNUGGLETEX_ENCODING = "SnuggleTeX";

    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        BuiltinEnvironment environment = token.getEnvironment();
        if (builder.isBuildingMathMLIsland()) {
            /* We're putting maths inside maths (e.g. \mbox{$ $}) so make a <mrow/> */
            Element mrow = builder.appendMathMLElement(parentElement, "mrow");
            builder.handleTokens(mrow, token.getContent(), false);
        }
        else {
        	/* Set output context appropriately */
        	boolean isDisplayMath = environment==GlobalBuiltins.DISPLAYMATH;
        	builder.setOutputContext(isDisplayMath ? OutputContext.MATHML_BLOCK : OutputContext.MATHML_INLINE);
        	
            /* Create a proper <math>...</math> element with optional annotation */
            Element math = builder.appendMathMLElement(parentElement, "math");
            if (isDisplayMath) {
                math.setAttribute("display", "block");
            }
            if (builder.getOptions().isAddingMathAnnotations()) {
                /* The structure here is <semantics>...<annotation/></semantics>
                 * where the first child of <semantics> is the resulting MathML.
                 * (Therefore, we need to wrap the MathML in an <mrow/> if there is
                 * more than one top level element here)
                 */
                Element semantics = builder.appendMathMLElement(math, "semantics");
                builder.handleMathTokensAsSingleElement(semantics, token.getContent());

                /* Create annotation */
                Element annotation = builder.appendMathMLTextElement(semantics, "annotation",
                        token.getContent().getSlice().extract().toString(), true);
                annotation.setAttribute("encoding", SNUGGLETEX_ENCODING);
            }
            else {
                builder.handleTokens(math, token.getContent(), false);
            }
            /* Reset output context back to XHTML */
            builder.setOutputContext(OutputContext.XHTML);
        }
    }
    
    public static String extractSnuggleTeXAnnotation(Element mathmlElement) {
    	if (MATHML_NAMESPACE.equals(mathmlElement.getNamespaceURI()) && "math".equals(mathmlElement.getLocalName())) {
    		/* Look for semantics child then annotation child with encoding set appropriately */
    		Node search = mathmlElement.getFirstChild();
    		if (!(search.getNodeType()==ELEMENT_NODE && MATHML_NAMESPACE.equals(search.getNamespaceURI())
    				&& "semantics".equals(search.getLocalName()))) {
    			/* Didn't get <semantics/> as first and only child */
    			return null;
    		}
    		Element semantics = (Element) search;
    		NodeList childNodes = semantics.getChildNodes();
    		for (int i=0, length=childNodes.getLength(); i<length; i++) {
    			search = childNodes.item(i);
    			if (search.getNodeType()==ELEMENT_NODE && MATHML_NAMESPACE.equals(search.getNamespaceURI())
    					&& "annotation".equals(search.getLocalName()) && SNUGGLETEX_ENCODING.equals(((Element) search).getAttribute("encoding"))) {
    				return search.getFirstChild().getNodeValue();
    		}
    		}
    	}
    	return null;
    }
}
