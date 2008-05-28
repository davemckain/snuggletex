/* $Id: XMLUtilities.java 2724 2008-03-14 09:43:49Z davemckain $
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

/**
 * Some trivial little helpers for creating suitably-configured {@link TransformerFactory}
 * instances.
 * <p>
 * (This is based on similar utility methods in Aardvark.)
 *
 * @author David McKain
 * @version $Revision: 2724 $
 */
public final class XMLUtilities {

     /** 
     * Value of Saxon 8 FeatureKeys#VERSION_WARNING. This has been looked up and
     * pasted in here to avoid a compile-time dependency on Saxon 8.
     */ 
    private static final String SAXON_VERSION_WARNING_FEATURE_KEY = "http://saxon.sf.net/feature/version-warning";

    public static TransformerFactory createTransformerFactory() {
        TransformerFactory transformerFactory = null;
        try {
            transformerFactory = TransformerFactory.newInstance();
        }
        catch (TransformerFactoryConfigurationError e) {
            throw new SnuggleRuntimeException(e);
        }
        /* Make sure we have DOM-based features */
        requireFeature(transformerFactory, DOMSource.FEATURE);
        requireFeature(transformerFactory, DOMResult.FEATURE);
        
        /* If using Saxon 7+, we'll turn off warnings when running on XSLT 1.0 stylesheets */
        if (transformerFactory.getClass().getName().equals("net.sf.saxon.TransformerFactoryImpl")) {
            try {
                transformerFactory.setAttribute(SAXON_VERSION_WARNING_FEATURE_KEY, Boolean.FALSE);
            }
            catch (IllegalArgumentException e) {
                /* (Safe to ignore this) */
            }
        }
        
        /* Must have been OK! */
        return transformerFactory;
    }
    
    private static void requireFeature(TransformerFactory transformerFactory, String feature) {
        if (!transformerFactory.getFeature(feature)) {
            throw new SnuggleRuntimeException("TransformerFactory "
                    + transformerFactory.getClass().getName()
                    + " needs to support feature "
                    + feature
                    + " in order to be used with SnuggleTeX");
        }   
    }
    
    /**
     * Creates a (namespace-aware) DOM {@link DocumentBuilder}, throwing a {@link SnuggleRuntimeException}
     * if such a thing cannot be created/configured.
     */
    public static DocumentBuilder createNSAwareDocumentBuilder() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            return documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new SnuggleRuntimeException("Could not create Namespace-aware DocumentBuilder", e);
        }
    }
}