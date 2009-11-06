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

import java.util.HashMap;
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
    public static final String GENERAL_ASSUMPTIONS_VARIABLE_NAME = "general-assumptions";

    public static final Set<String> SYMBOL_ASSUMPTION_TYPES = makeHashSet(new String[] {
            "function",
            "imaginaryNumber",
            "exponentialNumber",
            "constantPi",
            "eulerGamma"
    });
    
    public static final Set<String> BRACKET_ASSUMPTION_PROPERTIES = makeHashSet(new String[] {
            "list",
            "set",
            "vector",
            "none", /* (Treats brackets as grouping only with no special meaning) */
            "error", /* (Causes up-conversion to fail, instead of using default behaviour) */
    });
    
    public static final Map<String, Set<String>> PROPERTY_ASSUMPTION_DEFS = makeAssumptionMap(new Object[] {
            "roundBracketsAs", BRACKET_ASSUMPTION_PROPERTIES,
            "trivialRoundBracketAs", BRACKET_ASSUMPTION_PROPERTIES,
            "squareBracketsAs", BRACKET_ASSUMPTION_PROPERTIES,
            "bracesAs", BRACKET_ASSUMPTION_PROPERTIES,
            "emptyBracketsAs", BRACKET_ASSUMPTION_PROPERTIES,
    });
        
    private static Set<String> makeHashSet(String[] inputs) {
        Set<String> result = new HashSet<String>();
        for (String input : inputs) {
            result.add(input);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Set<String>> makeAssumptionMap(Object[] inputs) {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (int i=0; i<inputs.length; ) {
            String propertyName = (String) inputs[i++];
            Set<String> propertyValue = (Set<String>) inputs[i++];
            result.put(propertyName, propertyValue);
        }
        return result;
    }
    
    //----------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    protected Map<ElementMapKeyWrapper, String> getSymbolAssumptionsMap(DOMBuilder builder) {
        /* Retrieve symbol assumptions map */
        VariableManager variableManager = builder.getVariableManager();
        Map<ElementMapKeyWrapper, String> result = (Map<ElementMapKeyWrapper, String>) variableManager.getVariable(ASSUME_VARIABLE_NAMESPACE, SYMBOL_ASSUMPTIONS_VARIABLE_NAME);
        if (result==null) {
            result = new HashMap<ElementMapKeyWrapper, String>();
            variableManager.setVariable(ASSUME_VARIABLE_NAMESPACE, SYMBOL_ASSUMPTIONS_VARIABLE_NAME, result);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, String> getPropertyAssumptionsMap(DOMBuilder builder) {
        /* Retrieve symbol assumptions map */
        VariableManager variableManager = builder.getVariableManager();
        Map<String, String> result = (Map<String, String>) variableManager.getVariable(ASSUME_VARIABLE_NAMESPACE, GENERAL_ASSUMPTIONS_VARIABLE_NAME);
        if (result==null) {
            result = new HashMap<String, String>();
            variableManager.setVariable(ASSUME_VARIABLE_NAMESPACE, GENERAL_ASSUMPTIONS_VARIABLE_NAME, result);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    protected void buildAssumptionsElement(DOMBuilder builder, Element parentElement) {
        Element assumptionsContainer = builder.appendSnuggleElement(parentElement, "assumptions");
        VariableManager variableManager = builder.getVariableManager();
        
        /* Property Assumptions */
        Map<String, String> propertyAssumptionsMap = (Map<String, String>) variableManager.getVariable(ASSUME_VARIABLE_NAMESPACE, GENERAL_ASSUMPTIONS_VARIABLE_NAME);
        if (propertyAssumptionsMap!=null) {
            for (Entry<String, String> bracketAssumptionEntry : propertyAssumptionsMap.entrySet()) {
                String name = bracketAssumptionEntry.getKey();
                String value = bracketAssumptionEntry.getValue();
                
                Element assumeElement = builder.appendSnuggleElement(assumptionsContainer, "property");
                assumeElement.setAttribute("name", name);
                assumeElement.setAttribute("value", value);
            }
        }
        
        /* Symbol assumptions */
        Map<ElementMapKeyWrapper, String> symbolAssumptionsMap = (Map<ElementMapKeyWrapper, String>) variableManager.getVariable(ASSUME_VARIABLE_NAMESPACE, SYMBOL_ASSUMPTIONS_VARIABLE_NAME);
        if (symbolAssumptionsMap!=null) {
            for (Entry<ElementMapKeyWrapper, String> symbolAssumptionEntry : symbolAssumptionsMap.entrySet()) {
                Element assumptionTarget = symbolAssumptionEntry.getKey().getSymbolElement();
                String assumptionProperty = symbolAssumptionEntry.getValue();
                
                Element assumeElement = builder.appendSnuggleElement(assumptionsContainer, "symbol");
                assumeElement.setAttribute("assume", assumptionProperty);
                Node assumptionTargetCopy = assumptionTarget.cloneNode(true);
                assumeElement.appendChild(assumptionTargetCopy);
            }
        }

    }
    
    protected Element ensureLegalSymbolTarget(DOMBuilder builder, Element parentElement, CommandToken token,
            NodeList targetNodeList)
            throws SnuggleParseException {
        /* Target must be a single Element Node... */
        if (targetNodeList.getLength()!=1 && !MathMLUtilities.isMathMLElement(targetNodeList.item(0))) {
            /* Error: unsupported symbol construct */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAES01);
            return null;
        }
        /* ...and either a <mn>, <mi> or <msub> with similar content */
        Element targetElement = (Element) targetNodeList.item(0);
        String localName = targetElement.getLocalName();
        if ("msub".equals(localName)) {
            if (ensureLegalSymbolTarget(builder, parentElement, token, targetElement.getChildNodes())==null) {
                return null;
            }
        }
        else if (!("mn".equals(localName) || "mi".equals(localName))) {
            /* Error: unsupported symbol construct */
            builder.appendOrThrowError(parentElement, token, UpConversionErrorCode.UAES01);
            return null;
        }
        return targetElement;
    }
}
