/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.jeuclid;

import java.io.File;

import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.LayoutContext.Parameter;
import net.sourceforge.jeuclid.context.LayoutContextImpl;

/**
 * Partial convenience implementation of {@link MathMLImageSavingCallback} that
 * assumes that the same Content Type will be used to produce each MathML image and
 * restricts the number of configurable features somewhat.
 *
 * @author  David McKain
 * @version $Revision$
 */
public abstract class SimpleMathMLImageSavingCallback implements MathMLImageSavingCallback {
    
    private String fontSize;
    private boolean antiAliasing;
    private String imageContentType;
    
    private final LayoutContextImpl layoutContext;
    
    public SimpleMathMLImageSavingCallback() {
        this.layoutContext = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
        setFontSize("16.0");
        setAntiAliasing(true);
        setImageContentType("image/png");
    }
    
    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
        this.layoutContext.setParameter(Parameter.MATHSIZE, Float.valueOf(fontSize));
    }


    public boolean isAntiAliasing() {
        return antiAliasing;
    }

    public void setAntiAliasing(boolean antiAliasing) {
        this.antiAliasing = antiAliasing;
        this.layoutContext.setParameter(Parameter.ANTIALIAS, Boolean.valueOf(antiAliasing));
    }

    
    public String getImageContentType() {
        return imageContentType;
    }
    
    public void setImageContentType(String imageType) {
        this.imageContentType = imageType;
    }
    
    //----------------------------------------------------
    
    public final String getImageContentType(int mathmlCounter) {
        return imageContentType;
    }
    
    public final MutableLayoutContext getLayoutContext(int mathmlCounter) {
        return layoutContext;
    }
    
    public abstract File getImageOutputFile(int mathmlCounter);
    
    public abstract String getImageURL(int mathmlCounter);
    
    public void imageSavingSucceeded(File imageFile, int mathmlCounter, String contentType) {
        /* (Do nothing by default) */
    }

}
