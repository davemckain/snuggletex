/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;

/**
 * Trivial helper class to manage the loading of SnuggleTeX's internal stylesheets, using
 * the optional {@link StylesheetCache} to cache stylesheets for performance.
 * <p>
 * This has been made "public" as it is used by certain standalone tools, like the
 * {@link MathMLDownConverter}, but its use outside SnuggleTeX is somewhat limited.
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
     * @param classPathUri location of the XSLT stylesheet in the ClassPath, following the
     *   URI scheme in {@link ClassPathURIResolver}.
     *   
     * @return compiled XSLT stylesheet.
     */
    public Templates getStylesheet(String classPathUri) {
        Templates result;
        if (stylesheetCache==null) {
            result = compileStylesheet(classPathUri);
        }
        else {
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(classPathUri);
                if (result==null) {
                    result = compileStylesheet(classPathUri);
                    stylesheetCache.putStylesheet(classPathUri, result);
                }
            }
        }
        return result;
    }
    
    private Templates compileStylesheet(String classPathUri) {
        /* (Can use JAXP default here) */
        TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
        return XMLUtilities.compileInternalStylesheet(transformerFactory, classPathUri);
    }
}
