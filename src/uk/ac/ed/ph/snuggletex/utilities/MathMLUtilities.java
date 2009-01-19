/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import static org.w3c.dom.Node.ELEMENT_NODE;

import uk.ac.ed.ph.commons.util.ConstraintUtilities;
import uk.ac.ed.ph.commons.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Some general utility methods for manipulating MathML via the DOM.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathMLUtilities {
    
    public static final String ANNOTATION_LOCAL_NAME = "annotation";
    public static final String ANNOTATION_XML_LOCAL_NAME = "annotation-xml";
    
    /**
     * Convenience method that parses a MathML document specified as a UTF-8-encoded String.
     * 
     * @return DOM Document
     * @param mathmlDocument
     * @throws IOException
     * @throws SAXException
     */
    public static Document parseMathMLDocumentString(String mathmlDocument)
            throws IOException, SAXException {
        return XMLUtilities.createNSAwareDocumentBuilder()
            .parse(new InputSource(new StringReader(mathmlDocument)));
    }
    
    //---------------------------------------------------------------------
    
    /**
     * Convenience method that serializes the given DOM Document as a String (encoded in UTF-8),
     * indenting the results and omitting the XML declaration, which is a reasonable way of
     * serializing a MathML document.
     *
     * @param document DOM document to serialize
     */
    public static String serializeDocument(Document document) {
        return serializeNode(document, true, true);
    }
    
    /**
     * Convenience method that serializes the given DOM Document as a String (encoded in UTF-8).
     * <p>
     * (This can be used in more general cases than MathML, through experienced programmers
     * may want more control over what happens here.)
     *
     * @param document DOM element to serialize
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    public static String serializeDocument(Document document, boolean indent, boolean omitXMLDeclaration) {
        return serializeNode(document, indent, omitXMLDeclaration);
    }
    
    /**
     * Convenience method that serializes the given DOM Element as a String (encoded in UTF-8),
     * indenting the results and omitting the XML declaration, which is a reasonable way of
     * serializing MathML.
     *
     * @param element DOM element to serialize
     */
    public static String serializeElement(Element element) {
        return serializeNode(element, true, true);
    }
    
    /**
     * Convenience method that serializes the given DOM Element as a String (encoded in UTF-8).
     * <p>
     * (This can be used in more general cases than MathML, through experienced programmers
     * may want more control over what happens here.)
     *
     * @param element DOM element to serialize
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    public static String serializeElement(Element element, boolean indent, boolean omitXMLDeclaration) {
        return serializeNode(element, indent, omitXMLDeclaration);
    }
    
    /**
     * Does the actual donkey-work of the methods above.
     *
     * @param node DOM Node to serialize.
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    private static String serializeNode(Node node, boolean indent, boolean omitXMLDeclaration) {
        StringWriter resultWriter = new StringWriter();
        try {
            Transformer serializer = XMLUtilities.createTransformerFactory().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(indent));
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, StringUtilities.toYesNo(omitXMLDeclaration));
            serializer.transform(new DOMSource(node), new StreamResult(resultWriter));
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Could not serialize DOM", e);
        }
        return resultWriter.toString();
    }
    
    //---------------------------------------------------------------------

    /**
     * Convenience method to unwrap at MathML DOM Object containing top-level parallel markup,
     * as defined Section 5.3.1 of the MathML 2.0 specification.
     * <p>
     * If there is no parallel markup detected then null if returned.
     * <p>
     * If the given element is null or is not a MathML "math" element, then an {@link IllegalArgumentException}
     * is thrown.
     */
    public static UnwrappedParallelMathMLDOM unwrapParallelMathMLDOM(Element mathmlElement) {
        ensureMathMLContainer(mathmlElement);
        
        /* Look for semantics child then annotation child with encoding set appropriately */
        Node search = mathmlElement.getFirstChild();
        if (!(search.getNodeType()==ELEMENT_NODE && Globals.MATHML_NAMESPACE.equals(search.getNamespaceURI())
                && "semantics".equals(search.getLocalName()))) {
            /* Didn't get <semantics/> as first and only child so not parallel markup */
            return null;
        }
        
        /* OK, this looks like parallel markup */
        UnwrappedParallelMathMLDOM result = new UnwrappedParallelMathMLDOM();
        Element semantics = (Element) search;
        NodeList childNodes = semantics.getChildNodes();
        
        /* Pull out the first child */
        result.setFirstBranch((Element) childNodes.item(0));
        
        /* Then pull out annotations, which must be the subsequent children */
        Element searchElement;
        for (int i=1, length=childNodes.getLength(); i<length; i++) {
            search = childNodes.item(i);
            if (search.getNodeType()==ELEMENT_NODE && Globals.MATHML_NAMESPACE.equals(search.getNamespaceURI())) {
                searchElement = (Element) search;
                if (ANNOTATION_LOCAL_NAME.equals(search.getLocalName())) {
                    result.getTextAnnotations().put(searchElement.getAttribute("encoding"), extractTextElementValue(searchElement));
                }
                else if (ANNOTATION_XML_LOCAL_NAME.equals(search.getLocalName())) {
                    result.getXmlAnnotaions().put(searchElement.getAttribute("encoding"), searchElement.getChildNodes());
                }
                else {
                    /* (Just silently ignore this) */
                }
            }
        }
        return result;
    }


    /**
     * Extracts the first textual annotation found having the given encoding attribute from
     * the given MathML <tt>math</tt> element, if such an annotation is found.
     * <p>
     * This assumes the following structure:
     * <pre><![CDATA[
     * <math>
     *   <semantics>
     *     ...
     *     <annotation encoding="...">text</annotation>
     *   </semantics>
     * </math>
     * ]]></pre>
     * 
     * @param mathmlElement
     * @return first matching annotation, or null if not present.
     */
    public static String extractAnnotationString(Element mathmlElement, String encodingAttribute) {
        Element annotationElement = extractAnnotationElement(mathmlElement, ANNOTATION_LOCAL_NAME, encodingAttribute);
        return annotationElement!=null ? extractTextElementValue(annotationElement) : null;
    }

    /**
     * Extracts the first XML-based annotation having the given encoding attribute from the given MathML
     * <tt>math</tt> element, if such an annotation is found.
     * <p>
     * This assumes the following structure:
     * <pre><![CDATA[
     * <math>
     *   <semantics>
     *     ...
     *     <annotation-xml encoding="...">XML annotation</annotation>
     *   </semantics>
     * </math>
     * ]]></pre>
     * 
     * @param mathmlElement
     * @return DOM NodeList corresponding to the first matching annotation, or null if no such annotation found.
     */
    public static NodeList extractAnnotationXML(Element mathmlElement, String encodingAttribute) {
        Element annotationElement = extractAnnotationElement(mathmlElement, ANNOTATION_XML_LOCAL_NAME, encodingAttribute);
        return annotationElement!=null ? annotationElement.getChildNodes() : null;
    }

    private static Element extractAnnotationElement(Element mathmlElement, String annotationElementLocalName, String encodingAttribute) {
        ensureMathMLContainer(mathmlElement);
        ConstraintUtilities.ensureNotNull(encodingAttribute, "encoding");
        
        /* (We could have used the XPath API for this but it's almost as easy to traverse the DOM
         * directly here.)
         */
        /* Look for semantics child then annotation child with encoding set appropriately */
        Node search = mathmlElement.getFirstChild();
        if (!(search.getNodeType()==ELEMENT_NODE && Globals.MATHML_NAMESPACE.equals(search.getNamespaceURI())
                && "semantics".equals(search.getLocalName()))) {
            /* Didn't get <semantics/> as first and only child */
            return null;
        }
        Element semantics = (Element) search;
        NodeList childNodes = semantics.getChildNodes();
        for (int i=0, length=childNodes.getLength(); i<length; i++) {
            search = childNodes.item(i);
            if (search.getNodeType()==ELEMENT_NODE && Globals.MATHML_NAMESPACE.equals(search.getNamespaceURI())
                    && annotationElementLocalName.equals(search.getLocalName())
                    && encodingAttribute.equals(((Element) search).getAttribute("encoding"))) {
                return (Element) search;
            }
        }
        return null;
    }
    
    private static void ensureMathMLContainer(Element mathmlElement) {
        ConstraintUtilities.ensureNotNull(mathmlElement, "MathML element");
        if (!(Globals.MATHML_NAMESPACE.equals(mathmlElement.getNamespaceURI()) && "math".equals(mathmlElement.getLocalName()))) {
            throw new IllegalArgumentException("Not a MathML <math/> element");
        }
    }
    
    private static String extractTextElementValue(Element textElement) {
        NodeList childNodes = textElement.getChildNodes();
        String result;
        if (childNodes.getLength()==1) {
            /* Text Nodes have been coalesced */
            result = childNodes.item(0).getNodeValue();
        }
        else {
            /* Need to coalesce manually */
            StringBuilder resultBuilder = new StringBuilder();
            for (int i=0; i<childNodes.getLength(); i++) {
                resultBuilder.append(childNodes.item(i).getNodeValue());
            }
            result = resultBuilder.toString();
        }
        return result;
    }

}
