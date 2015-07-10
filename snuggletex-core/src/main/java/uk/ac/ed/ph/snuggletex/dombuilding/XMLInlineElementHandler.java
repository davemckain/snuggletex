/* Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

/**
 * Builds custom inline XML elements.
 *
 * @author  David McKain
 */
public final class XMLInlineElementHandler extends AbstractCustomXMLElementHandler {
    
    @Override
    protected boolean isBlock() {
        return false;
    }
}
