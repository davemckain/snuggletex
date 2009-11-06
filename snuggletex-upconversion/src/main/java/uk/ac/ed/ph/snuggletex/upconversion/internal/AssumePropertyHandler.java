/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.internal;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionErrorCode;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * Handler for the <tt>\\assumeProperty{name}{value}</tt> command.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class AssumePropertyHandler extends AssumeHandlerBase {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* First argument is property name */
        String propertyName = builder.extractStringValue(token.getArguments()[0]);
        if (!PROPERTY_ASSUMPTION_DEFS.containsKey(propertyName)) {
            /* Error: Unrecognised property name */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAEP00, propertyName);
            return;
        }

        /* Second argument is the property being assumed */
        String propertyValue = builder.extractStringValue(token.getArguments()[1]);
        Set<String> allowedValues = PROPERTY_ASSUMPTION_DEFS.get(propertyName);
        if (allowedValues!=null && !allowedValues.contains(propertyValue)) {
            /* Error: Unrecognised value for property */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAEP01, propertyName, propertyValue);
            return;
        }
        
        /* Retrieve bracket assumptions map */
        Map<String, String> propertyAssumptionsMap = getPropertyAssumptionsMap(builder);

        /* Wrap up the target for storing in the assumptions map */
        propertyAssumptionsMap.put(propertyName, propertyValue);
        
        /* Now output all current assumptions */
        buildAssumptionsElement(builder, parentElement);
    }
}
