/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.util.Map;

import org.w3c.dom.Document;

/**
 * Implementation of {@link DOMPostProcessor} that bootstraps into {@link MathMLUpConverter},
 * providing the functionality offered by it.
 * 
 * @see MathMLUpConverter
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConvertingPostProcessor implements DOMPostProcessor {
    
    private Map<String, Object> upconversionParameterMap;
    
    public UpConvertingPostProcessor() {
        this(null);
    }
    
    public UpConvertingPostProcessor(Map<String, Object> upconversionParameterMap) {
        this.upconversionParameterMap = upconversionParameterMap;
    }

    public Map<String, Object> getUpconversionParameterMap() {
        return upconversionParameterMap;
    }
    
    public void setUpconversionParameterMap(Map<String, Object> upconversionParameterMap) {
        this.upconversionParameterMap = upconversionParameterMap;
    }
    
    public Document postProcessDOM(Document workDocument, DOMOutputOptions unused,
            StylesheetManager stylesheetManager) {
        return new MathMLUpConverter(stylesheetManager.getStylesheetCache())
            .upConvertSnuggleTeXMathML(workDocument, upconversionParameterMap);
    }
}
