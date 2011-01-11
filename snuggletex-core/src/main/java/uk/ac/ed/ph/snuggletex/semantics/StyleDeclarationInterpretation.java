/* $Id:StyleDeclarationInterpretation.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

import uk.ac.ed.ph.snuggletex.definitions.Command;
import uk.ac.ed.ph.snuggletex.definitions.Environment;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle.FontFamily;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle.FontSize;
import uk.ac.ed.ph.snuggletex.internal.StyleEvaluator;

/**
 * Represents styled text in either MATH and/or TEXT Modes. {@link Command}s and
 * {@link Environment}s having this interpretation are removed from the parse tree
 * by the {@link StyleEvaluator}, so these are somewhat transient.
 * 
 * @author  David McKain
 * @version $Revision:179 $
 */
public enum StyleDeclarationInterpretation implements TextInterpretation {

    BF(FontFamily.BF, null),
    RM(FontFamily.RM, null),
    EM(FontFamily.EM, null),
    IT(FontFamily.IT, null),
    TT(FontFamily.TT, null),
    SC(FontFamily.SC, null),
    SL(FontFamily.SL, null),
    SF(FontFamily.SF, null),

    TINY(null, FontSize.TINY),
    SCRIPTSIZE(null, FontSize.SCRIPTSIZE),
    FOOTNOTESIZE(null, FontSize.FOOTNOTESIZE),
    SMALL(null, FontSize.SMALL),
    NORMALSIZE(null, FontSize.NORMALSIZE),
    LARGE(null, FontSize.LARGE),
    LARGE_2(null, FontSize.LARGE_2),
    LARGE_3(null, FontSize.LARGE_3),
    HUGE(null, FontSize.HUGE),
    HUGE_2(null, FontSize.HUGE_2),

    ;
    
    private final FontFamily fontFamily;
    
    private final FontSize fontSize;

    private StyleDeclarationInterpretation(final FontFamily fontFamily, final FontSize fontSize) {
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
    }
    
    public FontFamily getFontFamily() {
        return fontFamily;
    }

    public FontSize getFontSize() {
        return fontSize;
    }

    public InterpretationType getType() {
        return InterpretationType.STYLE_DECLARATION;
    }
}
