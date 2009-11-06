/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.internal;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionErrorCode;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handler for the <tt>\\unassumeSymbol{target}</tt> command.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UnassumeSymbolHandler extends AssumeHandlerBase {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* First argument is the symbol that this assumption will apply to, which is
         * a blob of MathML. This will be "checked" in the XSLT to make sure we can handle
         * this correctly.
         */
        builder.pushOutputContext(OutputContext.MATHML_INLINE);
        NodeList assumptionTargetRaw = builder.extractNodeListValue(token.getArguments()[0]);
        builder.popOutputContext();
        Element assumptionTarget = ensureLegalSymbolTarget(builder, parentElement, token, assumptionTargetRaw);
        if (assumptionTarget==null) {
            return;
        }
        
        /* Retrieve symbol assumptions map */
        Map<ElementMapKeyWrapper, String> symbolAssumptionsMap = getSymbolAssumptionsMap(builder);
        
        /* Remove assumption from map, raising an error if nothing is already assumed */
        ElementMapKeyWrapper symbolTarget = new ElementMapKeyWrapper(assumptionTarget);
        if (symbolAssumptionsMap.containsKey(symbolTarget)) {
            symbolAssumptionsMap.remove(symbolTarget);
        }
        else {
            /* Error: nothing assumed about symbol */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAES02);
        }
        
        /* Now output all current assumptions */
        buildAssumptionsElement(builder, parentElement);
    }
}
