/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.dombuilding;

import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.VariableManager;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder.OutputContext;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class AssumeHandler implements CommandHandler {
    
    public static final String ASSUME_VARIABLE_NAMESPACE = "assume-database";
    
    @SuppressWarnings("unchecked")
    public void handleCommand(DOMBuilder builder, Element parentElement, CommandToken token)
            throws SnuggleParseException {
        String assumptionType = builder.extractStringValue(token.getArguments()[0]);

        /* Need to generate assumptionTarget as a blob of MathML */
        builder.pushOutputContext(OutputContext.MATHML_INLINE);
        NodeList assumptionTarget = builder.extractNodeListValue(token.getArguments()[1]);
        builder.popOutputContext();
        
        VariableManager variableManager = builder.getVariableManager();
        Set<NodeList> assumptionTargetsByType = (Set<NodeList>) variableManager.getVariable(ASSUME_VARIABLE_NAMESPACE, assumptionType);
        if (assumptionTargetsByType==null) {
            assumptionTargetsByType = new HashSet<NodeList>();
            variableManager.setVariable(ASSUME_VARIABLE_NAMESPACE, assumptionType, assumptionTargetsByType);
        }
        assumptionTargetsByType.add(assumptionTarget);
        
        /* Now output all current assumptions */
        Map<String, Object> assumeVariableMap = variableManager.getVariableMapForNamespace(ASSUME_VARIABLE_NAMESPACE);
        Element assumptionsContainer = builder.appendSnuggleElement(parentElement, "assumptions");
        for (Entry<String, Object> assumeEntry : assumeVariableMap.entrySet()) {
            String assumptionProperty = assumeEntry.getKey();
            Set<NodeList> assumptionTargets = (Set<NodeList>) assumeEntry.getValue();
            Element assumeElement = builder.appendSnuggleElement(assumptionsContainer, "assume");
            assumeElement.setAttribute("property", assumptionProperty);
            for (NodeList target : assumptionTargets) {
                Element assumeTargetElement = builder.appendSnuggleElement(assumeElement, "target");
                for (int i=0, length=target.getLength(); i<length; i++) {
                    Node node = target.item(i);
                    Node copy = node.cloneNode(true);
                    assumeTargetElement.appendChild(copy);
                }
            }
        }
    }
}
