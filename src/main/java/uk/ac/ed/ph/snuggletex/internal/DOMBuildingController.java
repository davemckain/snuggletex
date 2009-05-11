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
        DOMPostProcessor domPostProcessor = options.getDOMPostProcessor();
        if (domPostProcessor!=null) {
            /* We'll build into a "work" Document first, then apply the post-processor and then
             * finally adopt all of the resulting Nodes as children of the targetRoot
             */
            Document workDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
            Element workRoot = workDocument.createElementNS(SnuggleConstants.SNUGGLETEX_NAMESPACE, "root");
            workDocument.appendChild(workRoot);
            
            /* Do raw DOM Building */
            DOMBuilder domBuilder = new DOMBuilder(sessionContext, workRoot, options);
            domBuilder.buildDOMSubtree(fixedTokens);
            
            /* Now let post-processor do its thing */
            Document finalDocument = domPostProcessor.postProcessDOM(workDocument, options, sessionContext.getStylesheetManager());
            
            /* Pull the children of the root element in the resulting Document into the targetRoot */
            Element resultRoot = finalDocument.getDocumentElement();
            NodeList childNodes = resultRoot.getChildNodes();
            Node childNode;
            for (int i=0, size=childNodes.getLength(); i<size; i++) {
                childNode = childNodes.item(0); /* (Adopting each child moves subsequent ones back!) */
                targetRoot.appendChild(targetRoot.getOwnerDocument().adoptNode(childNode));
            }
        }
        else {
            /* Just build as normal */
            DOMBuilder domBuilder = new DOMBuilder(sessionContext, targetRoot, options);
            domBuilder.buildDOMSubtree(fixedTokens);
        }
    }
}
