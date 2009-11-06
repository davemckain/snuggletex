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

import org.w3c.dom.Element;

/**
 * Handler for the <tt>\\unassumeProperty{name}</tt> command.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UnassumePropertyHandler extends AssumeHandlerBase {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* Argument is the type of brackets */
        String propertyName = builder.extractStringValue(token.getArguments()[0]);
        if (!PROPERTY_ASSUMPTION_DEFS.containsKey(propertyName)) {
            /* Error: Unrecognised property name */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAEP00, propertyName);
            return;
        }

        /* Retrieve bracket assumptions map */
        Map<String, String> propertyAssumptionsMap = getPropertyAssumptionsMap(builder);
        
        /* Remove assumption from map, raising an error if nothing is already assumed */
        if (propertyAssumptionsMap.containsKey(propertyName)) {
            propertyAssumptionsMap.remove(propertyName);
        }
        else {
            /* Error: nothing assumed about these brackets */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAEP02, propertyName);
        }
        
        /* Now output all current assumptions */
        buildAssumptionsElement(builder, parentElement);
    }
}
