/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
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
    
    private TransformerFactoryChooser transformerFactoryChooser;
    private StylesheetCache stylesheetCache;
    
    public StylesheetManager() {
        this(DefaultTransformerFactoryChooser.getInstance(), null);
    }
    
    public StylesheetManager(StylesheetCache cache) {
        this(DefaultTransformerFactoryChooser.getInstance(), cache);
    }
    
    public StylesheetManager(TransformerFactoryChooser transformerFactoryChooser, StylesheetCache cache) {
        this.transformerFactoryChooser = transformerFactoryChooser;
        this.stylesheetCache = cache;
    }
    
    //----------------------------------------------------------
    
    public TransformerFactoryChooser getTransformerFactoryChooser() {
        return transformerFactoryChooser;
    }

    public void setTransformerFactoryChooser(TransformerFactoryChooser transformerFactoryChooser) {
        this.transformerFactoryChooser = transformerFactoryChooser;
    }

    
    public StylesheetCache getStylesheetCache() {
        return stylesheetCache;
    }

    public void setStylesheetCache(StylesheetCache stylesheetCache) {
        this.stylesheetCache = stylesheetCache;
    }
    
    //----------------------------------------------------------
    
    /**
     * Helper method to retrieve an XSLT stylesheet from the {@link StylesheetCache} using the
     * current {@link TransformerFactoryChooser} to determine which {@link TransformerFactory}
     * to use.
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
     * Helper method to retrieve an XSLT stylesheet from the {@link StylesheetCache}
     * using the current {@link TransformerFactoryChooser} to determine which {@link TransformerFactory}
     * to use.
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
        Templates result;
        if (stylesheetCache==null) {
            result = compileStylesheet(classPathUri, requireXSLT20);
        }
        else {
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(classPathUri);
                if (result==null) {
                    result = compileStylesheet(classPathUri, requireXSLT20);
                    stylesheetCache.putStylesheet(classPathUri, result);
                }
            }
        }
        return result;
    }
    
    private Templates compileStylesheet(final String classPathUri, final boolean requireXSLT20) {
        TransformerFactory transformerFactory = getTransformerFactory(requireXSLT20);
        return XMLUtilities.compileInternalStylesheet(transformerFactory, classPathUri);
    }
    
    public boolean supportsXSLT20() {
        ensureChooserSpecified();
        return transformerFactoryChooser.isXSLT20SupportAvailable();
    }
    
    public TransformerFactory getTransformerFactory(final boolean requireXSLT20) {
        ensureChooserSpecified();
        
        /* Choose appropriate TransformerFactory implementation */
        TransformerFactory transformerFactory;
        if (requireXSLT20) {
            transformerFactory = transformerFactoryChooser.getSuitableXSLT10TransformerFactory();
        }
        else {
            transformerFactory = transformerFactoryChooser.getSuitableXSLT20TransformerFactory();
        }
        
        /* Configure URIResolver */
        transformerFactory.setURIResolver(ClassPathURIResolver.getInstance());
        return transformerFactory;
    }
    
    private void ensureChooserSpecified() {
        if (transformerFactoryChooser==null) {
            throw new SnuggleRuntimeException("No TransformerFactoryChooser set on this StylesheetManager");
        }
    }
}
