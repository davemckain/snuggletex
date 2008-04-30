/*
 * $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles the (rather complex) <tt>tabular</tt> environment.
 * 
 * TODO: This is legal inside $\mbox{...}$ so needs to output MathML in this case. Eeek!!!
 * TODO: No support for \hline, \vline and friends!!!
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class TabularBuilder implements EnvironmentHandler {
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException, SnuggleParseException {
        /* Compute the geometry of the table and make sure its content model is OK */
        int[] geometry = computeTableDimensions(token.getContent());
        int numColumns = geometry[1];
        
        /* Parse the cell alignment and vertical border properties.
         * 
         * We are currently only supporting the characters '|lrc'.
         * Double borders are currently ignored.
         * 
         * NOTE: We'll apply borders to the left of cells by default. We keep track of whether
         * the final cell in a row needs a right border separately.
         * 
         */
        List<List<String>> columnClasses = new ArrayList<List<String>>();
        ArgumentContainerToken specToken = token.getArguments()[0];
        CharSequence specData = token.getArguments()[0].getSlice().extract(); /* This includes {...} */
        char c;
        String cellAlign = null;
        boolean borderFlag = false; /* Gets set when we find '|' to indicate that next cell should include left border */
        for (int i=1; i<specData.length()-1; i++) { /* We just iterate over content of {...} */
            c = specData.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            switch(c) {
                case 'c':
                    cellAlign = "align-center";
                    break;
                    
                case 'l':
                    cellAlign = "align-left";
                    break;
                    
                case 'r':
                    cellAlign = "align-right";
                    break;
                
                case '|':
                    borderFlag = true;
                    break;
                    
                default:
                    /* Error: can't handle column spec */
                    builder.appendOrThrowError(parentElement, specToken, ErrorCode.TDETB1,
                            String.valueOf(c));
                    break;
            }
            if (cellAlign!=null) {
                /* We've specified a whole column, so record which CSS classes it should get */
                List<String> classes = new ArrayList<String>();
                classes.add(cellAlign);
                if (borderFlag) {
                    classes.add("left-border");
                }
                columnClasses.add(classes);
                cellAlign = null;
                borderFlag = false;
            }
        }
        
        /* Make sure we actually specified a column! */
        if (columnClasses.isEmpty()) {
            /* Error: no columns! */
            builder.appendOrThrowError(parentElement, specToken, ErrorCode.TDETB2);
            /* We'll bail out here as nothing good will come of continuing! */
            return;
        }
        
        /* If we had a trailing '|' then that means we want a right border on the last column */
        if (borderFlag) {
            columnClasses.get(columnClasses.size()-1).add("right-border");
        }
        
        /* LaTeX gives an error if the table has more columns than specified here, so we'll
         * do the same.
         */
        if (columnClasses.size() < numColumns) {
            /* Error: More columns than expected */
            builder.appendOrThrowError(parentElement, specToken, ErrorCode.TDETB0,
                    Integer.valueOf(columnClasses.size()), Integer.valueOf(numColumns));
        }

        /* Build XHTML */
        Element tableElement = builder.appendXHTMLElement(parentElement, "table");
        builder.applyCSSStyle(tableElement, "tabular");
        
        Element tbodyElement = builder.appendXHTMLElement(tableElement, "tbody");
        Element trElement, tdElement;
        int columnIndex, columnsInRow;
        for (FlowToken rowToken : token.getContent()) {
            trElement = builder.appendXHTMLElement(tbodyElement, "tr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            columnsInRow = columns.size();
            
            /* Create a cell for each entry. Note that some entries may be empty if nothing
             * has been specified for them and not all with have corresponding column specs.
             */
            for (columnIndex=0; columnIndex<numColumns; columnIndex++) {
                tdElement = builder.appendXHTMLElement(trElement, "td");
                
                List<String> tdClasses = new ArrayList<String>();
                tdClasses.add("tabular");
                if (columnIndex<columnClasses.size()) {
                    tdClasses.addAll(columnClasses.get(columnIndex));
                }
                builder.applyCSSStyle(tdElement, tdClasses.toArray(new String[tdClasses.size()]));
                if (columnIndex<columnsInRow) {
                    builder.handleTokens(tdElement, ((CommandToken) columns.get(columnIndex)).getArguments()[0].getContents(), true);
                }
            }
        }
    }
     
    /**
     * Computes the dimensions of the table by looking at its content.
     * 
     * @param tableContent content of the <tt>tabular</tt> environment.
     * 
     * @return { rowCount, columnCount } pair
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
