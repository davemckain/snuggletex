/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion.internal;

import uk.ac.ed.ph.snuggletex.dombuilding.CommandHandler;
import uk.ac.ed.ph.snuggletex.internal.DOMBuilder;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.VariableManager;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.upconversion.UpConversionErrorCode;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base handler for the various assume-based commands.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class AssumeHandlerBase implements CommandHandler {
    
    public static final String ASSUME_VARIABLE_NAMESPACE = "assume-database";
    public static final String SYMBOL_ASSUMPTIONS_VARIABLE_NAME = "symbol-assumptions";
    
    public static final String[] SYMBOL_ASSUMPTION_TYPES = {
        "function",
        "imaginary",
        "exponential"
    };
    
    protected static final Set<String> SYMBOL_ASSUMPTION_TYPES_SET;
    
    static {
        SYMBOL_ASSUMPTION_TYPES_SET = new HashSet<String>();
        for (String type : SYMBOL_ASSUMPTION_TYPES) {
            SYMBOL_ASSUMPTION_TYPES_SET.add(type);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void buildAssumptionsElement(DOMBuilder builder, Element parentElement) {
        Element assumptionsContainer = builder.appendSnuggleElement(parentElement, "assumptions");
        
        VariableManager variableManager = builder.getVariableManager();
        Map<ElementMapKeyWrapper, String> symbolAssumptionsMap = (Map<ElementMapKeyWrapper, String>) variableManager.getVariable(ASSUME_VARIABLE_NAMESPACE, SYMBOL_ASSUMPTIONS_VARIABLE_NAME);
        if (symbolAssumptionsMap!=null) {
            for (Entry<ElementMapKeyWrapper, String> symbolAssumptionEntry : symbolAssumptionsMap.entrySet()) {
                Element assumptionTarget = symbolAssumptionEntry.getKey().getSymbolElement();
                String assumptionType = symbolAssumptionEntry.getValue();
                
                Element assumeElement = builder.appendSnuggleElement(assumptionsContainer, "assumption");
                assumeElement.setAttribute("type", assumptionType);
                assumeElement.setAttribute("class", "symbol");
                Element assumeTargetElement = builder.appendSnuggleElement(assumeElement, "target");
                Node assumptionTargetCopy = assumptionTarget.cloneNode(true);
                assumeTargetElement.appendChild(assumptionTargetCopy);
            }
            /* TODO: Need to output other types of assumptions when get them as well... */
        }
    }
    
    public Element ensureLegalTargetSymbol(DOMBuilder builder, Element parentElement, CommandToken token,
            NodeList targetNodeList)
            throws SnuggleParseException {
        /* Target must be a single Element Node... */
        if (targetNodeList.getLength()!=1 && !MathMLUtilities.isMathMLElement(targetNodeList.item(0))) {
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAETG0);
            return null;
        }
        /* ...and either a <mn>, <mi> or <msub> with similar content */
        Element targetElement = (Element) targetNodeList.item(0);
        String localName = targetElement.getLocalName();
        if ("msub".equals(localName)) {
            if (ensureLegalTargetSymbol(builder, parentElement, token, targetElement.getChildNodes())==null) {
                return null;
            }
        }
        else if (!("mn".equals(localName) || "mi".equals(localName))) {
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAETG0);
            return null;
        }
        return targetElement;
    }
}
