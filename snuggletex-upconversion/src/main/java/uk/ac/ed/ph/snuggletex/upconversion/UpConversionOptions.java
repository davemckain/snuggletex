/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.upconversion;

import static uk.ac.ed.ph.snuggletex.upconversion.UpConversionDefinitions.OPTION_DEFINITIONS;
import static uk.ac.ed.ph.snuggletex.upconversion.UpConversionDefinitions.SYMBOL_ASSUMPTION_TYPES;

import uk.ac.ed.ph.snuggletex.upconversion.UpConversionDefinitions.OptionValueDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * FIXME: Document this type!
 *
 * @since 1.2.0
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class UpConversionOptions {
    
    private final Map<String, String> options;
    
    private final Map<ElementWrapper, String> symbolAssumptions;
    
    public UpConversionOptions() {
        this.options = new HashMap<String, String>();
        this.symbolAssumptions = new HashMap<ElementWrapper, String>();
    }
    
    public boolean isOptionSpecified(final String name) {
        return options.containsKey(name);
    }
    
    public Set<String> getSpecifiedOptionNames() {
        return options.keySet();
    }
    
    public String getSpecifiedOptionValue(final String name) {
        if (!OPTION_DEFINITIONS.containsKey(name)) {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAEOP0, name);
        }
        return options.get(name);
    }
    
    public String getOptionValue(final String name, final boolean applyDefault) {
        if (!OPTION_DEFINITIONS.containsKey(name)) {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAEOP0, name);
        }
        return options.containsKey(name) ? options.get(name) : (applyDefault ? OPTION_DEFINITIONS.get(name).getDefaultValue() : null);
    }
    
    /**
     * @throws IllegalUpconversionOptionException
     */
    public void setSpecifiedOption(final String name, final String value) {
        OptionValueDefinition valueDefinition = OPTION_DEFINITIONS.get(name);
        if (valueDefinition==null) {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAEOP0, name);
        }
        Set<String> valueSpace = valueDefinition.getValueSpace();
        if (valueSpace!=null && !valueSpace.contains(value)) {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAEOP1, name, value);
        }
        options.put(name, value);
    }
    
    /**
     * @throws IllegalUpconversionOptionException
     */
    public void clearOption(final String name) {
        if (options.containsKey(name)) {
            options.remove(name);
        }
        else {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAEOP2, name);
        }
    }
    
    public Set<ElementWrapper> getAssumedSymbols() {
        return symbolAssumptions.keySet();
    }
    
    public String getSymbolAssumptionType(Element element) {
        return getSymbolAssumptionType(new ElementWrapper(element));
    }
    
    public String getSymbolAssumptionType(ElementWrapper elementWrapper) {
        return symbolAssumptions.get(elementWrapper);
    }
    
    /**
     * @throws IllegalUpconversionOptionException
     */
    public void assumeSymbol(Element element, String assumptionType) {
        if (!SYMBOL_ASSUMPTION_TYPES.contains(assumptionType)) {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAESY0, assumptionType);
        }
        symbolAssumptions.put(new ElementWrapper(element), assumptionType);
    }
    
    /**
     * @throws IllegalUpconversionOptionException
     */
    public void unassumeSymbol(Element element) {
        ElementWrapper wrapper = new ElementWrapper(element);
        if (symbolAssumptions.containsKey(wrapper)) {
            symbolAssumptions.remove(wrapper);
        }
        else {
            throw new IllegalUpconversionOptionException(UpConversionErrorCode.UAESY2);
        }
    }
}
