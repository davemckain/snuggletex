/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.jeuclid;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.DownConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsBuilder;

/**
 * Some utility methods for using the JEuclid-based "MathML to Image" conversion
 * functionality.
 *
 * @see WebPageOutputOptionsBuilder
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class JEuclidUtilities {
    
    /**
     * Takes an existing {@link DOMOutputOptions} and configures it to convert
     * MathML to images using JEuclid, optionally down-converting simple expressions to HTML+CSS
     * beforehand.
     * <p>
     * This works by <strong>replacing</strong> any existing {@link DOMPostProcessor}s. If you
     * want to use other {@link DOMPostProcessor}s, then you will have to work out whether they
     * fit in with this process and configure things manually.
     * 
     * @param options existing {@link DOMOutputOptions} Object
     * @param downConvertFirst
     * @param callback
     */
    public static void setupJEuclidPostProcessors(DOMOutputOptions options,
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
    
    /**
     * Creates a new {@link WebPageOutputOptions} suitably configured for converting MathML
     * to images, with optional down-conversion.
     * 
     * As of SnuggleTeX 1.3.0, this simply calls #createXHTMLWebPageOptions(). This method
     * will be removed in SnuggleTeX 1.4.
     * 
     * @deprecated Use one of the explicit
     *   {@link #createXHTMLWebPageOptions(boolean, MathMLImageSavingCallback)},
     *   {@link #createHTML4WebPageOptions(boolean, MathMLImageSavingCallback)}
     *   or {@link #createHTML5WebPageOptions(boolean, MathMLImageSavingCallback)} methods now.
     * 
     * @param downConvertFirst
     * @param callback
     */
    @Deprecated
    public static WebPageOutputOptions createWebPageOptions(boolean downConvertFirst, MathMLImageSavingCallback callback) {
        return createXHTMLWebPageOptions(downConvertFirst, callback);
    }
    
    /**
     * Creates a new {@link WebPageOutputOptions} suitably configured for to convert MathML
     * to images, with optional down-conversion. The resulting web page will be XHTML.
     * 
     * @param downConvertFirst
     * @param callback
     */
    public static WebPageOutputOptions createXHTMLWebPageOptions(boolean downConvertFirst, MathMLImageSavingCallback callback) {
        WebPageOutputOptions options = WebPageOutputOptionsBuilder.createXHTMLOptions();
        setupJEuclidPostProcessors(options, downConvertFirst, callback);
        return options;
    }
    
    /**
     * Creates a new {@link WebPageOutputOptions} suitably configured for to convert MathML
     * to images, with optional down-conversion. The resulting web page will be HTML 4.
     * 
     * @param downConvertFirst
     * @param callback
     */
    public static WebPageOutputOptions createHTML4WebPageOptions(boolean downConvertFirst, MathMLImageSavingCallback callback) {
        WebPageOutputOptions options = WebPageOutputOptionsBuilder.createHTML4Options();
        setupJEuclidPostProcessors(options, downConvertFirst, callback);
        return options;
    }
    
    /**
     * Creates a new {@link WebPageOutputOptions} suitably configured for to convert MathML
     * to images, with optional down-conversion. The resulting web page will be HTML 5.
     * 
     * @param downConvertFirst
     * @param callback
     */
    public static WebPageOutputOptions createHTML5WebPageOptions(boolean downConvertFirst, MathMLImageSavingCallback callback) {
        WebPageOutputOptions options = WebPageOutputOptionsBuilder.createHTML5Options();
        setupJEuclidPostProcessors(options, downConvertFirst, callback);
        return options;
    }
}