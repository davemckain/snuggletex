/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import static org.w3c.dom.Node.ELEMENT_NODE;

import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;

import java.io.IOException;
import java.io.StringReader;

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
    public static Document parseMathMLDocumentString(final String mathmlDocument)
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
    public static String serializeDocument(final Document document) {
        return serializeNode(document, null, true, true);
    }
    
    /**
     * Convenience method that serializes the given DOM Document as a String, using the
     * specified encoding, indenting the results and omitting the XML declaration,
     * which is a reasonable way of serializing a MathML document.
     *
     * @param document DOM document to serialize
     * @param encoding desired encoding, null is interpreted as UTF-8.
     */
    public static String serializeDocument(final Document document, final String encoding) {
        return serializeNode(document, encoding, true, true);
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
    public static String serializeDocument(final Document document, final boolean indent,
            final boolean omitXMLDeclaration) {
        return serializeNode(document, null, indent, omitXMLDeclaration);
    }
    
    /**
     * Convenience method that serializes the given DOM Document as a String, using the
     * specified encoding.
     * <p>
     * (This can be used in more general cases than MathML, through experienced programmers
     * may want more control over what happens here.)
     *
     * @param document DOM element to serialize
     * @param encoding desired encoding, null is interpreted as UTF-8.
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    public static String serializeDocument(final Document document, final String encoding,
            final boolean indent, final boolean omitXMLDeclaration) {
        return serializeNode(document, encoding, indent, omitXMLDeclaration);
    }
    
    /**
     * Convenience method that serializes the given DOM Element as a String (encoded in UTF-8),
     * indenting the results and omitting the XML declaration, which is a reasonable way of
     * serializing MathML.
     *
     * @param element DOM element to serialize
     */
    public static String serializeElement(final Element element) {
        return serializeNode(element, null, true, true);
    }
    
    /**
     * Convenience method that serializes the given DOM Element as a String, using
     * the given encoding, indenting the results and omitting the XML declaration,
     * which is a reasonable way of serializing MathML.
     *
     * @param element DOM element to serialize
     * @param encoding desired encoding, null is interpreted as UTF-8.
     */
    public static String serializeElement(final Element element, final String encoding) {
        return serializeNode(element, encoding, true, true);
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
    public static String serializeElement(final Element element, final boolean indent,
            final boolean omitXMLDeclaration) {
        return serializeNode(element, null, indent, omitXMLDeclaration);
    }
    
    /**
     * Convenience method that serializes the given DOM Element as a String, using
     * the given encoding
     * <p>
     * (This can be used in more general cases than MathML, through experienced programmers
     * may want more control over what happens here.)
     *
     * @param element DOM element to serialize
     * @param encoding desired encoding, null is interpreted as UTF-8.
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    public static String serializeElement(final Element element, final String encoding,
            final boolean indent, final boolean omitXMLDeclaration) {
        return serializeNode(element, encoding, indent, omitXMLDeclaration);
    }
    
    /**
     * Does the actual donkey-work of the methods above.
     *
     * @param node DOM Node to serialize.
     * @param indent whether to indent the results or not
     * @param omitXMLDeclaration whether to omit the XML declaration or not.
     */
    private static String serializeNode(final Node node, final String encoding,
            final boolean indent, final boolean omitXMLDeclaration) {
        return XMLUtilities.serializeNode(node, encoding, indent, omitXMLDeclaration);
    }
    
    //---------------------------------------------------------------------
    
    /**
     * Convenience method to test whether the given DOM {@link Node} node is a MathML element
     * having any localName.
     * 
     * @param node Node to test
     *   
     * @throws IllegalArgumentException if node is null.
     */
    public static boolean isMathMLElement(final Node node) {
        return isMathMLElement(node, null);
    }
    
    /**
     * Convenience method to test whether the given DOM {@link Node} node is a MathML element
     * having the given localName.
     * 
     * @param node Node to test
     * @param localName MathML local name to test for, such as <tt>mi</tt> or <tt>math</tt>,
     *   or null to indicate "any name in the MathML namespace".
     *   
     * @throws IllegalArgumentException if node is null.
     */
    public static boolean isMathMLElement(final Node node, final String localName) {
        ConstraintUtilities.ensureNotNull(node, "Node");
        return node.getNodeType()==ELEMENT_NODE
            && Globals.MATHML_NAMESPACE.equals(node.getNamespaceURI())
            && localName==null || localName.equals(node.getLocalName());
    }
    
    public static void ensureMathMLContainer(final Element mathElement) {
        if (!isMathMLElement(mathElement, "math")) {
            throw new IllegalArgumentException("Not a MathML <math/> element");
        }
    }
    
    /**
     * Checks that the given DOM {@link Document} contains a single MathML <tt>math</tt>
     * Element. If so, the Element is returned. Otherwise, an {@link IllegalArgumentException} is
     * thrown.
     */
    public static Element ensureMathMLDocument(final Document document) {
        Element result = document.getDocumentElement();
        if (result==null) {
            throw new IllegalArgumentException("Document does not have a document element");
        }
        ensureMathMLContainer(result);
        return result;
    }

    /**
     * Convenience method to unwrap at MathML DOM Object containing top-level parallel markup,
     * as defined Section 5.3.1 of the MathML 2.0 specification.
     * <p>
     * If there is no parallel markup detected then null if returned.
     * <p>
     * If the given element is null or is not a MathML "math" element, then an {@link IllegalArgumentException}
     * is thrown.
     */
    public static UnwrappedParallelMathMLDOM unwrapParallelMathMLDOM(final Element mathElement) {
        ensureMathMLContainer(mathElement);
        
        /* Look for semantics child then annotation child with encoding set appropriately */
        Node search = mathElement.getFirstChild();
        if (search==null || !isMathMLElement(search, "semantics")) {
            /* Didn't get <semantics/> as first and only child so not parallel markup */
            return null;
        }
        
        /* OK, this looks like parallel markup */
        UnwrappedParallelMathMLDOM result = new UnwrappedParallelMathMLDOM();
        result.setMathElement(mathElement);
        
        /* Pull out the first child */
        Element semantics = (Element) search;
        NodeList childNodes = semantics.getChildNodes();
        if (childNodes.getLength()==0) {
            return null;
        }
        result.setFirstBranch((Element) childNodes.item(0));
        
        /* Then pull out annotations, which must be the subsequent children */
        Element searchElement;
        for (int i=1, length=childNodes.getLength(); i<length; i++) {
            search = childNodes.item(i);
            if (isMathMLElement(search)) {
                searchElement = (Element) search;
                if (ANNOTATION_LOCAL_NAME.equals(search.getLocalName())) {
                    result.getTextAnnotations().put(searchElement.getAttribute("encoding"), XMLUtilities.extractTextElementValue(searchElement));
                }
                else if (ANNOTATION_XML_LOCAL_NAME.equals(search.getLocalName())) {
                    result.getXmlAnnotations().put(searchElement.getAttribute("encoding"), searchElement.getChildNodes());
                }
                else {
                    /* (Just silently ignore this) */
                }
            }
        }
        return result;
    }
    
    public static Element extractFirstSemanticsBranch(final Element mathElement) {
        ensureMathMLContainer(mathElement);
        
        /* Look for semantics child then annotation child with encoding set appropriately */
        Node search = mathElement.getFirstChild();
        if (search==null || !isMathMLElement(search, "semantics")) {
            /* Didn't get <semantics/> as first and only child so not parallel markup */
            return null;
        }
        Element semantics = (Element) search;
        NodeList childNodes = semantics.getChildNodes();
        return childNodes.getLength()==0 ? null : ((Element) childNodes.item(0));
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
     * @param mathElement
     * @return first matching annotation, or null if not present.
     */
    public static String extractAnnotationString(final Element mathElement, final String encodingAttribute) {
        Element annotationElement = extractAnnotationElement(mathElement, ANNOTATION_LOCAL_NAME, encodingAttribute);
        return annotationElement!=null ? XMLUtilities.extractTextElementValue(annotationElement) : null;
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
     * @param mathElement
     * @return DOM NodeList corresponding to the child Nodes of the first matching
     *   <tt>annotation-xml</tt> element, or null if no such annotation found.
     */
    public static NodeList extractAnnotationXML(final Element mathElement, final String encodingAttribute) {
        Element annotationElement = extractAnnotationElement(mathElement, ANNOTATION_XML_LOCAL_NAME, encodingAttribute);
        return annotationElement!=null ? annotationElement.getChildNodes() : null;
    }

    private static Element extractAnnotationElement(final Element mathmlElement, final String annotationElementLocalName, String encodingAttribute) {
        ensureMathMLContainer(mathmlElement);
        ConstraintUtilities.ensureNotNull(encodingAttribute, "encoding");
        
        /* (We could have used the XPath API for this but it's almost as easy to traverse the DOM
         * directly here.)
         */
        /* Look for semantics child then annotation child with encoding set appropriately */
        Node search = mathmlElement.getFirstChild();
        if (search==null || !isMathMLElement(search, "semantics")) {
            /* Didn't get <semantics/> as first and only child */
            return null;
        }
        Element semantics = (Element) search;
        NodeList childNodes = semantics.getChildNodes();
        for (int i=0, length=childNodes.getLength(); i<length; i++) {
            search = childNodes.item(i);
            if (isMathMLElement(search, annotationElementLocalName)
                    && encodingAttribute.equals(((Element) search).getAttribute("encoding"))) {
                return (Element) search;
            }
        }
        return null;
    }

    
    /**
     * "Isolates" the first <semantics/> branch of an annotation MathML element
     * by producing a copy of the MathML element with a single child containing
     * only the first child of the <semantics/> element.
     * <p>
     * For example:
     * <pre><![CDATA[
     *   <math>
     *     <semantics>
     *       <mi>x</mi>
     *       <annotation-xml encoding='blah'><x/></annotation-xml>
     *     </semantics>
     *   </math>
     * ]]>
     * results in:
     * <pre><![CDATA[
     *   <math>
     *     <mi>x</mi>
     *   </math>
     * ]]>
     * 
     * @return new MathML Document with the given structure or null if the given <tt>math</tt>
     *   element is not annotated.
     * 
     * @throws IllegalArgumentException if passed null or the given element is not a
     *   <tt>math</tt> element.
     */
    public static Document isolateFirstSemanticsBranch(final Element mathElement) {
        Element firstSemantics = extractFirstSemanticsBranch(mathElement);
        return firstSemantics!=null ? isolateDescendant(mathElement, firstSemantics) : null;
    }
    
    /**
     * Version of {@link #isolateFirstSemanticsBranch(Element)} that works on an
     * {@link UnwrappedParallelMathMLDOM}.
     * 
     * @return new MathML Document with the given structure or null if the given wrapper has
     *   no first branch.
     *   
     * @throws IllegalArgumentException if passed null.
     */
    public static Document isolateFirstSemanticsBranch(final UnwrappedParallelMathMLDOM unwrappedDOM) {
        ConstraintUtilities.ensureNotNull(unwrappedDOM, "UnwrappedParallelMathMLDOM");
        Element firstSemantics = unwrappedDOM.getFirstBranch();
        return firstSemantics!=null ? isolateDescendant(unwrappedDOM.getMathElement(), firstSemantics) : null;
    }
    
    /**
     * "Isolates" the XML annotation having the given encoding by producing a copy of the MathML
     * element with only the given annotation contents as children.
     * <p>
     * For example:
     * <pre><![CDATA[
     *   <math>
     *     <semantics>
     *       <mi>x</mi>
     *       <annotation-xml encoding='blah'><x/></annotation-xml>
     *     </semantics>
     *   </math>
     * ]]>
     * results in:
     * <pre><![CDATA[
     *   <math>
     *     <x/>
     *   </math>
     * ]]>
     * 
     * @return new MathML Document with the given structure or null if the given <tt>math</tt>
     *   element is not annotated or does not have the required annotation.
     * 
     * @throws IllegalArgumentException if passed null or the given element is not a
     *   <tt>math</tt> element.
     */
    public static Document isolateAnnotationXML(final Element mathElement, final String encodingAttribute) {
        NodeList annotationContents = extractAnnotationXML(mathElement, encodingAttribute);
        return annotationContents!=null ? isolateDescendant(mathElement, annotationContents) : null;
    }
    
    /**
     * Version of {@link #isolateAnnotationXML(Element, String)} that works on an
     * {@link UnwrappedParallelMathMLDOM}.
     * 
     * @return new MathML Document with the given structure or null if the given wrapper does
     *   not have the required annotation.
     *   
     * @throws IllegalArgumentException if passed null.
     */
    public static Document isolateAnnotationXML(final UnwrappedParallelMathMLDOM unwrappedDOM, final String encodingAttribute) {
        ConstraintUtilities.ensureNotNull(unwrappedDOM, "UnwrappedParallelMathMLDOM");
        NodeList annotationContents = unwrappedDOM.getXmlAnnotations().get(encodingAttribute);
        return annotationContents!=null ? isolateDescendant(unwrappedDOM.getMathElement(), annotationContents) : null;
    }
    
    private static Document isolateDescendant(final Element mathElement, final NodeList descendants) {
        Document result = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element resultMathElement = (Element) mathElement.cloneNode(false);
        result.adoptNode(resultMathElement);
        result.appendChild(resultMathElement);
        for (int i=0, size=descendants.getLength(); i<size; i++) {
            Node annotationNode = descendants.item(i);
            Node annotationNodeCopy = annotationNode.cloneNode(true);
            result.adoptNode(annotationNodeCopy);
            resultMathElement.appendChild(annotationNodeCopy);
        }
        return result;
    }
    
    private static Document isolateDescendant(final Element mathElement, final Element descendant) {
        Document result = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element resultMathElement = (Element) mathElement.cloneNode(false);
        result.adoptNode(resultMathElement);
        result.appendChild(resultMathElement);
        
        Element firstSemanticElementCopy = (Element) descendant.cloneNode(true);
        result.adoptNode(firstSemanticElementCopy);
        resultMathElement.appendChild(firstSemanticElementCopy);
        return result;
    }
}
