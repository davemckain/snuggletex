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
 * Handler for the <tt>\\assumeSymbol{target}{property}</tt> command.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class AssumeSymbolHandler extends AssumeHandlerBase {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* First argument is the target symbol that this assumption will apply to, which is
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
        
        /* Second argument is the property being assumed */
        String assumptionType = builder.extractStringValue(token.getArguments()[1]);
        if (!SYMBOL_ASSUMPTION_TYPES.contains(assumptionType)) {
            /* Error: Unrecognised assumption property for symbol */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAES00, assumptionType);
            return;
        }
        
        /* Retrieve symbol assumptions map */
        Map<ElementMapKeyWrapper, String> symbolAssumptionsMap = getSymbolAssumptionsMap(builder);

        /* Wrap up the target for storing in the assumptions map */
        ElementMapKeyWrapper symbolTarget = new ElementMapKeyWrapper(assumptionTarget);
        symbolAssumptionsMap.put(symbolTarget, assumptionType);
        
        /* Now output all current assumptions */
        buildAssumptionsElement(builder, parentElement);
    }
}
