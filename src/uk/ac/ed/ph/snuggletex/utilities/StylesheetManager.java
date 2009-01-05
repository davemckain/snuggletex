/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;

import java.io.InputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Trivial helper class to manage the loading of SnuggleTeX's internal stylesheets, using
 * the optional {@link StylesheetCache} to cache stylesheets for performance.
 * <p>
 * This has been made "public" as it is used by certain standalone tools, like the
 * {@link MathMLDownConverter}.
 * <p>
 * This class is thread-safe.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class StylesheetManager {
    
    private StylesheetCache stylesheetCache;
    
    public StylesheetManager() {
        this(null);
    }
    
    public StylesheetManager(final StylesheetCache cache) {
        this.stylesheetCache = cache;
    }
    
    //----------------------------------------------------------
    
    public StylesheetCache getStylesheetCache() {
        return stylesheetCache;
    }

    public void setStylesheetCache(StylesheetCache stylesheetCache) {
        this.stylesheetCache = stylesheetCache;
    }
    
    //----------------------------------------------------------

    /**
     * Helper method to retrieve an XSLT stylesheet from the {@link StylesheetCache}, compiling
     * and storing one if the cache fails to return anything.
     * 
     * @param resourceName location of the XSLT stylesheet in the ClassPath.
     * @return compiled XSLT stylesheet.
     */
    public Templates getStylesheet(String resourceName) {
        Templates result;
        if (stylesheetCache==null) {
            result = compileStylesheet(resourceName);
        }
        else {
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(resourceName);
                if (result==null) {
                    result = compileStylesheet(resourceName);
                    stylesheetCache.putStylesheet(resourceName, result);
                }
            }
        }
        return result;
    }
    
    private Templates compileStylesheet(String resourceName) {
        TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
        InputStream xslStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        try {
            return transformerFactory.newTemplates(new StreamSource(xslStream));
        }
        catch (TransformerConfigurationException e) {
            throw new SnuggleRuntimeException("Could not compile SnuggleTeX XSLT stylesheet at "
                    + resourceName, e);
        }
    }
}
