/* $Id: EnvironmentHandler.java,v 1.2 2008/04/18 09:44:05 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public interface EnvironmentHandler {
    
    void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
        throws DOMException, SnuggleParseException;

}
