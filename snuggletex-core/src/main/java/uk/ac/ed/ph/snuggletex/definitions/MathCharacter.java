/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.definitions;

import uk.ac.ed.ph.snuggletex.internal.util.BeanToStringOptions;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumperOptions;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.PropertyOptions;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathCharacterInterpretation;

import java.util.EnumMap;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MathCharacter {
    
    public static enum MathCharacterType {
        ACCENT,
        ALPHA,
        BIN,
        CLOSE,
        FENCE,
        NUMERIC,
        OP,
        OPEN,
        ORD,
        PUNCT,
        REL,
    }

    private final int codePoint;
    private final String inputCommandName;
    private final MathCharacterType type;
    private final MathCharacterInterpretation mathCharacterInterpretation;
    private final EnumMap<InterpretationType, Interpretation> interpretationMap;
    
    public MathCharacter(int codePoint, String inputCommandName, MathCharacterType type) {
        this.codePoint = codePoint;
        this.inputCommandName = inputCommandName;
        this.type = type;
        this.mathCharacterInterpretation = new MathCharacterInterpretation(this);
        this.interpretationMap = new EnumMap<InterpretationType, Interpretation>(InterpretationType.class);
        interpretationMap.put(InterpretationType.MATH_CHARACTER, mathCharacterInterpretation);
    }
    
    public int getCodePoint() {
        return codePoint;
    }
    
    public String getInputCommandName() {
        return inputCommandName;
    }

    public MathCharacterType getType() {
        return type;
    }
    
    public String getChars() {
        return new String(Character.toChars(codePoint));
    }
    
    @ObjectDumperOptions(DumpMode.IGNORE)
    @BeanToStringOptions(PropertyOptions.IGNORE_PROPERTY)
    public MathCharacterInterpretation getMathCharacterInterpretation() {
        return mathCharacterInterpretation;
    }
    
    @ObjectDumperOptions(DumpMode.IGNORE)
    @BeanToStringOptions(PropertyOptions.IGNORE_PROPERTY)
    public EnumMap<InterpretationType, Interpretation> getInterpretationMap() {
        return interpretationMap;
    }
    
    public void addInterpretation(Interpretation interpretation) {
        interpretationMap.put(interpretation.getType(), interpretation);
    }
     
    @Override
    public String toString() {
        return ObjectUtilities.beanToString(this);
    }
}
