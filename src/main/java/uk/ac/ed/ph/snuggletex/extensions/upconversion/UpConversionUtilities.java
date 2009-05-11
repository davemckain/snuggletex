/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.extensions.upconversion;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.SnuggleConstants;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.internal.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Some static utility methods for the up-conversion process, including methods for extracting
 * errors.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConversionUtilities {
    
    public static List<UpConversionFailure> extractUpConversionFailures(Document upConvertedDocument) {
        return extractUpConversionFailures(upConvertedDocument.getDocumentElement());
    }
    
    public static List<UpConversionFailure> extractUpConversionFailures(Element upConvertedElement) {
        List<UpConversionFailure> result = new ArrayList<UpConversionFailure>();
        walkDOM(upConvertedElement, result);
        return result;
    }
    
    /**
     * Returns a full error message for the given {@link UpConversionFailure}, using
     * the SnuggleTeX {@link MessageFormatter} class to do the hard work.
     * 
     * @param failure
     */
    public static String getErrorMessage(UpConversionFailure failure) {
        return MessageFormatter.getErrorMessage(failure.getErrorCode().toString(), failure.getArguments());
    }

    /**
     * Checks to see whether the given element is of the form:
     * 
     * <![CDATA[
     * <s:fail code="...">
     *   <s:arg>...</s:arg>
     *   ...
     *   <s:context>...</s:context>
     * </s:fail>
     * ]]>
     * 
     * If so, creates an {@link UpConversionFailure} for this and adds to the result. If not,
     * traverses downwards recursively.
     * 
     * @param element
     * @param resultBuilder
     */
    private static void walkDOM(Element element, List<UpConversionFailure> resultBuilder) {
        NodeList childNodes = element.getChildNodes();
        Node child;
        if (element.getNamespaceURI().equals(SnuggleConstants.SNUGGLETEX_NAMESPACE) && element.getLocalName().equals("fail")) {
            /* Yay! Found one of our <s:fail/> elements. First extract ErrorCode */
            String codeAttribute = element.getAttribute("code");
            ErrorCode errorCode;
            try {
                errorCode = ErrorCode.valueOf(codeAttribute);
            }
            catch (IllegalArgumentException e) {
                throw new SnuggleLogicException("Error code '" + codeAttribute + "' not defined");
            }
            /* Now get arguments and context */
            List<String> arguments = new ArrayList<String>();
            String context = null;
            for (int i=0, size=childNodes.getLength(); i<size; i++) {
                child = childNodes.item(i);
                if (child.getNodeType()==Node.ELEMENT_NODE) {
                    if (!child.getNamespaceURI().equals(SnuggleConstants.SNUGGLETEX_NAMESPACE)) {
                        throw new SnuggleLogicException("Didn't expect child of <s:fail/> in namespace " + child.getNamespaceURI());
                    }
                    if (child.getLocalName().equals("arg")) {
                        arguments.add(XMLUtilities.extractTextElementValue((Element) child));
                    }
                    else if (child.getLocalName().equals("context")) {
                        if (context!=null) {
                            throw new SnuggleLogicException("Did not expect more than 1 <s:context/> element inside <s:fail/>");
                        }
                        context = MathMLUtilities.serializeElement((Element) child);
                    }
                    else {
                        throw new SnuggleLogicException("Didn't expect child of <s:fail/> with local name " + child.getLocalName());
                    }
                }
            }
            if (context==null) {
                throw new SnuggleLogicException("No <s:context/> element found inside <s:fail/>");
            }
            resultBuilder.add(new UpConversionFailure(errorCode, context, arguments.toArray(new String[arguments.size()])));
        }
        else {
            /* Descend into child elements */
            for (int i=0, size=childNodes.getLength(); i<size; i++) {
                child = childNodes.item(i);
                if (child.getNodeType()==Node.ELEMENT_NODE) {
                    walkDOM((Element) child, resultBuilder);
                }
            }
        }
    }
}
