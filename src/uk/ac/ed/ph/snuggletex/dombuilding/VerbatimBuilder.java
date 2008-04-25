/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.FrozenSlice;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class VerbatimBuilder implements EnvironmentHandler {
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws DOMException {
        FrozenSlice verbatimContentSlice = token.getContent().getSlice();
        Element verbatimElement = builder.appendXHTMLElement(parentElement, "pre");
        builder.appendTextNode(verbatimElement, verbatimContentSlice.extract().toString(), false);
    }
}
