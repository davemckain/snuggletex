/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.DOMPostProcessor;
import uk.ac.ed.ph.snuggletex.SnuggleConstants;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the main entry point into the DOM generation process. This uses a {@link DOMBuilder} to
 * build the raw DOM and then does any further work on the resulting DOM as specified by the
 * {@link DOMOutputOptions}.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class DOMBuildingController {
    
    private final SessionContext sessionContext;
    private final DOMOutputOptions options;
    
    public DOMBuildingController(final SessionContext sessionContext, final DOMOutputOptions options) {
        this.sessionContext = sessionContext;
        this.options = options;
    }
    
    public void buildDOMSubtree(final Element targetRoot, final List<FlowToken> fixedTokens)
            throws SnuggleParseException {
        DOMPostProcessor[] domPostProcessors = options.getDOMPostProcessors();
        if (domPostProcessors!=null && domPostProcessors.length > 0) {
            /* We run through each Post-Processor in turn, building a temporary "outputDocument"
             * and then using it as an input for the next stage. The final outputDocument is
             * used to decide what gets added into the target Document.
             */
            Document workDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
            Element workRoot = workDocument.createElementNS(SnuggleConstants.SNUGGLETEX_NAMESPACE, "root");
            workDocument.appendChild(workRoot);
            for (int processorIndex=0; processorIndex<domPostProcessors.length; processorIndex++) {
                DOMPostProcessor domPostProcessor = domPostProcessors[processorIndex];
                
                /* Do raw DOM Building */
                DOMBuilder domBuilder = new DOMBuilder(sessionContext, workRoot, options);
                domBuilder.buildDOMSubtree(fixedTokens);
                
                /* Now let post-processor do its thing */
                Document outputDocument = domPostProcessor.postProcessDOM(workDocument, options, sessionContext.getStylesheetManager());
                Element adoptRoot;
                
                if (processorIndex==domPostProcessors.length - 1) {
                    /* This is the final step, so add to targetRoot */
                    adoptRoot = targetRoot;
                }
                else {
                    /* Still go more steps to do, so we will be reusing workRoot. */
                    adoptRoot = workRoot;
                    /* But need to remove existing child Nodes first */
                    NodeList toDelete = workRoot.getChildNodes();
                    for (int i=toDelete.getLength()-1; i>=0; i--) {
                        workRoot.removeChild(toDelete.item(i));
                    }
                }
                /* Adopt nodes from outputDocument to adoptRoot */
                Element resultRoot = outputDocument.getDocumentElement();
                NodeList childNodes = resultRoot.getChildNodes();
                Node childNode;
                for (int nodeIndex=0, size=childNodes.getLength(); nodeIndex<size; nodeIndex++) {
                    childNode = childNodes.item(0); /* (Adopting each child moves subsequent ones back!) */
                    adoptRoot.appendChild(adoptRoot.getOwnerDocument().adoptNode(childNode));
                }
            }
        }
        else {
            /* Just build as normal */
            DOMBuilder domBuilder = new DOMBuilder(sessionContext, targetRoot, options);
            domBuilder.buildDOMSubtree(fixedTokens);
        }
    }
}
