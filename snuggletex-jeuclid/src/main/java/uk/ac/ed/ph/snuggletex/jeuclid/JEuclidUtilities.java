/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.jeuclid;

import uk.ac.ed.ph.snuggletex.DownConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsTemplates;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;

/**
 * FIXME: Document this type!
 *
 * @see WebPageOutputOptionsTemplates
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class JEuclidUtilities {
    
    public static WebPageOutputOptions createWebPageOptions(boolean downConvertFirst, MathMLImageSavingCallback callback) {
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(WebPageType.PROCESSED_HTML);
        setupJEuclidPostProcessors(options, downConvertFirst, callback);
        return options;
    }
    
    public static void setupJEuclidPostProcessors(WebPageOutputOptions options,
            boolean downConvertFirst, MathMLImageSavingCallback callback) {
        if (downConvertFirst) {
            options.setDOMPostProcessors(
                    new DownConvertingPostProcessor(),
                    new JEuclidMathMLPostProcessor(callback)
            );
        }
        else {
            options.setDOMPostProcessors(new JEuclidMathMLPostProcessor(callback));
        }
    }
}