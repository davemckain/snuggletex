/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.Command;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.Environment;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle.FontFamily;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle.FontSize;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.StyleDeclarationInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.BraceContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.RootToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;

import java.util.List;

/**
 * This calculates the {@link ComputedStyle} for each raw parsed {@link Token}. It is
 * used directly after {@link LaTeXTokeniser} but before the {@link TokenFixer}.
 * <p>
 * Once this has run, all {@link Command}s and {@link Environment}s with a
 * {@link StyleDeclarationInterpretation} will have been removed from the parse tree. This
 * corresponds to commands such as \bf, \textrm{...} and environments like \begin{em}...\end{em}.
 * Each {@link Token} remaining in the tree will have its
 * {@link Token#setComputedStyle(ComputedStyle)} called
 * to indicate the resulting "style" it will have.
 * 
 * @see LaTeXTokeniser
 * @see TokenFixer
 * @see StyleRebuilder
 * @see SnuggleSession#parseInput(uk.ac.ed.ph.snuggletex.SnuggleInput)
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class StyleEvaluator {
    
    @SuppressWarnings("unused")
    private final SessionContext sessionContext;
    
    public StyleEvaluator(final SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
    
    //-----------------------------------------

    public void evaluateStyles(RootToken rootToken) throws SnuggleParseException {
        rootToken.setComputedStyle(ComputedStyle.DEFAULT_STYLE);
        visitSiblings(rootToken.getContents(), ComputedStyle.DEFAULT_STYLE);
    }
    
    //-----------------------------------------
    
    private void visitSiblings(List<FlowToken> content, ComputedStyle scopeStyle)
            throws SnuggleParseException {
        ComputedStyle currentStyle = scopeStyle;
        FlowToken token;
        for (int index=0; index<content.size(); ) { /* (The siblings will be fixed in place here) */
            token = content.get(index);
            
            /* Evaluate and then handle any style change tokens, which are all either commands
             * or environments
             */
            if (token.hasInterpretationType(InterpretationType.STYLE_DECLARATION)) {
                ComputedStyle newStyle = mergeStyle(currentStyle, (StyleDeclarationInterpretation) token.getInterpretation(InterpretationType.STYLE_DECLARATION));
                if (token instanceof CommandToken) {
                    CommandToken commandToken = (CommandToken) token;
                    if (commandToken.getCommand().getArgumentCount()==0) {
                        /* Old-fashioned declarations like \\bf change the style for subsequent
                         * tokens and get removed from the parse tree */
                        currentStyle = newStyle;
                        content.remove(index); /* Subsequent tokens move back one, so next iteration starts from same index */
                    }
                    else {
                        /* Must be a \textrm{..} or \mathrm{...} command.
                         * We evaluate the content using the new style, then flatten the resulting
                         * tokens into the parse tree.
                         */
                        ArgumentContainerToken styleContentToken = commandToken.getArguments()[0];
                        visitContainerContent(styleContentToken, newStyle);
                        content.remove(index);
                        content.addAll(index, styleContentToken.getContents());
                        index += styleContentToken.getContents().size(); /* Skip over what we've just pulled in */
                    }
                }
                else if (token instanceof EnvironmentToken) {
                    /* Must be \begin{rm}...\end{rm} or similar. So we descend into content and
                     * flatten in the same way as above.
                     */
                    ArgumentContainerToken styleContentToken = ((EnvironmentToken) token).getContent();
                    visitContainerContent(styleContentToken, newStyle);
                    content.remove(index);
                    content.addAll(index, styleContentToken.getContents());
                    index += styleContentToken.getContents().size(); /* Skip over what we've just pulled in */
                }
                else {
                    /* We should have accounted for all possibilities above */
                    throw new SnuggleLogicException("Unexpected logic branch");
                }
                /* Continue from the same index */
                continue;
            }
            
            /* For non style change tokens, we tell it what the current ComputedStyle is and
             * then descend into its parse subtree as appropriate */
            token.setComputedStyle(currentStyle);
            switch (token.getType()) {
                case COMMAND:
                    visitCommand((CommandToken) token, currentStyle);
                    break;
                    
                case ENVIRONMENT:
                    visitEnvironment((EnvironmentToken) token, currentStyle);
                    break;
                    
                case BRACE_CONTAINER:
                    BraceContainerToken braceToken = (BraceContainerToken) token;
                    visitSiblings(braceToken.getContents(), newStyleScope(currentStyle));
                    break;
                    
                case TEXT_MODE_TEXT:
                case VERBATIM_MODE_TEXT:
                case LR_MODE_NEW_PARAGRAPH:
                case MATH_NUMBER:
                case MATH_CHARACTER:
                case ERROR:
                case TAB_CHARACTER:
                case NEW_PARAGRAPH:
                    /* Nothing to do here */
                    break;
                    
                default:
                    throw new SnuggleLogicException("Unhandled/unexpected TokenType " + token.getType());
            }
            index++; /* (Since we kept this token, next iteration looks at the next token) */
        }
    }
    
    private void visitContainerContent(ArgumentContainerToken parent, ComputedStyle scopeStyle) throws SnuggleParseException {
        parent.setComputedStyle(scopeStyle);
        visitSiblings(parent.getContents(), scopeStyle);
    }

    private void visitCommand(CommandToken commandToken, ComputedStyle currentStyle) throws SnuggleParseException {
        /* Visit arguments and content */
        ArgumentContainerToken optArgument = commandToken.getOptionalArgument();
        if (optArgument!=null) {
            visitContainerContent(optArgument, currentStyle);
        }
        ArgumentContainerToken[] arguments = commandToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitContainerContent(argument, currentStyle);
            }
        }
        ArgumentContainerToken combinerTarget = commandToken.getCombinerTarget();
        if (combinerTarget!=null) {
            visitContainerContent(combinerTarget, currentStyle);
        }
    }

    private void visitEnvironment(EnvironmentToken environmentToken, ComputedStyle currentStyle) throws SnuggleParseException {
        /* Visit arguments */
        ArgumentContainerToken optArgument = environmentToken.getOptionalArgument();
        if (optArgument!=null) {
            visitContainerContent(optArgument, currentStyle);
        }
        ArgumentContainerToken[] arguments = environmentToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitContainerContent(argument, currentStyle);
            }
        }
        
        /* Now descend into content.
         * 
         * We use current style, unless we're transitioning into MATH mode, in which case the
         * font size is inherited but the font family reverts to the default.
         */
        ComputedStyle contentStyle = currentStyle;
        if (environmentToken.getEnvironment().getContentMode()==LaTeXMode.MATH && environmentToken.getLatexMode()!=LaTeXMode.MATH) {
            /* We're transitioning into MATH mode.
             * 
             * MATH Mode inherits the current font size, but reverts font family
             */
            contentStyle = new ComputedStyle(contentStyle, FontFamily.NORMAL, contentStyle.getFontSize());
        }
        visitContainerContent(environmentToken.getContent(), contentStyle);
    }
    
    //-----------------------------------------
    
    private ComputedStyle newStyleScope(ComputedStyle currentStyle) {
        if (currentStyle==null) {
            throw new SnuggleLogicException("currentStyle should not be null");
        }
        return new ComputedStyle(currentStyle, currentStyle.getFontFamily(), currentStyle.getFontSize());
    }
    
    private ComputedStyle mergeStyle(ComputedStyle currentStyle, StyleDeclarationInterpretation interpretation) {
        FontFamily newFontFamily = interpretation.getFontFamily();
        FontSize newFontSize = interpretation.getFontSize();
        return new ComputedStyle(currentStyle, newFontFamily!=null ? newFontFamily : currentStyle.getFontFamily(),
                newFontSize!=null ?  newFontSize : currentStyle.getFontSize());
    }
}
