/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

/**
 * Builds custom block XML elements.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class XMLBlockElementBuilder extends AbstractCustomXMLElementBuilder {
    
    @Override
    protected boolean isBlock() {
        return true;
    }
}
