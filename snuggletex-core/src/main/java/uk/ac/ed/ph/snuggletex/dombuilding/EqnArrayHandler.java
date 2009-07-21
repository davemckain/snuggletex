/* $Id:EqnArrayBuilder.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleConstants;
import uk.ac.ed.ph.snuggletex.definitions.CoreErrorCode;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles the <tt>eqnarray*</tt> environment.
 * 
 * @see MathEnvironmentHandler
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class EqnArrayHandler implements EnvironmentHandler {
    
    private static final String[] COLUMN_ALIGNMENTS = {
        "right",
        "center",
        "left"
    };

    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        /* Compute the geometry of the table and make sure its content model is OK */
        int[] geometry = TabularHandler.computeTableDimensions(token.getContent());
        int numColumns = geometry[1];
        if (numColumns>3) {
            /* Error: eqnarray must have no more than 3 columns */
            builder.appendOrThrowError(parentElement, token, CoreErrorCode.TDEM01, numColumns);
            return;
        }
        
        /* This is the element we'll append the <mtable/> to. It will be either <math/>
         * (if no annotations) or <semantics/> (if annotations) */
        Element mtableParent; 

        /* Build MathML container and structure */
        builder.pushOutputContext(OutputContext.MATHML_BLOCK);
        Element mathElement = builder.appendMathMLElement(parentElement, "math");
        mathElement.setAttribute("display", "block");
        if (builder.getOptions().isAddingMathAnnotations()) {
            /* This is similar to what we do in MathEnvironmentBuilder. Things are a bit
             * simpler here, though, as we are only going to generate a single <mtable/>
             * element so there's no need to consider multiple child elements.
             */
            Element semantics = builder.appendMathMLElement(mathElement, "semantics");
            mtableParent = semantics;
        }
        else {
            mtableParent = mathElement;
        }
        
        /* Build <mtable/> */
        Element mtableElement = builder.appendMathMLElement(mtableParent, "mtable");
        Element mtrElement, mtdElement;
        int columnIndex;
        for (FlowToken rowToken : token.getContent()) {
            mtrElement = builder.appendMathMLElement(mtableElement, "mtr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            columnIndex = 0;
            for (FlowToken columnToken : columns) {
                mtdElement = builder.appendMathMLElement(mtrElement, "mtd");
                mtdElement.setAttribute("columnalign", COLUMN_ALIGNMENTS[columnIndex++]);
                builder.handleTokens(mtdElement, ((CommandToken) columnToken).getArguments()[0].getContents(), true);
            }
            /* Add empty <td/> for missing columns */
            for (int i=0; i<numColumns-columns.size(); i++) {
                builder.appendMathMLElement(mtrElement, "mtd");
            }
        }
        
        /* Maybe create MathML annotation */
        if (builder.getOptions().isAddingMathAnnotations()) {
            Element annotation = builder.appendMathMLTextElement(mtableParent, "annotation",
                    token.getSlice().extract().toString(), true);
            annotation.setAttribute("encoding", SnuggleConstants.SNUGGLETEX_MATHML_ANNOTATION_ENCODING);
        }
        
        /* Reset output context back to XHTML */
        builder.popOutputContext();
    }
}
