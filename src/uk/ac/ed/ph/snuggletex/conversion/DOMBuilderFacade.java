/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.DOMBuilderOptions;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the main entry point into the DOM Building process, down-converting the resulting
 * DOM is specified by {@link DOMBuilderOptions}.
 * 
 * FIXME: Yucky name for this!!!
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public class DOMBuilderFacade {
    
    private final SessionContext sessionContext;
    private final DOMBuilderOptions options;
    
    public DOMBuilderFacade(final SessionContext sessionContext, final DOMBuilderOptions options) {
        this.sessionContext = sessionContext;
        this.options = options;
    }
    
    public void buildDOMSubtree(final Element targetRoot, final List<FlowToken> fixedTokens)
            throws SnuggleParseException {
        if (options.isDownConverting()) {
            /* We'll build into a "work" Document first and then adopt all of the final
             * resulting Nodes as children of the targetRoot
             */
            Document workDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
            Element workRoot = workDocument.createElement("root");
            workDocument.appendChild(workRoot);
            
            DOMBuilder domBuilder = new DOMBuilder(sessionContext, workRoot, options);
            domBuilder.buildDOMSubtree(fixedTokens);
            
            /* Down-convert our work document */
            Document downConvertedDocument = new DOMDownConverter(sessionContext, options)
                .downConvertDOM(workDocument);
            
            /* Pull the children of the <root/> in the resulting Document into the targetRoot */
            /* FIXME: This is NOT EFFICIENT AT ALL! */
            Element resultRoot = downConvertedDocument.getDocumentElement();
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
