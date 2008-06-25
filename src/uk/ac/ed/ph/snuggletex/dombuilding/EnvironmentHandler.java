/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Defines how a {@link BuiltinEnvironment} should append children to the outgoing DOM.
 * <p>
 * An instance of this interface must be stateless once created.
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface EnvironmentHandler {
    
	/**
	 * Called when a {@link CommandToken} is being handled by the {@link DOMBuilder}.
	 * 
	 * @param builder {@link DOMBuilder} running this process, which provides access
	 *   to convenience method for appending Nodes to the DOM
	 * @param parentElement parent Element that the resulting Nodes should (can) be
	 *   added to
	 * @param token Token representing the environment being processed.
	 */
    void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
        throws DOMException, SnuggleParseException;

}
