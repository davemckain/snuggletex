/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import org.w3c.dom.Document;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConvertingPostProcessor implements DOMPostProcessor {
    
    public Document postProcessDOM(Document workDocument, DOMOutputOptions options,
            StylesheetManager stylesheetManager) {
        return new MathMLUpConverter(stylesheetManager.getStylesheetCache()).upConvertSnuggleTeXMathML(workDocument);
    }

}
