/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles the <tt>eqnarray*</tt> environment.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class EqnArrayBuilder implements EnvironmentHandler {

    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        /* Compute the geometry of the table and make sure its content model is OK */
        int[] geometry = computeTableDimensions(token.getContent());
        int numColumns = geometry[1];

        /* Build MathML */
        Element mathElement = builder.appendMathMLElement(parentElement, "math");
        mathElement.setAttribute("display", "block");
        
        Element mtableElement = builder.appendMathMLElement(mathElement, "mtable");
        Element mtrElement, mtdElement;
        for (FlowToken rowToken : token.getContent()) {
            mtrElement = builder.appendMathMLElement(mtableElement, "mtr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            for (FlowToken columnToken : columns) {
                mtdElement = builder.appendMathMLElement(mtrElement, "mtd");
                builder.handleTokens(mtdElement, ((CommandToken) columnToken).getArguments()[0].getContents(), true);
            }
            /* Add empty <td/> for missing columns */
            for (int i=0; i<numColumns-columns.size(); i++) {
                builder.appendMathMLElement(mtrElement, "mtd");
            }
        }
    }
     
    /**
     * This is fairly generic so could be shared amongst all of the related builders.
     * 
     * @param tableContent
     */
    protected int[] computeTableDimensions(ArgumentContainerToken tableContent) {
        int maxColumns = 0;
        int rowCount = 0;
        int colCountWithinRow = 0;
        for (FlowToken contentToken : tableContent) {
            if (contentToken.isCommand(GlobalBuiltins.TABLE_ROW)) {
                rowCount++;
                colCountWithinRow = 0;
                CommandToken rowToken = (CommandToken) contentToken;
                ArgumentContainerToken rowContents = rowToken.getArguments()[0];
                for (FlowToken rowContentToken : rowContents) {
                    if (rowContentToken.isCommand(GlobalBuiltins.TABLE_COLUMN)) {
                        colCountWithinRow++;
                    }
                    else {
                        throw new SnuggleLogicException("Did not expect to find token "
                                + rowContentToken
                                + " within a table row");
                    }
                }
                if (colCountWithinRow>maxColumns) {
                    maxColumns = colCountWithinRow;
                }
            }
            else {
                throw new SnuggleLogicException("Did not expect to find token "
                        + contentToken
                        + " within a top-level table content");
            }
        }
        return new int[] { rowCount, maxColumns };
    }
}
