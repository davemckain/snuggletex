/* $Id$
 *
 * Copyright (c) 2003 - 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.utilities.ClassPathURIResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
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

    /**
     * Creates an instance of the currently specified JAXP {@link TransformerFactory}, ensuring
     * that the result supports the {@link DOMSource#FEATURE} and {@link DOMResult#FEATURE}
     * features.
     */
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
     * Compiles an "internal" stylesheet located via the ClassPath at the given location.
     * <p>
     * (This takes an explicit {@link TransformerFactory} as the first argument as some extensions
     * require XSLT 2.0 so will have created an explicit instance of SAXON's {@link TransformerFactory}
     * to pass to this.)
     *
     * @param transformerFactory
     * @param classPathUri absolute URI specifying the location of the stylesheet in the ClassPath,
     *   specified via the scheme mentioned in {@link ClassPathURIResolver}.
     */
    public static Templates compileInternalStylesheet(TransformerFactory transformerFactory,
            String classPathUri) {
        ClassPathURIResolver uriResolver = ClassPathURIResolver.getInstance();
        transformerFactory.setURIResolver(uriResolver);
        Source resolved;
        try {
            resolved = uriResolver.resolve(classPathUri, "");
            return transformerFactory.newTemplates(resolved);
        }
        catch (TransformerConfigurationException e) {
            throw new SnuggleRuntimeException("Could not compile internal stylesheet at " + classPathUri, e);
        }
        catch (TransformerException e) {
            throw new SnuggleRuntimeException("Could not resolve internal stylesheet location " + classPathUri, e);
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