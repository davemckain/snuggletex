/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;

/**
 * Trivial helper class to manage the loading of SnuggleTeX's internal stylesheets, using
 * a {@link StylesheetCache} to cache stylesheets for performance.
 * <p>
 * This has been made "public" as it is used by certain standalone tools, like the
 * {@link MathMLDownConverter}, but its use outside SnuggleTeX is perhaps somewhat limited.
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
     * Helper method to retrieve an XSLT stylesheet from the {@link StylesheetCache} using the
     * JAXP Default {@link TransformerFactory}, compiling and storing one if the cache fails to
     * return anything.
     * 
     * @param classPathUri location of the XSLT stylesheet in the ClassPath, following the
     *   URI scheme in {@link ClassPathURIResolver}.
     *   
     * @return compiled XSLT stylesheet.
     */
    public Templates getStylesheet(final String classPathUri) {
        return getStylesheet(classPathUri, false);
    }
    
    /**
     * Helper method to retrieve an XSLT stylesheet from the {@link StylesheetCache}, using
     * the given {@link TransformerFactory}, compiling and storing a stylesheet
     * if the cache fails to return anything.
     * 
     * @param classPathUri location of the XSLT stylesheet in the ClassPath, following the
     *   URI scheme in {@link ClassPathURIResolver}.
     *   
     * @return compiled XSLT stylesheet.
     */
    public Templates getStylesheet(final String classPathUri, final TransformerFactory transformerFactory) {
        Templates result;
        if (stylesheetCache==null) {
            result = compileStylesheet(classPathUri, transformerFactory);
        }
        else {
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(classPathUri);
                if (result==null) {
                    result = compileStylesheet(classPathUri, transformerFactory);
                    stylesheetCache.putStylesheet(classPathUri, result);
                }
            }
        }
        return result;
    }
    
    /**
     * Helper method to retrieve an XSLT stylesheet from the {@link StylesheetCache}, using either the
     * JAXP Default {@link TransformerFactory} or SAXON (if requireXSLT20 is true),
     * compiling and storing one if the cache fails to return anything.
     * 
     * @param classPathUri location of the XSLT stylesheet in the ClassPath, following the
     *   URI scheme in {@link ClassPathURIResolver}.
     * @param requireXSLT20 if false uses the JAXP default {@link TransformerFactory}, otherwise
     *   specifies that we require an XSLT 2.0-compliant transformer, of which the only currently
     *   supported implementation is SAXON 9.x.
     *   
     * @return compiled XSLT stylesheet.
     */
    public Templates getStylesheet(final String classPathUri, final boolean requireXSLT20) {
        return getStylesheet(classPathUri, createTransformerFactory(requireXSLT20));
    }
    
    private TransformerFactory createTransformerFactory(final boolean requireXSLT20) {
        TransformerFactory transformerFactory;
        if (requireXSLT20) {
            transformerFactory = XMLUtilities.createSaxonTransformerFactory();
        }
        else {
            transformerFactory = XMLUtilities.createJAXPTransformerFactory();
        }
        return transformerFactory;
    }
    
    private Templates compileStylesheet(final String classPathUri, final TransformerFactory transformerFactory) {
        return XMLUtilities.compileInternalStylesheet(transformerFactory, classPathUri);
    }
}
