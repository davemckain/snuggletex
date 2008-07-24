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
    
    private MathMLImageSavingCallback imageSavingCallback;
    
    public JEuclidWebPageBuilderOptions() {
        super();
    }
    
	public MathMLImageSavingCallback getImageSavingCallback() {
		return imageSavingCallback;
	}

	public void setImageSavingCallback(MathMLImageSavingCallback callback) {
		this.imageSavingCallback = callback;
	}
}
