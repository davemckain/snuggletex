/* $Id: XMLInlineElementBuilder.java,v 1.1 2008/01/18 11:02:56 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public final class XMLInlineElementBuilder extends AbstractCustomXMLElementBuilder {
    
    @Override
    protected boolean isBlock() {
        return false;
    }
}
