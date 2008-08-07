/* $Id$
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
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
 * @author  David McKain
 * @version $Revision$
 */
public final class XMLUtilities {

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
     * Tests whether the given {@link Transformer} is known to support XSLT 2.0.
     * <p>
     * Currently, this involves checking for a suitable version of SAXON; this will
     * change once more processors become available.
     */
    public static boolean supportsXSLT20(Transformer tranformer) {
        return tranformer.getClass().getName().startsWith("net.sf.saxon.");
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