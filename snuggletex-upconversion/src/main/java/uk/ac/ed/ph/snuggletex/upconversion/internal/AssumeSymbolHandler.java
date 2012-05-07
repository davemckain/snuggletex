/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.internal;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.upconversion.IllegalUpconversionOptionException;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionOptions;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionUtilities;

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
public final class AssumeSymbolHandler extends UpConversionHandlerBase {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* First argument is the target symbol that this assumption will apply to, which is
         * a blob of MathML. This will be "checked" in the XSLT to make sure we can handle
         * this correctly.
         */
        builder.setOutputContext(parentElement, OutputContext.MATHML_INLINE);
        NodeList assumptionTargetRaw = builder.extractNodeListValue(token.getArguments()[0]);
        builder.setOutputContext(parentElement, OutputContext.XHTML);
        Element assumptionTarget = ensureLegalSymbolTarget(builder, parentElement, token, assumptionTargetRaw);
        if (assumptionTarget==null) {
            return;
        }
        
        /* Second argument is the property being assumed */
        String assumptionType = builder.extractStringValue(token.getArguments()[1]);
        
        /* Make the change */
        UpConversionOptions options = ensureGetAuthorUpconversionOptions(builder);
        try {
            options.assumeSymbol(assumptionTarget, assumptionType);
        }
        catch (IllegalUpconversionOptionException e) {
            builder.appendOrThrowError(parentElement, token, e.getErrorCode(), (Object[]) e.getArguments());
            return;
        }
        
        /* Now output all current assumptions for the XSLT to use */
        UpConversionUtilities.appendUpConversionOptionsElement(builder.getDocument(), parentElement, options, false);
    }
}
