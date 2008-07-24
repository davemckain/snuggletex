/* $Id: EqnArrayBuilder.java 52 2008-05-18 19:12:25Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles the LaTeX <tt>array</tt> environment.
 * <p>
 * This can only be used in MATH mode and generates a <tt>mtable</tt> as a result.
 * 
 * TODO: Do alignment!
 * 
 * @author  David McKain
 * @version $Revision: 52 $
 */
public final class ArrayBuilder implements EnvironmentHandler {
	
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        /* Compute the geometry of the table and make sure its content model is OK */
        int[] geometry = TabularBuilder.computeTableDimensions(token.getContent());
        int numColumns = geometry[1];
        
        /* Parse the cell alignment properties.
         * 
         * We only support the characters 'lrc'.
         */
        ArgumentContainerToken alignSpecToken = token.getArguments()[0];
        CharSequence alignSpecData = token.getArguments()[0].getSlice().extract(); /* This is something like "ccc" */
        
        /* Make sure we got at least one column */
        if (alignSpecData.length()==0) {
            /* Error: no columns! */
            builder.appendOrThrowError(parentElement, alignSpecToken, ErrorCode.TDEMA1);
            /* We'll bail out here as nothing good will come of continuing! */
            return;
        }
        
        /* Check that we've got l,r,c only */
        char c;
        List<String> alignments = new ArrayList<String>(alignSpecData.length());
        for (int i=0; i<alignSpecData.length(); i++) {
            c = alignSpecData.charAt(i);
            switch(c) {
                case 'c': alignments.add("center"); break;
                case 'l': alignments.add("left"); break;
                case 'r': alignments.add("right"); break;
                default:
                    builder.appendOrThrowError(parentElement, alignSpecToken, ErrorCode.TDEMA0,
                            String.valueOf(c));
                    alignments.add("center"); /* We'll use this as a default */
                    break;
            }
        }
        
        /* Make sure number of columns specified is at least as much as what was calculated */
        if (alignSpecData.length() < numColumns) {
            /* Error: More columns than expected */
            builder.appendOrThrowError(parentElement, alignSpecToken, ErrorCode.TDETB0,
                    Integer.valueOf(alignSpecData.length()), Integer.valueOf(numColumns));
        }

        /* Right, now we generate the resulting <mtable/> to the output */
        Element mtableElement = builder.appendMathMLElement(parentElement, "mtable");
        Element mtrElement, mtdElement;
        FlowToken columnToken;
        for (FlowToken rowToken : token.getContent()) {
            mtrElement = builder.appendMathMLElement(mtableElement, "mtr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            for (int i=0; i<columns.size(); i++) {
                columnToken = columns.get(i);
                mtdElement = builder.appendMathMLElement(mtrElement, "mtd");
                mtdElement.setAttribute("columnalign", alignments.get(i));
                builder.handleTokens(mtdElement, ((CommandToken) columnToken).getArguments()[0].getContents(), true);
            }
            /* Add empty <td/> for missing columns */
            for (int i=0; i<numColumns-columns.size(); i++) {
                builder.appendMathMLElement(mtrElement, "mtd");
            }
        }
    }
}
