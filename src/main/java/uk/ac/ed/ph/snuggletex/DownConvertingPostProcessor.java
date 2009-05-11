/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.utilities.MathMLDownConverter;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import org.w3c.dom.Document;

/**
 * Trivial implementation of {@link DOMPostProcessor} that hooks into the
 * {@link MathMLDownConverter}. See {@link MathMLDownConverter} for information on what
 * this does.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class DownConvertingPostProcessor implements DOMPostProcessor {
    
    public Document postProcessDOM(Document workDocument, DOMOutputOptions options,
            StylesheetManager stylesheetManager) {
        MathMLDownConverter downConverter = new MathMLDownConverter(stylesheetManager, options);
        return downConverter.downConvertDOM(workDocument);
    }

}
