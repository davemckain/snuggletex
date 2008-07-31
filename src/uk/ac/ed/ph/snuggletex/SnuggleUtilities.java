/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.MATHML_NAMESPACE;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A collection of occasionally useful SnuggleTeX-related utility methods.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class SnuggleUtilities {

    /**
     * Extracts a SnuggleTeX encoding from a MathML <tt>math</tt> element, if found. This allows
     * you to extract the input LaTeX from a MathML element created by SnuggleTeX, provided that
     * {@link DOMOutputOptions#isAddingMathAnnotations()} returned true when the element was
     * generated.
     * 
     * @param mathmlElement
     * @return SnuggleTeX encoding, or null if not present.
     */
    public static String extractSnuggleTeXAnnotation(Element mathmlElement) {
        if (MATHML_NAMESPACE.equals(mathmlElement.getNamespaceURI()) && "math".equals(mathmlElement.getLocalName())) {
            /* Look for semantics child then annotation child with encoding set appropriately */
            Node search = mathmlElement.getFirstChild();
            if (!(search.getNodeType()==ELEMENT_NODE && MATHML_NAMESPACE.equals(search.getNamespaceURI())
                    && "semantics".equals(search.getLocalName()))) {
                /* Didn't get <semantics/> as first and only child */
                return null;
            }
            Element semantics = (Element) search;
            NodeList childNodes = semantics.getChildNodes();
            for (int i=0, length=childNodes.getLength(); i<length; i++) {
                search = childNodes.item(i);
                if (search.getNodeType()==ELEMENT_NODE && MATHML_NAMESPACE.equals(search.getNamespaceURI())
                        && "annotation".equals(search.getLocalName())
                        && SnuggleConstants.SNUGGLETEX_MATHML_ANNOTATION_ENCODING.equals(((Element) search).getAttribute("encoding"))) {
                    return search.getFirstChild().getNodeValue();
                }
            }
        }
        return null;
    }
}
