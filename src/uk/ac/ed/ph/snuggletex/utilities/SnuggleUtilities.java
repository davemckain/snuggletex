/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.MATHML_NAMESPACE;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.SnuggleConstants;

import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A collection of occasionally useful SnuggleTeX-related utility methods, as well
 * as some more general LaTeX stuff.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleUtilities {
    
    private static final Pattern charsToVerbPattern = Pattern.compile("([\\\\^]+)");
    private static final Pattern charsToBackslashPattern = Pattern.compile("([%#_$&\\{\\}])");

    /**
     * Quotes the given (ASCII) String so that it could be safely input in TEXT Mode in
     * LaTeX input.
     * 
     * @param text text to quote, assumed to be ASCII.
     * @return quoted text suitable for being input into LaTeX.
     */
    public static String quoteTextForInput(String text) {
        String result = charsToVerbPattern.matcher(text).replaceAll("\\\\verb|$1|");
        result = charsToBackslashPattern.matcher(result).replaceAll("\\\\$1");
        return result;
    }

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
