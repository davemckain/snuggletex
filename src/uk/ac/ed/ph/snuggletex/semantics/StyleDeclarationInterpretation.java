/* $Id: StyleDeclarationInterpretation.java,v 1.7 2008/04/23 11:23:36 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.semantics;

/**
 * FIXME: Document this type;
 * 
 * @author  David McKain
 * @version $Revision: 1.7 $
 */
public enum StyleDeclarationInterpretation implements TextInterpretation {

    BF("div", "bf", "b",    null, "bold"),
    RM("div", "rm", "span", "rm", "normal"),
    EM("div", "em", "em",   null, null),
    IT("div", "it", "i",    null, "italic"),
    TT("div", "tt", "tt",   null, "monospace"),
    SC("div", "sc", "span", "sc", null),
    SL("div", "sl", "span", "sl", null),
    SF("div", "sf", "span", "sf", "sans-serif"),

    TINY("div", "tiny", "span", "tiny", null),
    SCRIPTSIZE("div", "scriptsize", "span", "scriptsize", null),
    FOOTNOTESIZE("div", "footnotesize", "span", "footnotesize", null),
    SMALL("div", "small", "span", "small", null),
    NORMALSIZE("div", "normalsize", "span", "normalsize", null),
    LARGE("div", "large", "span", "large", null),
    LARGE_2("div", "large2", "span", "large2", null),
    LARGE_3("div", "large3", "span", "large3", null),
    HUGE("div", "huge", "span", "huge", null),
    HUGE_2("div", "huge2", "span", "huge2", null),

    UNDERLINE("div", "underline", "span", "underline", null),
    
    ;
    
    private final String targetBlockXHTMLElementName;
    private final String targetBlockCSSClassName;
    private final String targetInlineXHTMLElementName;
    private final String targetInlineCSSClassName;
    private final String targetMathMLMathVariantName;
    
    private StyleDeclarationInterpretation(final String targetBlockXHTMLElementName,
            final String targetBlockCSSClassName, final String targetInlineXHTMLElementName,
            final String targetInlineCSSClassName, final String targetMathMLMathVariantName) {
        this.targetBlockXHTMLElementName = targetBlockXHTMLElementName;
        this.targetBlockCSSClassName = targetBlockCSSClassName;
        this.targetInlineXHTMLElementName = targetInlineXHTMLElementName;
        this.targetInlineCSSClassName = targetInlineCSSClassName;
        this.targetMathMLMathVariantName = targetMathMLMathVariantName;
    }
    
    public String getTargetBlockXHTMLElementName() {
        return targetBlockXHTMLElementName;
    }
    
    public String getTargetBlockCSSClassName() {
        return targetBlockCSSClassName;
    }


    public String getTargetInlineXHTMLElementName() {
        return targetInlineXHTMLElementName;
    }
    
    public String getTargetInlineCSSClassName() {
        return targetInlineCSSClassName;
    }

    public String getTargetMathMLMathVariantName() {
        return targetMathMLMathVariantName;
    }

    public InterpretationType getType() {
        return InterpretationType.STYLE_DECLARATION;
    }
}
