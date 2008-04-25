/*
 * $Id: TabularBuilder.java,v 1.7 2008/04/18 09:44:05 dmckain Exp $
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
 * FIXME: Document this type!
 * 
 * TODO: This should also be able to do mathematical constructs by generating <mtable/> and friends
 * instead of <table/>
 * TODO: This is legal inside $\mbox{...}$ so needs to output MathML in this case. Eeek!!!
 * TODO: I'm currently ignoring the 'cols' argument (e.g. 'lc', '|ccc|' etc.) Need to think about
 * how to integrate this best and also which arguments to support.
 * TODO: No support for \hline, \vline and friends!!!
 * 
 * @author  David McKain
 * @version $Revision: 1.7 $
 */
public final class TabularBuilder implements EnvironmentHandler {

    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        /* Compute the geometry of the table and make sure its content model is OK */
        int[] geometry = computeTableDimensions(token.getContent());
        int numColumns = geometry[1];

        /* Build XHTML */
        Element tableElement = builder.appendXHTMLElement(parentElement, "table");
        Element tbodyElement = builder.appendXHTMLElement(tableElement, "tbody");
        Element trElement, tdElement;
        for (FlowToken rowToken : token.getContent()) {
            trElement = builder.appendXHTMLElement(tbodyElement, "tr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            for (FlowToken columnToken : columns) {
                tdElement = builder.appendXHTMLElement(trElement, "td");
                builder.handleTokens(tdElement, ((CommandToken) columnToken).getArguments()[0].getContents(), true);
            }
            /* Add empty <td/> for missing columns */
            for (int i=0; i<numColumns-columns.size(); i++) {
                builder.appendXHTMLElement(trElement, "td");
            }
        }
    }
     
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
