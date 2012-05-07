/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import org.w3c.dom.Element;

/**
 * Handles the internal <![CDATA[<style>]]> environment delimiting a branch of content to be
 * rendered in a different style.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class StyleHandler implements EnvironmentHandler {
    
    public void handleEnvironment(DOMBuilder builder, Element parentElement, EnvironmentToken token)
            throws SnuggleParseException {
        ComputedStyle newStyle = token.getComputedStyle();
        ArgumentContainerToken contentContainerToken = token.getContent();
        boolean hasEmptyContent = contentContainerToken.getContents().isEmpty();
        boolean hasBlockContent = false;
        for (FlowToken contentToken : contentContainerToken) {
            if (contentToken.getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK) {
                hasBlockContent = true;
            }
        }
        Element styleContainer = builder.openStyle(parentElement, newStyle, hasBlockContent, hasEmptyContent);
        builder.handleTokens(styleContainer, contentContainerToken, false);
        builder.closeStyle();
    }
}
