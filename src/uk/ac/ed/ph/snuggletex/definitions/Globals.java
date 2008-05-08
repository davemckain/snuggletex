/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.LR;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.MATH;
import static uk.ac.ed.ph.snuggletex.definitions.LaTeXMode.PARAGRAPH;

import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Lists various useful (but internal) constants and helper methods.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class Globals {
    
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    public static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";
    public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    
    public static final String ERROR_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/error-messages";
    public static final String GENERAL_MESSAGES_PROPERTIES_BASENAME = "uk/ac/ed/ph/snuggletex/general-messages";
    public static final String CSS_PROPERTIES_NAME = "uk/ac/ed/ph/snuggletex/css.properties";
    
    public static final EnumSet<LaTeXMode> MATH_MODE_ONLY = EnumSet.of(MATH);
    public static final EnumSet<LaTeXMode> PARA_MODE_ONLY = EnumSet.of(PARAGRAPH);
    public static final EnumSet<LaTeXMode> TEXT_MODE_ONLY = EnumSet.of(PARAGRAPH, LR);
    public static final EnumSet<LaTeXMode> ALL_MODES = EnumSet.allOf(LaTeXMode.class);
    
    /** Literal Math characters, mapped to their resulting interpretations */
    private static final Object[] mathCharacterData = new Object[] {
       '_', new SimpleMathOperatorInterpretation(MathMLOperator.SUB),
       '^', new SimpleMathOperatorInterpretation(MathMLOperator.SUPER),
       '+', new SimpleMathOperatorInterpretation(MathMLOperator.ADD),
       '-', new SimpleMathOperatorInterpretation(MathMLOperator.SUBTRACT),
       '=', new SimpleMathOperatorInterpretation(MathMLOperator.EQUALS),
       ',', new SimpleMathOperatorInterpretation(MathMLOperator.COMMA),
       '(', new MathBracketOperatorInterpretation(MathMLOperator.OPEN_BRACKET, MathMLOperator.CLOSE_BRACKET, true),
       ')', new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_BRACKET, MathMLOperator.OPEN_BRACKET, false),
       '[', new MathBracketOperatorInterpretation(MathMLOperator.OPEN_SQUARE_BRACKET, MathMLOperator.CLOSE_SQUARE_BRACKET, true),
       ']', new MathBracketOperatorInterpretation(MathMLOperator.CLOSE_SQUARE_BRACKET, MathMLOperator.OPEN_SQUARE_BRACKET, false)
    };
    
    private static final Map<Character, MathInterpretation> mathCharacterMap;
    
    static {
        mathCharacterMap = new HashMap<Character, MathInterpretation>();
        for (int i=0; i<mathCharacterData.length; ) {
            Character character = (Character) mathCharacterData[i++];
            MathInterpretation interprestation = (MathInterpretation) mathCharacterData[i++];
            mathCharacterMap.put(Character.valueOf(character), interprestation);
        }
    }
    
    public static MathInterpretation getMathCharacter(char c) {
        return mathCharacterMap.get(Character.valueOf(c));
    }
}
