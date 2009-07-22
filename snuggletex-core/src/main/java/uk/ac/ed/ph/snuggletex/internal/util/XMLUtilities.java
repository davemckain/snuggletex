/* $Id$
 *
 * Copyright (c) 2003 - 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal.util;

import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.utilities.ClassPathURIResolver;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Some trivial little helpers for creating suitably-configured {@link TransformerFactory}
 * instances.
 * <p>
 * (This is based on similar utility methods in <tt>ph-commons</tt>.)
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class XMLUtilities {
    
    /** Explicit name of the SAXON 9.X TransformerFactoryImpl Class, as used by the up-conversion extensions */
    public static final String SAXON_TRANSFORMER_FACTORY_CLASS_NAME = "net.sf.saxon.TransformerFactoryImpl";

    /**
     * Creates an instance of the currently specified JAXP {@link TransformerFactory}, ensuring
     * that the result supports the {@link DOMSource#FEATURE} and {@link DOMResult#FEATURE}
     * features.
     */
    public static TransformerFactory createJAXPTransformerFactory() {
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
    
    private static void requireFeature(final TransformerFactory transformerFactory, final String feature) {
        if (!transformerFactory.getFeature(feature)) {
            throw new SnuggleRuntimeException("TransformerFactory "
                    + transformerFactory.getClass().getName()
                    + " needs to support feature "
                    + feature
                    + " in order to be used with SnuggleTeX");
        }   
    }

    /**
     * Explicitly creates a Saxon 9 {@link TransformerFactory}, as used by the up-conversion
     * extensions.
     */
    public static TransformerFactory createSaxonTransformerFactory() {
        try {
            /* We call up SAXON explicitly without going through the usual factory path */
            return (TransformerFactory) Class.forName(SAXON_TRANSFORMER_FACTORY_CLASS_NAME).newInstance();
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Failed to explicitly instantiate SAXON "
                    + SAXON_TRANSFORMER_FACTORY_CLASS_NAME
                    + " class - check your ClassPath!", e);
        }
    }
    
    /**
     * Compiles an "internal" stylesheet located via the ClassPath at the given location.
     * <p>
     * This takes an explicit {@link TransformerFactory} as the first argument as some extensions
     * require XSLT 2.0 so will have created an explicit instance of SAXON's {@link TransformerFactory}
     * to pass to this.
     * <p>
     * <strong>NOTE:</strong> a {@link ClassPathURIResolver} will be set on the {@link TransformerFactory}
     * passed here, which is a bit leaky. 
     *
     * @param transformerFactory
     * @param classPathUri absolute URI specifying the location of the stylesheet in the ClassPath,
     *   specified via the scheme mentioned in {@link ClassPathURIResolver}.
     */
    public static Templates compileInternalStylesheet(final TransformerFactory transformerFactory,
            String classPathUri) {
        ClassPathURIResolver uriResolver = ClassPathURIResolver.getInstance();
        transformerFactory.setURIResolver(uriResolver);
        Source resolved;
        try {
            resolved = uriResolver.resolve(classPathUri, "");
            if (resolved==null) {
                throw new SnuggleRuntimeException("Not a ClassPath URI: " + classPathUri);
            }
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
     * Tests whether the given {@link TransformerFactory} is known to support XSLT 2.0.
     * <p>
     * Currently, this involves checking for a suitable version of SAXON; this will
     * change once more processors become available.
     */
    public static boolean supportsXSLT20(final TransformerFactory tranformerFactory) {
        return tranformerFactory.getClass().getName().startsWith("net.sf.saxon.");
    }
    
    /**
     * Tests whether the given {@link Templates} is known to support XSLT 2.0.
     * <p>
     * Currently, this involves checking for a suitable version of SAXON; this will
     * change once more processors become available.
     */
    public static boolean supportsXSLT20(final Templates templates) {
        return templates.getClass().getName().startsWith("net.sf.saxon.");
    }
    
    /**
     * Tests whether the given {@link Transformer} is known to support XSLT 2.0.
     * <p>
     * Currently, this involves checking for a suitable version of SAXON; this will
     * change once more processors become available.
     */
    public static boolean supportsXSLT20(final Transformer tranformer) {
        return tranformer.getClass().getName().startsWith("net.sf.saxon.");
    }
    
    //------------------------------------------------------------------
    
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

    /**
     * Trivial convenience method to extract the value of a (text) Element, coping with the
     * possibility that text child Nodes may not have been coalesced.
     * 
     * @param textElement
     */
    public static String extractTextElementValue(final Element textElement) {
        NodeList childNodes = textElement.getChildNodes();
        String result;
        if (childNodes.getLength()==1) {
            /* Text Nodes have been coalesced */
            result = ensureExtractTextNodeValue(childNodes.item(0));
        }
        else {
            /* Need to coalesce manually */
            StringBuilder resultBuilder = new StringBuilder();
            for (int i=0; i<childNodes.getLength(); i++) {
                resultBuilder.append(ensureExtractTextNodeValue(childNodes.item(i)));
            }
            result = resultBuilder.toString();
        }
        return result;
    }
    
    private static String ensureExtractTextNodeValue(final Node node) {
        if (node.getNodeType()==Node.TEXT_NODE) {
            return node.getNodeValue();
        }
        throw new IllegalArgumentException("Node is not a text Node");
    }
    
    //------------------------------------------------------------------
    
    public static Transformer createSerializer(StylesheetManager stylesheetManager,
            final String serializerUri, final boolean useCharacterMap) {
        Transformer serializer;
        TransformerFactory transformerFactory = createJAXPTransformerFactory();
        transformerFactory.setURIResolver(ClassPathURIResolver.getInstance());
        boolean mapCharacters = useCharacterMap && supportsXSLT20(transformerFactory);
        try {
            if (mapCharacters && serializerUri!=null) {
                Templates serializerTemplates = cacheImporterStylesheet(transformerFactory,
                        stylesheetManager.getStylesheetCache(),
                        serializerUri, Globals.CHARACTER_MAPS_XSL_RESOURCE_NAME);
                serializer = serializerTemplates.newTransformer();
                serializer.setOutputProperty("use-character-maps", "output");
            }
            else if (serializerUri!=null) {
                serializer = stylesheetManager.getStylesheet(serializerUri, transformerFactory)
                    .newTransformer();
            }
            else if (mapCharacters) {
                serializer = stylesheetManager.getStylesheet(Globals.SERIALIZE_WITH_CHARACTER_MAPS_XSL_RESOURCE_NAME, transformerFactory)
                    .newTransformer();
            }
            else {
                serializer = transformerFactory.newTransformer();
            }
        }
        catch (TransformerConfigurationException e) {
            throw new SnuggleRuntimeException("Could not create serializer", e);
        }
        return serializer;
    }
    
    private static Templates cacheImporterStylesheet(final TransformerFactory transformerFactory,
            StylesheetCache stylesheetCache, final String... importUris) {
        Templates result;
        if (stylesheetCache==null) {
            result = compileImporterStylesheet(transformerFactory, importUris);
        }
        else {
            String cacheKey = "snuggletex-serializer(" + StringUtilities.join(importUris, ",") + ")";
            synchronized(stylesheetCache) {
                result = stylesheetCache.getStylesheet(cacheKey);
                if (result==null) {
                    result = compileImporterStylesheet(transformerFactory, importUris);
                    stylesheetCache.putStylesheet(cacheKey, result);
                }
            }
        }
        return result;
    }
    
    /**
     * Helper to create "driver" XSLT stylesheets that import the stylesheets at the given URIs,
     * using the given {@link TransformerFactory}.
     * 
     * @param transformerFactory
     * @param importUris
     */
    private static Templates compileImporterStylesheet(final TransformerFactory transformerFactory,
            final String... importUris) {
        StringBuilder xsltBuilder = new StringBuilder("<stylesheet version='1.0' xmlns='http://www.w3.org/1999/XSL/Transform'>\n");
        for (String importUri : importUris) {
            xsltBuilder.append("<import href='").append(importUri).append("'/>\n");
        }
        xsltBuilder.append("</stylesheet>");
        String xslt = xsltBuilder.toString();
        try {
            return transformerFactory.newTemplates(new StreamSource(new StringReader(xslt)));
        }
        catch (TransformerConfigurationException e) {
            throw new SnuggleRuntimeException("Could not compile stylesheet driver " + xslt, e);
        }
    }
    
    //------------------------------------------------------------------
    
    /**
     * Serializes the given {@link Node} to a well-formed external parsed entity.
     * (If the given Node is an {@link Element} or a {@link Document} then the result
     * will be a well-formed XML String.)
     *
     * @param node DOM Node to serialize.
     * @param encoding desired encoding, if null then UTF-8 is used.
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    public static String serializeNode(final Node node, final String encoding, final boolean indent,
            final boolean omitXMLDeclaration) {
        StringWriter resultWriter = new StringWriter();
        try {
            Transformer serializer = createJAXPTransformerFactory().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(indent));
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, StringUtilities.toYesNo(omitXMLDeclaration));
            serializer.setOutputProperty(OutputKeys.ENCODING, encoding!=null ? encoding : "UTF-8");
            serializer.transform(new DOMSource(node), new StreamResult(resultWriter));
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Could not serialize DOM", e);
        }
        return resultWriter.toString();
    }
    
    /**
     * Serializes the <tt>children</tt> of given {@link Node} to a well-formed external parsed entity.
     * <p>
     * (This uses a little XSLT stylesheet to help, hence the requirement for a {@link StylesheetManager}).
     *
     * @param node DOM Node to serialize.
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     * @param applyCharacterMap whether to map certain Unicode characters to entities in 
     *   the output (requires an XSLT 2.0 processor, ignored if not supported).
     * @param stylesheetManager used to help compile and cache stylesheets used in this process.
     */
    public static String serializeNodeChildren(final Node node, final String encoding, final boolean indent,
            final boolean omitXMLDeclaration, final boolean applyCharacterMap,
            StylesheetManager stylesheetManager) {
        StringWriter resultWriter = new StringWriter();
        
        /* This process consists of an XSLT 1.0 transform to exrtact the child Nodes, plus
         * a further optional XSLT 2.0 transform to map character references to named entities.
         * We'll implement this as a mini DOM pipeline.
         */
        try {
            Transformer serializer = createSerializer(stylesheetManager, Globals.EXTRACT_CHILD_NODES_XSL_RESOURCE_NAME, applyCharacterMap);
            serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(indent));
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, StringUtilities.toYesNo(omitXMLDeclaration));
            serializer.setOutputProperty(OutputKeys.ENCODING, encoding!=null ? encoding : "UTF-8");
            serializer.transform(new DOMSource(node), new StreamResult(resultWriter));
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Could not serialize DOM", e);
        }
        return resultWriter.toString();
    }
    
    //------------------------------------------------------------------
    
    public static boolean isXMLName(String string) {
        return string!=null && string.matches("[a-zA-Z_:][a-zA-Z0-9_:.-]*");
    }
    
    public static boolean isXMLNCName(String string) {
        return string!=null && string.matches("[a-zA-Z_][a-zA-Z0-9_.-]*");
    }
}