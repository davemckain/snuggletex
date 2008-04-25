/* $Id: MathEnvironmentBuilder.java,v 1.9 2008/04/18 09:44:05 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 * 
 * @author  David McKain
 * @version $Revision: 1.9 $
 */
public final class MathEnvironmentBuilder implements EnvironmentHandler {

    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        BuiltinEnvironment environment = token.getEnvironment();
        if (builder.isBuildingMathMLIsland(parentElement)) {
            /* We're putting maths inside maths (e.g. \mbox{$ $}) so make a <mrow/> */
            Element mrow = builder.appendMathMLElement(parentElement, "mrow");
            builder.handleTokens(mrow, token.getContent(), false);
        }
        else {
            /* Create a proper <math>...</math> element with optional annotation */
            Element math = builder.appendMathMLElement(parentElement, "math");
            if (environment==GlobalBuiltins.DISPLAYMATH) {
                math.setAttribute("display", "block");
            }
            if (builder.getSessionContext().getConfiguration().isAddingMathAnnotations()) {
                /* The structure here is <semantics>....<annotation-xml/></semantics> */
                Element semantics = builder.appendMathMLElement(math, "semantics");
                
                builder.handleTokens(semantics, token.getContent(), false);
                
                Element annotation = builder.appendMathMLTextElement(semantics, "annotation-xml",
                        token.getContent().getSlice().extract().toString(), true);
                annotation.setAttribute("encoding", "snuffle-tex");
            }
            else {
                builder.handleTokens(math, token.getContent(), false);
            }
        }
    }
}
