/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
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

import org.w3c.dom.Element;

/**
 * Trivial handler for the <tt>\\xmlName</tt> command. All this does is check that the argument
 * is a proper XML name and then handles it as usual.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class XMLNameHandler implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        ArgumentContainerToken nameArgument = token.getArguments()[0];
        String rawName = nameArgument.getSlice().extract().toString().trim();

        /* Check name. This is easy since our input is good old ASCII so the XML Name production
         * simplifies to the regexp below...
         */
        if (!rawName.matches("[a-zA-Z_:][a-zA-Z0-9_:.-]*")) {
            /* Error: Bad XML Name */
            builder.appendOrThrowError(parentElement, nameArgument, ErrorCode.TDEX03, rawName);
        }
        
        /* Then just append to output as normal */
        builder.appendTextNode(parentElement, rawName, true);
    }
}
