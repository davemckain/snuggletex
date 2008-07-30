/* $Id: SnuggleTeXConfiguration.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.jeuclid;

import uk.ac.ed.ph.snuggletex.conversion.AbstractWebPageBuilderOptions;

/**
 * Options Object for {@link JEuclidWebPageBuilder}.
 * 
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class JEuclidWebPageBuilderOptions extends AbstractWebPageBuilderOptions {
    
    /** Callback to use when saving out any images produced by the process. */
    private MathMLImageSavingCallback imageSavingCallback;
    
    /**
     * Flag to set to output (backward-compatible) XHTML in preference to HTML.
     * <p>
     * (This only has an effect if you are using an XSLT 2.0 processor since the "xhtml" will
     * be used in this case; otherwise trying to output properly backward-compatible XHTML
     * using a XSLT 1.0 is not worth trying.)
     */
    private boolean outputtingXHTML;
    
    public JEuclidWebPageBuilderOptions() {
        super();
        this.imageSavingCallback = null;
        this.outputtingXHTML = false;
    }
    
    public MathMLImageSavingCallback getImageSavingCallback() {
        return imageSavingCallback;
    }

    public void setImageSavingCallback(MathMLImageSavingCallback callback) {
        this.imageSavingCallback = callback;
    }

    
    public boolean isOutputtingXHTML() {
        return outputtingXHTML;
    }
    
    public void setOutputtingXHTML(boolean outputtingXHTML) {
        this.outputtingXHTML = outputtingXHTML;
    }
}
