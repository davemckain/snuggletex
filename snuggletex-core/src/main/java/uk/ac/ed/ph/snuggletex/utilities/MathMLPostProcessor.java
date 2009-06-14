/* $Id$
 *
 * Copyright (c) 2003 - 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.utilities;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Convenient base for {@link DOMPostProcessor}s that might want to do interesting
 * things to MathML islands.
 *
 * @author  David McKain
 * @version $Revision$
 */
public abstract class MathMLPostProcessor implements DOMPostProcessor {
    
    public final Document postProcessDOM(Document workDocument, final DOMOutputOptions options,
            StylesheetManager stylesheetManager) {
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        new NodeVisitor(workDocument, resultDocument).run();
        return resultDocument;
    }
    
    protected abstract void handleMathMLIsland(final Element inputMathIsland,
            Document outputDocument, Node outputParentNode, int mathmlCounter);
    
    private class NodeVisitor {
        
        private int mathmlCounter;
        private final Document inputDocument;
        private final Document outputDocument;
        private Node inputNode;
        private Node outputParentNode;
        
        public NodeVisitor(final Document inputDocument, final Document outputDocument) {
            this.inputDocument = inputDocument;
            this.outputDocument = outputDocument;
        }
        
        public void run() {
            outputParentNode = outputDocument;
            inputNode = inputDocument.getFirstChild();
            mathmlCounter = 0;
            while (inputNode!=null) {
                if (MathMLUtilities.isMathMLElement(inputNode, "math")) {
                    /* Let subclass decide what to do */
                    handleMathMLIsland((Element) inputNode, outputDocument, outputParentNode,
                            mathmlCounter++);
                }
                else {
                    /* Clone Node and add to outputDocument */
                    Node outputNode = outputDocument.adoptNode(inputNode.cloneNode(false));
                    outputParentNode.appendChild(outputNode);
                    if (inputNode.hasChildNodes()) {
                        /* Descend */
                        inputNode = inputNode.getFirstChild();
                        outputParentNode = outputNode;
                        continue;
                    }
                }
                /* Go to next sibling if available. Otherwise, go up and onto next sibling. Keep
                 * going in same way if required until we end up back at the top of the document */
                Node nextSibling = inputNode.getNextSibling();
                while (nextSibling==null) {
                    /* Up one level (if possible) then next sibling */
                    inputNode = inputNode.getParentNode();
                    if (inputNode==null) {
                        break;
                    }
                    nextSibling = inputNode.getNextSibling();
                    outputParentNode = outputParentNode.getParentNode();
                }
                inputNode = nextSibling;
            }
        }
    }
}