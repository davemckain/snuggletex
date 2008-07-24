/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Handles lonely instances of <tt>\\xmlAttr</tt>, which results in {@link ErrorCode#TDEX02}
 * being emitted.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class XMLAttrHandler implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        builder.appendOrThrowError(parentElement, token, ErrorCode.TDEX02);
    }

}
