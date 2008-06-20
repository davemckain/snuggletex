/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static uk.ac.ed.ph.snuggletex.definitions.Globals.MATHML_NAMESPACE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO: This will eventually contain a set of static methods for doing the most common types of jobs.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleTeX {
    
    /** Namespace for any SnuggleTeX-specific XML elements produced */
    public static final String SNUGGLETEX_NAMESPACE = "http://www.ph.ed.ac.uk/snuggletex";
    
    /** 
     * Value of the "encoding" attribute added to MathML element annotation elements, used
     * when requested.
     */
    public static final String SNUGGLETEX_MATHML_ANNOTATION_ENCODING = "SnuggleTeX";
    
    public static List<InputError> snuggle(final Element targetRoot, final SnuggleInput... inputs)
            throws IOException {
        SnuggleTeXSession session = new SnuggleTeXEngine().createSession();
        for (SnuggleInput input : inputs) {
            session.parseInput(input);
        }
        session.buildDOMSubtree(targetRoot);
        return session.getErrors();
    }
    
    public static List<InputError> writeWebPage(final WebPageBuilderOptions options,
            final OutputStream outputStream, final SnuggleInput... inputs)
            throws IOException {
        SnuggleTeXSession session = new SnuggleTeXEngine().createSession();
        for (SnuggleInput input : inputs) {
            session.parseInput(input);
        }
        session.writeWebPage(options, outputStream);
        return session.getErrors();
    }
    
    public static List<InputError> writeWebPage(final WebPageBuilderOptions options,
            final File outputFile, final SnuggleInput... inputs)
            throws IOException {
        return writeWebPage(options, new FileOutputStream(outputFile), inputs);
    }

    /**
     * Extracts a SnuggleTeX encoding from a MathML <tt>math</tt> element, if found. This allows
     * you to extract the input LaTeX from a MathML element created by SnuggleTeX, provided that
     * {@link DOMBuilderOptions#isAddingMathAnnotations()} returned true when the element was
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
    					&& "annotation".equals(search.getLocalName()) && SNUGGLETEX_MATHML_ANNOTATION_ENCODING.equals(((Element) search).getAttribute("encoding"))) {
    				return search.getFirstChild().getNodeValue();
    			}
    		}
    	}
    	return null;
    }
}
