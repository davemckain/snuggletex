/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.jeuclid;

import uk.ac.ed.ph.snuggletex.BaseWebPageOptions;

/**
 * Options Object for {@link JEuclidWebPageBuilder}.
 * 
 * @author  David McKain
 * @version $Revision$
 */
@Deprecated
public final class JEuclidWebPageOptions extends BaseWebPageOptions {
    
    /** Callback to use when saving out any images produced by the process. */
    private MathMLImageSavingCallback imageSavingCallback;
    
    public JEuclidWebPageOptions() {
        super();
        this.imageSavingCallback = null;
    }
    
    public MathMLImageSavingCallback getImageSavingCallback() {
        return imageSavingCallback;
    }

    public void setImageSavingCallback(MathMLImageSavingCallback callback) {
        this.imageSavingCallback = callback;
    }
}
