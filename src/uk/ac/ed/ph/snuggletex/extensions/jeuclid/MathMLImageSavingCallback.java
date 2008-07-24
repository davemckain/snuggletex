/* $Id: MasterNoMathMLViewStrategy.java 2712 2008-03-10 17:01:01Z davemckain $
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.jeuclid;

import java.io.File;

import org.w3c.dom.Document;

import net.sourceforge.jeuclid.MutableLayoutContext;

/**
 * Trivial callback interface used by {@link JEuclidMathMLConversionVisitor} to determine
 * where to save each generating MathML image and the URL to use in the resulting
 * HTML as it traverses a given DOM {@link Document}.
 * <p>
 * You should implement this to do whatever is appropriate for your needs.
 * <p>
 * This can be used independently of SnuggleTeX.
 * 
 * @see JEuclidMathMLConversionVisitor
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public interface MathMLImageSavingCallback {
    
    /**
     * Implement to return the desired MIME type for the resulting image.
     * <p>
     * Consult the JEuclid documentation for what is supported; you may need to add additional
     * libraries for certain MIME types.
     * <p>
     * SnuggleTeX contains everything required to support PNG. Note that conversion to GIF
     * does not generally work well so should be avoided...!
     * 
     * @param mathmlCounter identifies the position of the image within the document being processed,
     *   which can be used to ensure unique file names.
     */
    String getImageContentType(int mathmlCounter);
	
    /**
     * Implement to return the {@link File} that the given image should be saved to. This
     * must be writable.
     * 
     * @param mathmlCounter identifies the position of the image within the document being processed,
     *   which can be used to ensure unique file names.
     */
	File getImageOutputFile(int mathmlCounter);
	
    /**
     * Implement to return the URL String that will be put into the <tt>img</tt> <tt>src</tt>
     * attribute to refer to the image.
     * 
     * @param mathmlCounter identifies the position of the image within the document being processed,
     *   which can be used to ensure unique file names.
     */
	String getImageURL(int mathmlCounter);

	/**
	 * Implement to fill in the JEuclid {@link MutableLayoutContext} specifying how you want to
	 * render this image.
	 * 
	 * @param mathmlCounter identifies the position of the image within the document being processed,
     *   which can be used to ensure unique file names.
	 */
	MutableLayoutContext getLayoutContext(int mathmlCounter);
	
	/**
	 * Called back once a MathML image has been saved successfully. Implementors can do anything they
	 * need to do at this point.
	 * 
	 * @param imageFile saved image File
	 * @param mathmlCounter identifies the position of the image within the document being processed,
     *   which can be used to ensure unique file names.
     * @param contentType content type of the saved image File
	 */
	void imageSavingSucceeded(File imageFile, int mathmlCounter, String contentType);
	
	/**
     * Called back if MathML image could not be saved for some reason.
     * 
     * @param imageFile saved image File
     * @param mathmlCounter identifies the position of the image within the document being processed,
     *   which can be used to ensure unique file names.
     * @param contentType content type of the saved image File
     * @param exception cause of the failure, which may be null
     */
	void imageSavingFailed(File imageFile, int mathmlCounter, String contentType, Throwable exception);
}