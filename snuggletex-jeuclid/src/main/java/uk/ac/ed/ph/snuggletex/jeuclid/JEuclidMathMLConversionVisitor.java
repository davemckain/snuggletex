/* $Id$
 *
 * Copyright (c) 2003 - 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.jeuclid;

import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.utilities.SnuggleUtilities;

import java.awt.Dimension;
import java.io.File;

import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.converter.Converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple "visitor" class that traverses (part of a) DOM Document, using JEuclid
 * to replace any MathML <tt>math</tt> islands found by an appropriate image rendition.
 * <p>
 * This can be used independently of SnuggleTeX if required.
 * 
 * @see MathMLImageSavingCallback
 *
 * @author  David McKain
 * @version $Revision$
 */
public class JEuclidMathMLConversionVisitor {
    
    private final MathMLImageSavingCallback imageSavingCallback;
    private final Document document;
    
    /** Count of current <math/> element so as to ensure unique names for replaced files */
    private int mathmlCounter;
    
    public JEuclidMathMLConversionVisitor(final MathMLImageSavingCallback callback, final Document document) {
        this.imageSavingCallback = callback;
        this.document = document;
    }
    
    public void run() {
        run(document.getDocumentElement());
    }
    
    public void run(Element startElement) {
        this.mathmlCounter = 0;
        visitElement(startElement);
    }
    
    protected void visitElement(Element element) {
        if (Globals.MATHML_NAMESPACE.equals(element.getNamespaceURI()) && element.getLocalName().equals("math")) {
            replaceMathMLIsland(element);
        }
        else {
            /* Descend into child Nodes */
            NodeList childNodes = element.getChildNodes();
            Node childNode;
            for (int i=0, size=childNodes.getLength(); i<size; i++) {
                childNode = childNodes.item(i);
                if (childNode.getNodeType()==Node.ELEMENT_NODE) {
                    visitElement((Element) childNode);
                }
            }
        }
    }
    
    protected void replaceMathMLIsland(Element mathMLElement) {
        /* First we use JEuclid to create image rendition of Node and replace this element with
         * an XHTML image link.
         */
        File imageFile = imageSavingCallback.getImageOutputFile(mathmlCounter);
        String contentType = imageSavingCallback.getImageContentType(mathmlCounter);
        MutableLayoutContext layoutContext = imageSavingCallback.getLayoutContext(mathmlCounter);
        Dimension imageDimension = null;
        try {
            imageDimension = Converter.getInstance().convert(mathMLElement, imageFile, contentType, layoutContext);
            if (imageDimension!=null) {
                /* Let callback know that image has been saved successfully */
                imageSavingCallback.imageSavingSucceeded(imageFile, mathmlCounter, contentType);
            }
            else {
                imageDimension = new Dimension(0, 0);
                imageSavingCallback.imageSavingFailed(imageFile, mathmlCounter, contentType, null);
            }
        }
        catch (Exception e) {
            imageDimension = new Dimension(0, 0);
            imageSavingCallback.imageSavingFailed(imageFile, mathmlCounter, contentType, e);
        }

        /* Next we extract the SnuggleTeX annotation within the MathML element, if applicable, which contains the
         * original LaTeX input for this math region. This is used to create an "alt" attribute.
         */
        String snuggleTeXEncoding = SnuggleUtilities.extractSnuggleTeXAnnotation(mathMLElement);
        if (snuggleTeXEncoding!=null) {
            snuggleTeXEncoding = snuggleTeXEncoding
                .replaceAll("%\\s+", "") /* Strip LaTeX comments */
                .replaceAll("\\s+", " "); /* Normalise whitespace */
        }
        
        /* Next we replace the <math/> element with a <span> or <div> to house its image replacement. */
        Node parentNode = mathMLElement.getParentNode();
        boolean isBlock = mathMLElement.getAttribute("display").equals("block");
        Element replacementContainer = document.createElementNS(Globals.XHTML_NAMESPACE, isBlock ? "div" : "span");
        replacementContainer.setAttribute("class", "mathml-math");
        parentNode.replaceChild(replacementContainer, mathMLElement);
        
        /* Replace MathML with XHTML <img/> element */
        Element imgElement = document.createElementNS(Globals.XHTML_NAMESPACE, "img");
        imgElement.setAttribute("src", imageSavingCallback.getImageURL(mathmlCounter));
        imgElement.setAttribute("width", Integer.toString(imageDimension.width));
        imgElement.setAttribute("height", Integer.toString(imageDimension.height));
        if (snuggleTeXEncoding!=null) {
            imgElement.setAttribute("alt", snuggleTeXEncoding);
        }
        replacementContainer.appendChild(imgElement);
        
        /* Increment count of MathML islands for next time */
        mathmlCounter++;
    }
}