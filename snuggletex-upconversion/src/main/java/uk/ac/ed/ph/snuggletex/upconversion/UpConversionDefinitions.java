/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base handler for the various assume-based commands.
 * 
 * @since 1.2.0
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConversionDefinitions {
    
    public static final String UPCONVERSION_NAMESPACE = "upconversion";
    public static final String OPTIONS_VARIABLE_NAME = "options";
    
    public static final String DO_CONTENT_MATHML_NAME = "doContentMathML";
    public static final String DO_MAXIMA_NAME = "doMaxima";
    
    public static final Set<String> SYMBOL_ASSUMPTION_TYPES = makeHashSet(new String[] {
            "function",
            "imaginaryNumber",
            "exponentialNumber",
            "constantPi",
            "eulerGamma"
    });
    
    public static final Set<String> BOOLEAN_VALUES = makeHashSet(new String[] {
            "true",
            "false"
    });
    
    public static final Set<String> BRACKET_VALUES = makeHashSet(new String[] {
            "list",
            "set",
            "vector",
            "none", /* (Treats brackets as grouping only with no special meaning) */
            "error", /* (Causes up-conversion to fail, instead of using default behaviour) */
    });
    
    public static final Map<String, OptionValueDefinition> OPTION_DEFINITIONS = makeDefinitionMap(new Object[] {
            DO_CONTENT_MATHML_NAME, BOOLEAN_VALUES, "true",
            DO_MAXIMA_NAME, BOOLEAN_VALUES, "true",
            "maximaOperatorFunction", null, "operator",
            "maximaUnitsFunction", null, "units",
            "addOptionsAnnotation", BOOLEAN_VALUES, "false",
            "roundBracketsAs", BRACKET_VALUES, "vector",
            "trivialRoundBracketsAs", BRACKET_VALUES, "none",
            "squareBracketsAs", BRACKET_VALUES, "list",
            "bracesAs", BRACKET_VALUES, "set",
            "emptyBracketsAs", BRACKET_VALUES, "list",
    });
    
    public static class OptionValueDefinition {
        
        private final Set<String> valueSpace;
        private final String defaultValue;
        
        public OptionValueDefinition(final Set<String> valueSpace, final String defaultValue) {
            this.valueSpace = valueSpace;
            this.defaultValue = defaultValue;
        }

        public Set<String> getValueSpace() {
            return valueSpace;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }
    
    private static Set<String> makeHashSet(String[] inputs) {
        Set<String> result = new HashSet<String>();
        for (String input : inputs) {
            result.add(input);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, OptionValueDefinition> makeDefinitionMap(Object[] inputs) {
        Map<String, OptionValueDefinition> result = new HashMap<String, OptionValueDefinition>();
        for (int i=0; i<inputs.length; ) {
            String propertyName = (String) inputs[i++];
            Set<String> valueSpace = (Set<String>) inputs[i++];
            String defaultValue = (String) inputs[i++];
            result.put(propertyName, new OptionValueDefinition(valueSpace, defaultValue));
        }
        return result;
    }
}
