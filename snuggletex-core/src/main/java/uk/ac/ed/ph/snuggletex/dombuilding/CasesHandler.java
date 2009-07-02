/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Handles the AMSLaTeX <tt>cases</tt> environment.
 * <p>
 * This can only be used in MATH mode and generates a <tt>mtable</tt> as a result.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class CasesHandler implements EnvironmentHandler {
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        /* Create outer <mfenced/> */
        Element mfencedElement = builder.appendMathMLElement(parentElement, "mfenced");
        mfencedElement.setAttribute("open", "{");
        mfencedElement.setAttribute("close", "");
        
        /* Now we generate the resulting <mtable/> inside the <mfrenced/> */
        Element mtableElement = builder.appendMathMLElement(mfencedElement, "mtable");
        Element mtrElement, mtdElement;
        FlowToken columnToken;
        int rowColumns;
        for (FlowToken rowToken : token.getContent()) {
            mtrElement = builder.appendMathMLElement(mtableElement, "mtr");
            List<FlowToken> columns = ((CommandToken) rowToken).getArguments()[0].getContents();
            rowColumns = columns.size();
            if (rowColumns>2) {
                /* Error: Each row in a cases environment must have no more than 2 columns */
                builder.appendOrThrowError(mtrElement, rowToken, ErrorCode.TDEMC0);
                continue;
            }
            for (int i=0; i<2 && i<rowColumns; i++) {
                columnToken = columns.get(i);
                mtdElement = builder.appendMathMLElement(mtrElement, "mtd");
                builder.handleTokens(mtdElement, ((CommandToken) columnToken).getArguments()[0].getContents(), true);
            }
            /* Add empty <td/> for missing columns */
            for (int i=0; i<2-rowColumns; i++) {
                builder.appendMathMLElement(mtrElement, "mtd");
            }
        }
    }
}
