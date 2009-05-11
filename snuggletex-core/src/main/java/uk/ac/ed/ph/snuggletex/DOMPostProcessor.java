/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.extensions.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import org.w3c.dom.Document;

/**
 * This interface allows you to hook into the SnuggleTeX process immediately after the raw
 * DOM tree has been built. By implementing this interface, you may make modifications to the
 * DOM to suit your needs.
 * <p>
 * SnuggleTeX ships with a {@link DownConvertingPostProcessor} and (as an extension) an
 * {@link UpConvertingPostProcessor}.
 * 
 * @see DownConvertingPostProcessor
 * @see UpConvertingPostProcessor
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface DOMPostProcessor {
   
    /**
     * Implement this to post-process the raw DOM produced by SnuggleTeX.
     * <p>
     * You will be given an entire DOM Document to work with, containing a root element
     * called <tt>root</tt> in the {@link SnuggleConstants#SNUGGLETEX_NAMESPACE} namespace.
     * <p>
     * You should create a new Document and return it. The children of the root element will
     * end up being the Nodes added to the final DOM.
     */
    Document postProcessDOM(Document workDocument, DOMOutputOptions options,
            StylesheetManager stylesheetManager);

}
