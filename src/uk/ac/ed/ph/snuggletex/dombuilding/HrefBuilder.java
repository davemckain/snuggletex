/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import org.w3c.dom.Element;

/**
 * Handles the SnuggleTeX-specific <tt>\\href</tt> command for easily creating web links.
 * 
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class HrefBuilder implements CommandHandler {
    
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        /* Create <a> element with correct href attribute */
        String href = token.getArguments()[0].getSlice().extract().toString();
        Element aElement = builder.appendXHTMLElement(parentElement, "a");
        aElement.setAttribute("href", href);
        
        /* Now show link text, which is other provided explicitly via optional argument or
         * will just be the same as the 'href'
         */
        ArgumentContainerToken optionalArgument = token.getOptionalArgument();
        if (optionalArgument!=null) {
            builder.handleTokens(aElement, optionalArgument, true);
        }
        else {
            builder.appendTextNode(aElement, href, true);
        }
    }
}
