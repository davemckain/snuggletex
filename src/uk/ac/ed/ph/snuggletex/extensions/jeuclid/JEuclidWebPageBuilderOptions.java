/* $Id: SnuggleTeXConfiguration.java 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.jeuclid;

import uk.ac.ed.ph.snuggletex.conversion.BaseWebPageBuilderOptions;

import java.io.File;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class JEuclidWebPageBuilderOptions extends BaseWebPageBuilderOptions {
    
	/**
	 * FIXME: Document this type!
	 * 
	 * FIXME: Maybe this interface should specify the image type, since it is usually
	 * going to be linked to the image name?
	 *
	 * @author  David McKain
	 * @version $Revision: 3 $
	 */
    public static interface ImageSaver {
    	
    	File getImageOutputFile(int mathmlCounter);
    	
    	String getImageURL(int mathmlCounter);
    }
    
    private boolean wantHTML;
    private ImageSaver imageSaver;
    private String fontSize;
    private boolean antiAliasing;
    
    public JEuclidWebPageBuilderOptions() {
        super();
        setEncoding("UTF-8");
        setLanguage("en");
        this.fontSize = "16.0";
        this.antiAliasing = true;
        this.wantHTML = false;
    }

    
	public ImageSaver getImageSaver() {
		return imageSaver;
	}

	public void setImageSaver(ImageSaver imageSaver) {
		this.imageSaver = imageSaver;
	}


	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}


	public boolean isAntiAliasing() {
		return antiAliasing;
	}

	public void setAntiAliasing(boolean antiAliasing) {
		this.antiAliasing = antiAliasing;
	}


	public boolean isWantHTML() {
		return wantHTML;
	}

	public void setWantHTML(boolean wantHTML) {
		this.wantHTML = wantHTML;
	}
}
