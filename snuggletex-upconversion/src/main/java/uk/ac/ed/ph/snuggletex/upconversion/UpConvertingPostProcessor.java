/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import org.w3c.dom.Document;

/**
 * Implementation of {@link DOMPostProcessor} that bootstraps into {@link MathMLUpConverter},
 * providing the functionality offered by it.
 * 
 * @since 1.1.0
 * 
 * @see MathMLUpConverter
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConvertingPostProcessor implements DOMPostProcessor {
    
    private UpConversionOptions upconversionOptions;
    
    public UpConvertingPostProcessor() {
        this(null);
    }
    
    public UpConvertingPostProcessor(UpConversionOptions upconversionParameterMap) {
        this.upconversionOptions = upconversionParameterMap;
    }

    public UpConversionOptions getUpconversionParameterMap() {
        return upconversionOptions;
    }
    
    public void setUpconversionParameterMap(UpConversionOptions upconversionParameterMap) {
        this.upconversionOptions = upconversionParameterMap;
    }
    
    public Document postProcessDOM(Document workDocument, DOMOutputOptions unused,
            StylesheetManager stylesheetManager) {
        return new MathMLUpConverter(stylesheetManager)
            .upConvertSnuggleTeXMathML(workDocument, upconversionOptions);
    }
}
