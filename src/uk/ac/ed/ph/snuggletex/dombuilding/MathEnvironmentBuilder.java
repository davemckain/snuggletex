/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;


import uk.ac.ed.ph.snuggletex.SnuggleTeX;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Builds LaTeX math environments. Note that this might be inside an mbox inside
 * an existing math environment.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class MathEnvironmentBuilder implements EnvironmentHandler {
	
	public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        BuiltinEnvironment environment = token.getEnvironment();
        if (builder.isBuildingMathMLIsland()) {
            /* We're putting maths inside maths (e.g. \mbox{$ $}) so make a <mrow/> */
            Element mrow = builder.appendMathMLElement(parentElement, "mrow");
            builder.handleTokens(mrow, token.getContent(), false);
        }
        else {
        	boolean isDisplayMath = environment==GlobalBuiltins.ENV_DISPLAYMATH;
        	buildMathElement(builder, parentElement, token, token.getContent(), isDisplayMath);
        }
    }
	
	public void buildMathElement(final DOMBuilder builder, final Element parentElement,
	        final Token token, final ArgumentContainerToken contentToken,
	        final boolean isDisplayMath) throws SnuggleParseException {
	    /* Set output context appropriately */
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
            builder.handleMathTokensAsSingleElement(semantics, contentToken);

            /* Create annotation */
            Element annotation = builder.appendMathMLTextElement(semantics, "annotation",
                    token.getSlice().extract().toString(), true);
            annotation.setAttribute("encoding", SnuggleTeX.SNUGGLETEX_MATHML_ANNOTATION_ENCODING);
        }
        else {
            builder.handleTokens(math, contentToken, false);
        }
        /* Reset output context back to XHTML */
        builder.setOutputContext(OutputContext.XHTML);
	}
}
