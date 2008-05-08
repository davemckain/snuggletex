/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.Command;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.semantics.Interpretation;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathIdentifierInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator;
import uk.ac.ed.ph.snuggletex.semantics.MathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.SimpleMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.NottableMathOperatorInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathMLOperator.OperatorType;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.BraceContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.ErrorToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.SimpleToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;
import uk.ac.ed.ph.snuggletex.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This takes the output from {@link LaTeXTokeniser} and performs simplifications and
 * groupings on the {@link FlowToken}s that makes them easier to convert to a DOM.
 * 
 * @see LaTeXTokeniser
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class TokenFixer {
    
    private final SessionContext sessionContext;
    
    /** Whether to infer structures in maths */
    public boolean tryInferStructure = true;
    
    public TokenFixer(final SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
    
    //-----------------------------------------

    public void fixTokenTree(ArgumentContainerToken token) throws SnuggleParseException {
        visitBranch(token);
    }
    
    //-----------------------------------------
    
    private void visitBranch(Token rootToken) throws SnuggleParseException {
        /* Dive into containers */
        switch (rootToken.getType()) {
            case ARGUMENT_CONTAINER:
                visitContainerContent((ArgumentContainerToken) rootToken);
                break;
                
            case COMMAND:
                visitCommand((CommandToken) rootToken);
                break;
                
            case ENVIRONMENT:
                visitEnvironment(((EnvironmentToken) rootToken));
                break;
                
            case TEXT_MODE_TEXT:
                /* Currently not doing anything to this */
                break;
                
            case BRACE_CONTAINER:
                visitContainerContent(((BraceContainerToken) rootToken).getBraceContent());
                break;
                
            case VERBATIM_MODE_TEXT:
            case LR_MODE_NEW_PARAGRAPH:
            case MATH_NUMBER:
            case SINGLE_CHARACTER_MATH_IDENTIFIER:
            case SINGLE_CHARACTER_MATH_SPECIAL:
            case ERROR:
            case COMMENT:
            case TAB_CHARACTER:
                /* Nothing to do here */
                break;
                
            case NEW_PARAGRAPH:
                throw new SnuggleLogicException("Unfixed " + rootToken.getType() + " token: "
                        + rootToken);
                
            default:
                throw new SnuggleLogicException("Unhandled type " + rootToken.getType());
        }
    }

    private void visitEnvironment(EnvironmentToken environmentToken) throws SnuggleParseException {
        /* We may do special handling for certain environments */
        BuiltinEnvironment environment = environmentToken.getEnvironment();
        if (environment==GlobalBuiltins.ITEMIZE || environment==GlobalBuiltins.ENUMERATE) {
            fixListEnvironmentContent(environmentToken);
        }
        else if (environment==GlobalBuiltins.TABULAR || environment==GlobalBuiltins.EQNARRAY
                    || environment==GlobalBuiltins.EQNARRAYSTAR) {
            fixTabularEnvironmentContent(environmentToken);
        }
        
        /* Visit arguments and content */
        ArgumentContainerToken optArgument = environmentToken.getOptionalArgument();
        if (optArgument!=null) {
            visitContainerContent(optArgument);
        }
        ArgumentContainerToken[] arguments = environmentToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitContainerContent(argument);
            }
        }
        visitContainerContent(environmentToken.getContent());
    }
    
    private void visitCommand(CommandToken commandToken) throws SnuggleParseException {
        /* (Currently no requirement for any specific handling for certain commands) */
        
        /* Visit arguments and content */
        ArgumentContainerToken optArgument = commandToken.getOptionalArgument();
        if (optArgument!=null) {
            visitContainerContent(optArgument);
        }
        ArgumentContainerToken[] arguments = commandToken.getArguments();
        if (arguments!=null) {
            for (ArgumentContainerToken argument : arguments) {
                visitContainerContent(argument);
            }
        }
    }
    
    //-----------------------------------------
    
    private void visitContainerContent(ArgumentContainerToken parent) throws SnuggleParseException {
        /* Handle content as appropriate for the current mode */
        List<FlowToken> content = parent.getContents();
        switch (parent.getLatexMode()) {
            case PARAGRAPH:
                visitSiblingsParagraphMode(parent, content);
                break;
                
            case LR:
                visitSiblingsLRMode(parent, content);
                break;
                
            case MATH:
                visitSiblingsMathMode(parent, content);
                break;
                
            case VERBATIM:
                /* Nothing to do here! */
                break;
                
            default:
                throw new SnuggleLogicException("Unhandled mode " + parent.getLatexMode());
        }
    }
    
    //-----------------------------------------
    // PARAGRAPH mode stuff
    
    /**
     * NOTE: This does fix in place!
     * 
     * @throws SnuggleParseException 
     */
    private void visitSiblingsParagraphMode(Token parentToken, List<FlowToken> tokens) throws SnuggleParseException {
        groupStyleCommands(parentToken, tokens);
        stripRedundantWhitespaceTokens(tokens);
        inferParagraphs(tokens);
        for (FlowToken token : tokens) {
            visitBranch(token);
        }
    }
    
    /**
     * We go through looking for old TeX "style" and sizing commands like
     * 
     * <tt>A \\bf B ...</tt>
     * 
     * and replace with more explicit environment versions:
     * 
     * <tt>A \begin{bf} B ... \end{bf}</tt>
     *  
     * to make things a bit easier to handle.
     * <p>
     * Only commands that take no arguments are handled this way, since LaTeX style commands like
     * <tt>\\underline</tt> behave more like normal commands. 
     */
    private void groupStyleCommands(Token parentToken, List<FlowToken> tokens) {
        FlowToken token;
        for (int i=0; i<tokens.size(); i++) {
            token = tokens.get(i);
            if (token.getType()==TokenType.COMMAND && token.isInterpretationType(InterpretationType.STYLE_DECLARATION)
                    && ((CommandToken) token).getCommand().getArgumentCount()==0) {
                /* Look up the corresponding environment (having the same name as the command) */
                CommandToken commandToken = (CommandToken) token;
                BuiltinCommand command = commandToken.getCommand();
                BuiltinEnvironment environment = sessionContext.getEnvironmentByTeXName(command.getTeXName());
                if (environment==null) {
                    throw new SnuggleLogicException("No environment defined to replace old TeX command " + command);
                }
                
                /* Replacement environment content is everything from after the token */
                FlowToken lastToken = tokens.get(tokens.size()-1);
                ArgumentContainerToken contentToken = ArgumentContainerToken.createFromContiguousTokens(parentToken, token.getLatexMode(), tokens, i+1, tokens.size());
                FrozenSlice replacementSlice = token.getSlice().rightOuterSpan(lastToken.getSlice());
                
                /* Now make replacement and remove all tokens that come afterwards */
                EnvironmentToken replacement = new EnvironmentToken(replacementSlice, token.getLatexMode(), environment, contentToken);
                tokens.set(i, replacement);
                tokens.subList(i+1, tokens.size()).clear();
                break;
            }
        }
    }
    
    /**
     * Strips out redundant whitespace text tokens at the start and end of a List of siblings
     * and between a pair of "block" tokens.
     * 
     * @param tokens
     */
    private void stripRedundantWhitespaceTokens(List<FlowToken> tokens) {
        boolean blockBefore, blockAfter;
        FlowToken token;
        for (int i=0; i<tokens.size(); ) { /* (This does fix-in-place) */
            token = tokens.get(i);
            if (token.getType()==TokenType.TEXT_MODE_TEXT) {
                if ((i==0 || i==tokens.size()-1) && token.getSlice().isWhitespace()) {
                    /* Remove leading/trailing space token */
                    tokens.remove(i);
                    continue;
                }
                blockBefore = (i==0) || tokens.get(i-1).getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK;
                blockAfter = (i==tokens.size()-1) || tokens.get(i+1).getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK;
                if (blockBefore && blockAfter && token.getSlice().isWhitespace()) {
                    /* This token is whitespace between 2 blocks */
                    tokens.remove(i);
                    continue;
                }
            }
            /* If still here, then token was kept so move on */
            i++;
        }
    }
    
    /**
     * Infers explicit paragraphs by searching for the traditional LaTeX
     * {@link TokenType#NEW_PARAGRAPH} and/or {@link GlobalBuiltins#PAR} tokens, replacing with
     * the more tree-like {@link GlobalBuiltins#PARAGRAPH}.
     * 
     * @param tokens
     */
    private void inferParagraphs(List<FlowToken> tokens) {
        List<FlowToken> paragraphBuilder = new ArrayList<FlowToken>(); /* Builds up paragraph content */
        List<FlowToken> resultBuilder = new ArrayList<FlowToken>(); /* Builds up individual "paragraphs" */
        int paragraphCount = 0;
        boolean hasParagraphs = false;
        for (int i=0; i<tokens.size(); i++) {
            FlowToken token = tokens.get(i);
            if (token.getType()==TokenType.NEW_PARAGRAPH || token.isCommand(GlobalBuiltins.PAR)) {
                /* This token is an explicit "end current paragraph" token */
                hasParagraphs = true;
                if (!paragraphBuilder.isEmpty()) {
                    resultBuilder.add(buildGroupedCommandToken(token, GlobalBuiltins.PARAGRAPH, paragraphBuilder));
                    paragraphCount++;
                }
            }
            else if (token.getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK) {
                /* This token wants to start a new block, so first end current paragraph if one is
                 * being built and then add token. */
                hasParagraphs = true;
                if (!paragraphBuilder.isEmpty()) {
                    CommandToken leftOver = buildGroupedCommandToken(tokens.get(0), GlobalBuiltins.PARAGRAPH, paragraphBuilder);
                    resultBuilder.add(leftOver);
                    paragraphCount++;
                }
                resultBuilder.add(token);
            }
            else if (token.getTextFlowContext()==TextFlowContext.IGNORE && paragraphBuilder.isEmpty()) {
                /* This token makes no output and the current paragraph is empty so we'll just
                 * emit the token into the output grouping
                 */
                resultBuilder.add(token);
            }
            else {
                /* Normal inline token, or one which makes no output and occurs within the
                 * current paragraph, so add to this paragraph.
                 */
                paragraphBuilder.add(token);
            }
        }
        if (!hasParagraphs) {
            /* We didn't make any changes */
            return;
        }
        
        /* Finish off current paragraph */
        if (!paragraphBuilder.isEmpty()) {
            CommandToken leftOver = buildGroupedCommandToken(tokens.get(0), GlobalBuiltins.PARAGRAPH, paragraphBuilder);
            resultBuilder.add(leftOver);
            paragraphCount++;
        }

        /* We'll replace the existing tokens */
        tokens.clear();
        
        if (paragraphCount>1) {
            /* We ended up with multiple paragraphs */
            tokens.addAll(resultBuilder);
        }
        else {
            /* We ended up with a single paragraph, possibly mixed in with other tokens like
             * comments and stuff. As a slight optimisation, we'll pull up the paragraph's contents.
             */
            for (FlowToken resultToken : resultBuilder) {
                if (resultToken.isCommand(GlobalBuiltins.PARAGRAPH)) {
                    tokens.addAll(((CommandToken) resultToken).getArguments()[0].getContents());
                }
                else {
                    tokens.add(resultToken);
                }
            }
        }
    }
    
    //-----------------------------------------
    // LR mode stuff
    
    /**
     * NOTE: This does fix in place!
     * 
     * @throws SnuggleParseException 
     */
    private void visitSiblingsLRMode(Token parentToken, List<FlowToken> tokens) throws SnuggleParseException {
        groupStyleCommands(parentToken, tokens);
        stripBlocks(tokens);
        for (FlowToken token : tokens) {
            visitBranch(token);
        }
    }
    
    /**
     * LR Mode doesn't let "block" stuff do its normal thing. We strip out any "new paragraph"
     * markers and fail on any other kind of block tokens.
     * 
     * @param tokens
     * @throws SnuggleParseException 
     */
    private void stripBlocks(List<FlowToken> tokens) throws SnuggleParseException {
        /* This does fix-in-place */
        for (int i=0; i<tokens.size(); i++) {
            FlowToken token = tokens.get(i);
            if (token.getType()==TokenType.NEW_PARAGRAPH || token.isCommand(GlobalBuiltins.PAR)) {
                /* We'll replace with a space */
                tokens.set(i, new SimpleToken(token.getSlice(), TokenType.LR_MODE_NEW_PARAGRAPH,
                        LaTeXMode.LR, TextFlowContext.ALLOW_INLINE));
            }
            else if (token.getType()==TokenType.ERROR) {
                /* Keep errors as-is */
            }
            else if (token.getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK) {
                /* We're not allowing blocks inside LR mode, which is more prescriptive but generally
                 * consistent with LaTeX.
                 */
                tokens.set(i, createError(token, ErrorCode.TFEG00, token.getSlice().extract().toString()));
            }
        }
    }
    
    //-----------------------------------------
    // Commands and Environments

    /**
     * Helper to fix the representation of a list environment.
     * The original content should be of the form:
     * 
     * \item ..... \item ..... \item ......
     * 
     * (and maybe include {@link GlobalBuiltins#LIST_ITEM}s as well)
     * 
     * We replace with a more tree-like version.
     * 
     * If \item is used outside a suitable environment, then it will be left as-is. The
     * {@link DOMBuilder} will cope with this in due course.
     * 
     * @throws SnuggleParseException 
     */
    private void fixListEnvironmentContent(EnvironmentToken environmentToken)
            throws SnuggleParseException {
        List<FlowToken> contents = environmentToken.getContent().getContents();
        List<FlowToken> itemBuilder = new ArrayList<FlowToken>();
        List<FlowToken> resultBuilder = new ArrayList<FlowToken>();
        
        /* Go through contents, building up item groups */
        FlowToken token;
        boolean foundItem = false;
        for (int i=0, size=contents.size(); i<size; i++) {
            token = contents.get(i);
            if (token.isCommand(GlobalBuiltins.ITEM)) {
                /* Old-style \item. Stop building up content (if appropriate) and replace with
                 * new LIST_ITEM command */
                if (foundItem) {
                    CommandToken itemBefore = buildGroupedCommandToken(environmentToken, GlobalBuiltins.LIST_ITEM, itemBuilder);
                    resultBuilder.add(itemBefore);
                }
                foundItem = true;
                continue;
            }
            else if (!foundItem) {
                /* Found stuff before first \item. The only things we allow are comments and
                 * whitespace text */
                if (token.getType()==TokenType.COMMENT) {
                    resultBuilder.add(token);
                }
                else if (token.getType()==TokenType.TEXT_MODE_TEXT && token.getSlice().isWhitespace()
                        || token.getType()==TokenType.NEW_PARAGRAPH) {
                    /* This is whitespace, so we'll just ignore this token */
                }
                else {
                    /* Error: (non-trivial) content before first \item */
                    resultBuilder.add(createError(token, ErrorCode.TFEL00));
                }
            }
            else {
                /* Add to current item */
                itemBuilder.add(token);
            }
        }
        /* At end, finish off last item */
        if (foundItem) {
            resultBuilder.add(buildGroupedCommandToken(environmentToken, GlobalBuiltins.LIST_ITEM, itemBuilder));
        }
        
        /* Replace content */
        contents.clear();
        contents.addAll(resultBuilder);
    }
    
    /**
     * Helper to fix the content of tabular environments to make it more clearly demarcated
     * as rows and columns. This kills off instances of {@link GlobalBuiltins#CHAR_BACKSLASH} and
     * {@link TokenType#TAB_CHARACTER} within tables/arrays and replaces then with
     * zero or more {@link GlobalBuiltins#TABLE_ROW} each containing zero or more
     * {@link GlobalBuiltins#TABLE_COLUMN}.
     */
    private void fixTabularEnvironmentContent(EnvironmentToken environmentToken)
            throws SnuggleParseException {
        List<FlowToken> resultBuilder = new ArrayList<FlowToken>();
        List<CommandToken> rowBuilder = new ArrayList<CommandToken>();
        List<FlowToken> columnBuilder = new ArrayList<FlowToken>();
        List<FlowToken> contents = environmentToken.getContent().getContents();
        
        /* It's easier to process things if we explicitly add a final explicit "end row"
         * token to the environment contents if there's not one there already as it simplifies
         * the end of row processing. We'll cheat and use 'null' to signify this rather than
         * create a fake token.
         */
        List<FlowToken> entries = contents;
        if (entries.size()>1 && !entries.get(entries.size()-1).isCommand(GlobalBuiltins.CHAR_BACKSLASH)) {
            entries = new ArrayList<FlowToken>(entries);
            entries.add(null);
        }
        
        /* Go through contents, building up rows and columns */
        FlowToken token;
        FlowToken hlineToken = null;
        for (int i=0, size=entries.size(); i<size; i++) {
            token = entries.get(i);
            if (token==null || token.isCommand(GlobalBuiltins.CHAR_BACKSLASH)) {
                /* End of a row (see above). */
                if (hlineToken!=null) {
                    /* This row contains \\hline. This must be only token in the row. */
                    if (!columnBuilder.isEmpty()) {
                        /* Error: \\hline must be on its own within a row */
                        resultBuilder.add(createError(columnBuilder.get(0), ErrorCode.TFETB0));
                    }
                    else if (!rowBuilder.isEmpty()) {
                        /* Error: \\hline must be on its own within a row */
                        resultBuilder.add(createError(rowBuilder.get(0), ErrorCode.TFETB0));
                    }
                    /* Add \\hline to result as a "row" */
                    resultBuilder.add(hlineToken);
                    
                    /* Reset for next row */
                    hlineToken = null;
                }
                else {
                    /* This is a normal row. First, finish off the last column (which may be
                     * completely empty but should always exist) */
                    rowBuilder.add(buildGroupedCommandToken(environmentToken, GlobalBuiltins.TABLE_COLUMN, columnBuilder));
                    
                    /* Then add row */
                    resultBuilder.add(buildGroupedCommandToken(environmentToken, GlobalBuiltins.TABLE_ROW, rowBuilder));
                }
            }
            else if (token.getType()==TokenType.TEXT_MODE_TEXT && token.getSlice().isWhitespace()) {
                /* Whitespace token - we'll ignore this */
                continue;
            }
            else if (token.getType()==TokenType.COMMENT) {
                /* We'll strip comments as trying to keep them around makes life far too complicated */
                continue;
            }
            else if (token.getType()==TokenType.TAB_CHARACTER) {
                /* Ends the column being built. This may be null (e.g. '& &') so we need to consider
                 * that case carefully.
                 */
                rowBuilder.add(buildGroupedCommandToken(environmentToken, GlobalBuiltins.TABLE_COLUMN, columnBuilder));
            }
            else if (token.isCommand(GlobalBuiltins.HLINE)) {
                /* \\hline must be the only token in a row. */
                if (hlineToken!=null) {
                    /* Error: Only one \\hline per row */
                    resultBuilder.add(createError(columnBuilder.get(0), ErrorCode.TFETB0));
                }
                else {
                    hlineToken = token;
                }
            }
            else {
                /* Add to current column */
                columnBuilder.add(token);
            }
        }
        /* Replace content */
        contents.clear();
        contents.addAll(resultBuilder);
    }
    
    //-----------------------------------------
    // MathML stuff
    
    private void visitSiblingsMathMode(ArgumentContainerToken parent, List<FlowToken> tokens) throws SnuggleParseException {
        /* Perform fixes and semantic guess work as required if the tokens are in a context that would normally
         * make up some kind of expression. Examples where this is not the case is in the structural parts
         * of tabular content (after being fixed) which contain either a number of TABLE_ROW or TABLE_COLUMN
         * tokens.
         */
        if (tokens.isEmpty()) {
            return;
        }

        /* Decide whether we've got something structural as opposed to an expression.
         * 
         * NOTE: We may need to add things here if new types of structures need to be considered.
         */
        boolean isStructural = false;
        FlowToken firstToken = tokens.get(0);
        if (firstToken.getType()==TokenType.COMMAND) {
            Command command = ((CommandToken) firstToken).getCommand();
            if (command==GlobalBuiltins.TABLE_ROW || command==GlobalBuiltins.TABLE_COLUMN) {
                isStructural = true;
            }
        }
        
        /* If it looks like we've got an expression then tidy it up and try to infer semantics */
        if (!isStructural) {
            /* The order below is important in order to establish precedence */
            fencePairedParentheses(parent, tokens); /* (Want to get parentheses first) */
            fixOverInstances(parent, tokens);
            fixSubscriptAndSuperscripts(parent, tokens);
            fixPrimes(tokens);
            if (sessionContext.getConfiguration().isInferringMathStructure()) {
                inferParenthesisFences(parent, tokens);
                groupOverInfixOperators(parent, tokens);
                inferApplyFunctionAndInvisibleTimes(tokens);
            }
        }
        
        /* Visit each sub-token */
        for (FlowToken token : tokens) {
            visitBranch(token);
        }
    }
    
    /**
     * This handles the old-fashioned "... \over ..." by refactoring the tokens into a \frac{...}{...}.
     * As with LaTeX, we only allow one \over in a single level.
     * @throws SnuggleParseException 
     */
    private void fixOverInstances(ArgumentContainerToken parentToken, List<FlowToken> tokens) throws SnuggleParseException {
        int overIndex = -1; /* Will be set to index of \over token, if found */
        FlowToken token;
        for (int i=0; i<tokens.size(); i++) { /* Note: size() may change here */
            token = tokens.get(i);
            if (token.isCommand(GlobalBuiltins.OVER)) {
                if (overIndex!=-1) {
                    /* Multiple \over occurrence, which we're not going to allow so kill this expression */
                    tokens.clear();
                    tokens.add(createError(token, ErrorCode.TFEM00));
                    return;
                }
                overIndex = i;
            }
        }
        if (overIndex!=-1) {
            /* OK, we've got {... \over ...} which we'll convert into \frac{...}{...} */
            List<FlowToken> beforeTokens = new ArrayList<FlowToken>(tokens.subList(0, overIndex));
            List<FlowToken> afterTokens = new ArrayList<FlowToken>(tokens.subList(overIndex+1, tokens.size()));
            CommandToken replacement = new CommandToken(parentToken.getSlice(),
                    LaTeXMode.MATH,
                    GlobalBuiltins.FRAC,
                    null, /* No optional arg */
                    new ArgumentContainerToken[] {
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, beforeTokens), /* Numerator */
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, afterTokens)  /* Denominator */
            });
            /* Now we'll continue with our \frac{...}{...} token replacing the original tokens */
            tokens.clear();
            tokens.add(replacement);
        }
    }
    
    /**
     * Hunts through tokens for occurrences of primes used as <tt>f'</tt>, which are converted
     * to a superscript by binding to the preceding token
     * 
     * @param tokens
     */
    private void fixPrimes(List<FlowToken> tokens) {
        FlowToken leftToken, maybePrimeToken, replacementToken;
        FrozenSlice replacementSlice;
        for (int i=0; i<tokens.size()-1; i++) { /* We're fixing in place so tokens.size() may decrease over time */
            maybePrimeToken = tokens.get(i+1);
            if (maybePrimeToken.isInterpretationType(InterpretationType.MATH_IDENTIFIER)
                    && ((MathIdentifierInterpretation) maybePrimeToken.getInterpretation()).getName().equals("'")) {
                /* Found a prime, so combine with previous token */
                leftToken = tokens.get(i);
                replacementSlice = leftToken.getSlice().rightOuterSpan(maybePrimeToken.getSlice());
                replacementToken = new CommandToken(replacementSlice, LaTeXMode.MATH, GlobalBuiltins.MSUP, null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, leftToken),
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, maybePrimeToken),
                });
                tokens.set(i, replacementToken);
                tokens.remove(i+1);
                /* Keep searching! */
            }
        }
    }
    
    /**
     * We'll look for any occurrences of T1_T2[^T3] or T1^T2[_T3], where [..] denotes
     * "optional". If found, we'll replace with the more tree-like equivalents:
     * 
     * \msub{T1}{T2}
     * \msup{T1}{T2}
     * \msubsup{T1}{T2}{T3}
     * 
     * which will be easier to handle later on.
     */
    private void fixSubscriptAndSuperscripts(Token parentToken, List<FlowToken> tokens) throws SnuggleParseException {
        int size, startModifyIndex;
        FlowToken subOrSuperToken;
        FlowToken t1, t2, t3;
        SimpleMathOperatorInterpretation tokenInterp;
        MathMLOperator tokenOperator = null;
        MathMLOperator followingOperator;
        boolean isSubOrSuper;
        boolean firstIsSuper;
        for (int i=0; i<tokens.size(); i++) { /* NB: tokens.size() may decrease during this loop! */
            size = tokens.size();
            subOrSuperToken = tokens.get(i);
            firstIsSuper = false;
            isSubOrSuper = false;
            if (subOrSuperToken.isInterpretationType(InterpretationType.MATH_OPERATOR)) {
                tokenInterp = (SimpleMathOperatorInterpretation) subOrSuperToken.getInterpretation();
                tokenOperator = tokenInterp.getOperator();
                isSubOrSuper = tokenOperator==MathMLOperator.SUPER || tokenOperator==MathMLOperator.SUB;
            }
            if (!isSubOrSuper) {
                continue;
            }
            /* OK, we've found a '_' or '^'. As with LaTeX, we raise an error if it is *last* token
             * amongst siblings but allow it to be the first, in which case it is applied to a fake
             * empty token.
             */
            if (i==size-1) {
                /* Error: Trailing subscript/superscript */
                tokens.set(i, createError(subOrSuperToken, ErrorCode.TFEM01));
                continue;
            }
            if (i==0) {
            	/* No token before sub/super, so we'll make a pretend one */
            	ArgumentContainerToken emptyBeforeContainer = ArgumentContainerToken.createEmptyContainer(parentToken, LaTeXMode.MATH);
            	t1 = new BraceContainerToken(emptyBeforeContainer.getSlice(), LaTeXMode.MATH, emptyBeforeContainer);
            	startModifyIndex = i;
            }
            else {
            	/* Found token before sub/super */
            	t1 = tokens.get(i-1);
            	startModifyIndex = i-1;
            }
            t2 = tokens.get(i+1);
            
            /* See if there's another '^' or '_' afterwards */
            t3 = null;
            followingOperator = null;
            if (i+2<size && tokens.get(i+2).isInterpretationType(InterpretationType.MATH_OPERATOR)) {
                followingOperator = ((SimpleMathOperatorInterpretation) tokens.get(i+2).getInterpretation()).getOperator();
                if (followingOperator==MathMLOperator.SUPER || followingOperator==MathMLOperator.SUB) {
                    /* OK, need to find the "T3" operator! */
                    if (i+3>=size) {
                        /* Trailing super/subscript */
                        tokens.set(i-1, createError(subOrSuperToken, ErrorCode.TFEM01));
                        tokens.subList(i, i+3).clear();
                        continue;
                    }
                    t3 = tokens.get(i+3);
                    
                    /* Make sure we've got the right pair of operators e.g. not something like T1^T2^T3 */
                    if (tokenOperator==MathMLOperator.SUPER && followingOperator==MathMLOperator.SUPER
                            || tokenOperator==MathMLOperator.SUB && followingOperator==MathMLOperator.SUB) {
                        /* Double super/subscript */
                        tokens.set(i-1, createError(subOrSuperToken, ErrorCode.TFEM02));
                        tokens.subList(i, i+3).clear();
                        continue;
                    }
                }
            }
            /* Now be build the replacements */
            FrozenSlice replacementSlice;
            BuiltinCommand replacementCommand;
            firstIsSuper = tokenOperator==MathMLOperator.SUPER;
            if (t3!=null) {
                /* Create replacement, replacing tokens at i-1,i+1,i+2 and i+3 */
                replacementSlice = t1.getSlice().rightOuterSpan(t3.getSlice());
                replacementCommand = GlobalBuiltins.MSUBSUP;
                CommandToken replacement = new CommandToken(replacementSlice,
                        LaTeXMode.MATH,
                        replacementCommand,
                        null, /* No optional args */
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t1),
                            firstIsSuper ? ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t3) : ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t2),
                            firstIsSuper ? ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t2) : ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t3)        
                });
                tokens.set(startModifyIndex, replacement);
                tokens.subList(startModifyIndex+1, i+4).clear();
            }
            else {
                /* Just replace tokens at i-1, i, i+1 */
                replacementSlice = t1.getSlice().rightOuterSpan(t2.getSlice());
                replacementCommand = firstIsSuper ? GlobalBuiltins.MSUP : GlobalBuiltins.MSUB;
                CommandToken replacement = new CommandToken(replacementSlice, LaTeXMode.MATH,
                        replacementCommand,
                        null, /* No optional args */
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t1),
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t2)
                });
                tokens.set(startModifyIndex, replacement);
                tokens.subList(startModifyIndex+1, i+2).clear();
            }
        }
    }
    
    /**
     * This looks for an outermost pairs of \left and \right and converts them to a more tree-like
     * fence token.
     * <p>
     * Mismatched pairs will cause an error, as they do in LaTeX.
     * 
     * @param tokens
     * @throws SnuggleParseException
     */
    private void fencePairedParentheses(Token parentToken, List<FlowToken> tokens) throws SnuggleParseException {
        FlowToken token;
        LEFT_SEARCH: for (int i=0; i<tokens.size(); i++) { /* (List may change from 'i' onwards during loop) */
            token = tokens.get(i);
            
            /* Is this a \left? If so, work out where its balancing \right is and
             * wrap inside a fake environment. We'll also make sure we don't find a 
             * \right before a left!
             */
            if (token.isCommand(GlobalBuiltins.RIGHT)) {
                /* \right had not preceding \left */
                tokens.set(i, createError(token, ErrorCode.TFEM03));
                continue LEFT_SEARCH;
            }
            else if (token.isCommand(GlobalBuiltins.LEFT)) {
                /* Now search forward for matching \right */
                List<FlowToken> innerTokens = new ArrayList<FlowToken>();
                CommandToken openBracketToken = (CommandToken) token;
                CommandToken matchingCloseBracketToken = null;
                int matchingCloseBracketIndex = -1;
                int bracketLevel = 1;
                FlowToken innerToken;
                MATCH_SEARCH: for (int j=i+1; j<tokens.size(); j++) { /* 'j' is search index from current point onwards */
                    innerToken = tokens.get(j);
                    if (innerToken.isCommand(GlobalBuiltins.LEFT)) {
                        bracketLevel++;
                    }
                    else if (innerToken.isCommand(GlobalBuiltins.RIGHT)) {
                        bracketLevel--;
                        if (bracketLevel==0) {
                            /* We've found the matcher */
                            matchingCloseBracketToken = (CommandToken) innerToken;
                            matchingCloseBracketIndex = j;
                            break MATCH_SEARCH;
                        }
                    }
                    innerTokens.add(innerToken);
                }
                if (matchingCloseBracketToken==null) {
                    /* We never found a match for \\left so we'll kill the whole expression off */
                    tokens.set(i, createError(token, ErrorCode.TFEM04));
                    tokens.subList(i, tokens.size()).clear();
                    break LEFT_SEARCH;
                }
                /* Now replace this bracket with a fence */
                FrozenSlice replacementSlice = openBracketToken.getSlice().rightOuterSpan(matchingCloseBracketToken.getSlice());
                EnvironmentToken replacementToken = new EnvironmentToken(replacementSlice,
                        LaTeXMode.MATH,
                        GlobalBuiltins.FENCED,
                        null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, openBracketToken.getCombinerTarget()),
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, matchingCloseBracketToken.getCombinerTarget())
                        },
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, innerTokens)
                );
                tokens.set(i, replacementToken);
                tokens.subList(i+1, matchingCloseBracketIndex+1).clear();
                continue LEFT_SEARCH;
            }
        }
    }
    
    /**
     * This attempts to group balanced pairs of open and close brackets which normally are considered
     * matching into a corresponding fence token.
     * <p>
     * In order for this to work, the pair and all "inner" pairs must all be naturally balanced.
     * <p>
     * Note that this means $[1,2[$ and $[1,2)$ will *not* be considered matching, even
     * though both are common in subjects like Mathematical Analysis.
     * <p>
     * To get semantics here, you must use
     * 
     * <tt>$\left[1,2\right[$ and $\left[1,2\right)$</tt>
     * 
     * and let {@link #fencePairedParentheses(Token, List)} take care of this for you.
     */
    private void inferParenthesisFences(Token parentToken, List<FlowToken> tokens) {
        /* The algorithm used here is similar to groupPairedParentheses() */
        FlowToken token;
        LEFT_SEARCH: for (int i=0; i<tokens.size(); i++) { /* (List may change from 'i' onwards during loop) */
            token = tokens.get(i);
            if (!token.isInterpretationType(InterpretationType.MATH_BRACKET_OPERATOR)) {
                continue LEFT_SEARCH;
            }
            MathBracketOperatorInterpretation interpretation = (MathBracketOperatorInterpretation) token.getInterpretation();
            if (!interpretation.isOpener()) {
                /* No use - we started with a close! */
                return;
            }
            /* If we're here, then we found some sort of open bracket. We'll search forward for
             * the matching close, taking care to balance up matching open/close pairs we see on our way.
             */
            List<FlowToken> innerTokens = new ArrayList<FlowToken>();
            FlowToken openBracketToken = token;
            FlowToken matchingCloseBracketToken = null;
            int matchingCloseBracketIndex = -1;
            FlowToken innerToken;
            Stack<MathBracketOperatorInterpretation> openerStack = new Stack<MathBracketOperatorInterpretation>();
            openerStack.add(interpretation);
            MATCH_SEARCH: for (int j=i+1; j<tokens.size(); j++) { /* 'j' is search index from current point onwards */
                innerToken = tokens.get(j);
                if (innerToken.isInterpretationType(InterpretationType.MATH_BRACKET_OPERATOR)) {
                    MathBracketOperatorInterpretation innerInterpretation = (MathBracketOperatorInterpretation) innerToken.getInterpretation();
                    if (innerInterpretation.isOpener()) {
                        openerStack.add(innerInterpretation);
                    }
                    else {
                        /* Make sure the close matches the last open */
                        MathBracketOperatorInterpretation lastOpen = openerStack.pop(); /* (This will always succeed here) */
                        if (!innerInterpretation.getOperator().equals(lastOpen.getPartnerOperator())) {
                            return;
                        }
                        if (openerStack.isEmpty()) {
                            /* Yay! We've found a balance */
                            matchingCloseBracketToken = innerToken;
                            matchingCloseBracketIndex = j;
                            break MATCH_SEARCH;
                        }
                    }
                }
                innerTokens.add(innerToken);
            }
            if (matchingCloseBracketToken==null) {
                /* We never found a match */
                return;
            }
            /* Now replace this bracket with a fence */
            FrozenSlice replacementSlice = openBracketToken.getSlice().rightOuterSpan(matchingCloseBracketToken.getSlice());
            EnvironmentToken replacementToken = new EnvironmentToken(replacementSlice,
                    LaTeXMode.MATH,
                    GlobalBuiltins.FENCED,
                    null,
                    new ArgumentContainerToken[] {
                        ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, openBracketToken),
                        ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, matchingCloseBracketToken)
                    },
                    ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, innerTokens)
            );
            tokens.set(i, replacementToken);
            tokens.subList(i+1, matchingCloseBracketIndex+1).clear();
            continue LEFT_SEARCH;
        }
    }
    
    /**
     * This attempts to group subexpressions across infix operators, provided that all
     * tokens are non-brackets. Having bracket tokens indicates deeper complexity that
     * this grouping algorithm won't be able to cope with!
     * <p>
     * (This should be run after attempting to convert matching brackets to fences in order
     * to be more likely to succeed.)
     * 
     * @param tokens
     */
    private void groupOverInfixOperators(ArgumentContainerToken parent, List<FlowToken> tokens) {
        List<FlowToken> resultBuilder = new ArrayList<FlowToken>(); /* (Won't fix in place this time) */
        List<FlowToken> groupBuilder = new ArrayList<FlowToken>(); /* Builds up subexpression groups */
        FlowToken token;
        boolean isInfixOperator;
        for (int i=0, size=tokens.size(); i<size; i++) {
            token = tokens.get(i);
            
            /* Make sure this isn't a bracket */
            if (token.isInterpretationType(InterpretationType.MATH_BRACKET_OPERATOR)) {
                /* Give up trying! */
                return;
            }
            
            /* Is this an operator? If so, is it infix? */
            MathMLOperator operator = resolveMathMLOperator(token);
            isInfixOperator = operator!=null && operator.getOperatorType()==OperatorType.INFIX;
            
            /* Decide what to do */
            if (!isInfixOperator) {
                /* Not an infix operator so add to current group */
                groupBuilder.add(token);
            }
            else {
                /* It's an infix operator */
                if (groupBuilder.isEmpty()) {
                    if (i==0) {
                        /* Leading infix operator at start of whole expression */
                        resultBuilder.add(token);
                    }
                    else {
                        /* Infix was used at the start of a sub-expression, so need to group */
                        groupBuilder.add(token);
                    }
                }
                else {
                    /* This operator ends the current group. So group together what's there so far
                     * (if required) and add to result list.
                     */
                    resultBuilder.add(maybeBuildGroupedCommandContainerToken(parent, GlobalBuiltins.MROW, groupBuilder));
                    resultBuilder.add(token);
                }
            }
        }
        /* See if we actually found an infix operator. If we didn't, then no grouping should
         * be attempted.
         */
        if (resultBuilder.isEmpty()) {
            return;
        }
        /* Handle final group in expression */
        if (!groupBuilder.isEmpty()) {
            resultBuilder.add(maybeBuildGroupedCommandContainerToken(parent, GlobalBuiltins.MROW, groupBuilder));
        }
        
        /* Replace original tokens with our result */
        tokens.clear();
        tokens.addAll(resultBuilder);
    }
    
    private void inferApplyFunctionAndInvisibleTimes(List<FlowToken> tokens) {
        boolean justDidFunction = false;
        boolean justDidNonOperator = false;
        FlowToken token;
        boolean isFunction;
        OperatorType operatorType;
        MathMLOperator operator;
        boolean isOperator;
        List<FlowToken> resultBuilder = new ArrayList<FlowToken>(); 
        for (int i=0, size=tokens.size(); i<size; i++) {
            token = tokens.get(i);
            operator = resolveMathMLOperator(token);
            operatorType = operator!=null ? operator.getOperatorType() : null;
            isOperator = operator!=null;
            isFunction = token.isInterpretationType(InterpretationType.MATH_FUNCTION_IDENTIFIER);
            
            if (token.isInterpretationType(InterpretationType.MATH_BRACKET_OPERATOR) || operatorType==OperatorType.INFIX) {
                /* We can't deal with brackets and infix operators. */
                return;
            }
            
            /* If we did a function last time round and this token isn't an "ApplyFunction", add one now */
            if (justDidFunction && !token.isCommand(GlobalBuiltins.APPLY_FUNCTION)) {
                /* We post-increment 'i' so it still points to the current token, which has moved right one place */
                resultBuilder.add(new CommandToken(token.getSlice(), token.getLatexMode(), GlobalBuiltins.APPLY_FUNCTION));
            }
            /* If we did a non-fn/operator last time round, add "InivisibleTimes" now, but only
             * if we're not about to output a postfix operator and if the current token is not an explicit InvisibleTimes
             */
            if (justDidNonOperator && !token.isCommand(GlobalBuiltins.INVISIBLE_TIMES) &&
                    (operatorType==null || operatorType!=OperatorType.POSTFIX)) {
                resultBuilder.add(new CommandToken(token.getSlice(), token.getLatexMode(), GlobalBuiltins.INVISIBLE_TIMES));
            }
            /* Then add the current token */
            resultBuilder.add(token);
            
            /* If we just did a function, set flag so that we maybe add "ApplyFunction" next time */
            justDidFunction = isFunction;
            
            /* If we did a non-operator or non-function, set flag so that we maybe output "InvisibleTimes" next time */
            justDidNonOperator = !(isFunction || isOperator);
        }
        /* Replace original tokens */
        tokens.clear();
        tokens.addAll(resultBuilder);
    }
    
    /**
     * Helper to resolve the MathML operator behind the given Token, if appropriate. This also
     * handles the case of resolving the target of a <tt>\not</tt> token.
     * 
     * @param token
     */
    private MathMLOperator resolveMathMLOperator(FlowToken token) {
        Interpretation interpretation = token.getInterpretation();
        if (interpretation instanceof MathOperatorInterpretation) {
            return ((MathOperatorInterpretation) interpretation).getOperator();
        }
        if (token.isCommand(GlobalBuiltins.NOT)) {
            CommandToken notToken = (CommandToken) token;
            FlowToken targetToken = notToken.getCombinerTarget();
            if (targetToken.isInterpretationType(InterpretationType.MATH_RELATION_OPERATOR)) {
                return ((NottableMathOperatorInterpretation) targetToken.getInterpretation()).getNotOperator();
            }
            throw new SnuggleLogicException("Unexpected logic branch - we should already have ensured that \\not is followed by a relation operator?!");
        }
        return null;
    }
    
    //-----------------------------------------
    // Helpers
    
    /**
     * Useful helper. Takes a "builder" that has been accumulating tokens. Groups all tokens
     * into a {@link CommandToken} containing the accumulated tokens as a single argument
     * and clears the builder.
     * <p>
     * An empty group is legal and will result in a "fake" empty token assuming the same Slice
     * and LaTeXMode as parentToken.
     * 
     * @param command
     * @param itemBuilder
     * @return null if the builder is empty, otherwise grouped {@link CommandToken}
     */
    private CommandToken buildGroupedCommandToken(final Token parentToken,
            final BuiltinCommand command, final List<? extends FlowToken> itemBuilder) {
        ArgumentContainerToken contentToken;
        if (itemBuilder.isEmpty()) {
        	contentToken = ArgumentContainerToken.createEmptyContainer(parentToken, parentToken.getLatexMode());
        }
        else {
            contentToken = ArgumentContainerToken.createFromContiguousTokens(parentToken, itemBuilder.get(0).getLatexMode(), itemBuilder);
        }
        CommandToken result = new CommandToken(contentToken.getSlice(), contentToken.getLatexMode(),
                command,
                null, /* No optional argument */
                new ArgumentContainerToken[] { contentToken } /* Single argument containing content */
        );
        itemBuilder.clear();
        return result;
    }

    /**
     * Version of {@link #maybeBuildGroupedCommandContainerToken(Token, BuiltinCommand, List)} that only groups
     * if there are zero or more and 1 items. If 1 item, returns the sole item itself.
     * 
     * @param command
     * @param itemBuilder
     */
    private FlowToken maybeBuildGroupedCommandContainerToken(final Token parentToken,
            final BuiltinCommand command, final List<? extends FlowToken> itemBuilder) {
        if (itemBuilder.size()==1) {
            FlowToken result = itemBuilder.get(0);
            itemBuilder.clear();
            return result;
        }
        return buildGroupedCommandToken(parentToken, command, itemBuilder);
    }

    private ErrorToken createError(final FlowToken token, final ErrorCode errorCode,
            final Object... arguments) throws SnuggleParseException {
        FrozenSlice slice = token.getSlice();
        InputError error = new InputError(errorCode, slice, arguments);
        sessionContext.registerError(error);
        return new ErrorToken(error, token.getLatexMode());
    }
}
