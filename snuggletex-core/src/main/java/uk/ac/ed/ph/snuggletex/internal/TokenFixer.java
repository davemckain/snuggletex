/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleLogicException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.Command;
import uk.ac.ed.ph.snuggletex.definitions.ComputedStyle;
import uk.ac.ed.ph.snuggletex.definitions.CoreErrorCode;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;
import uk.ac.ed.ph.snuggletex.definitions.LaTeXMode;
import uk.ac.ed.ph.snuggletex.definitions.TextFlowContext;
import uk.ac.ed.ph.snuggletex.semantics.InterpretationType;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation;
import uk.ac.ed.ph.snuggletex.semantics.MathBracketInterpretation.BracketType;
import uk.ac.ed.ph.snuggletex.semantics.MathNumberInterpretation;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.BraceContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.CommandToken;
import uk.ac.ed.ph.snuggletex.tokens.EnvironmentToken;
import uk.ac.ed.ph.snuggletex.tokens.ErrorToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.tokens.RootToken;
import uk.ac.ed.ph.snuggletex.tokens.SimpleToken;
import uk.ac.ed.ph.snuggletex.tokens.Token;
import uk.ac.ed.ph.snuggletex.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This takes the parse tree after {@link StyleEvaluator} has run and performs grouping operations
 * on the {@link FlowToken}s to convert them to a more tree-like structure that is easier to handle.
 * <p>
 * Once this has run, all {@link BraceContainerToken} tokens present in PARAGRAPH mode will have
 * been unwrapped or flattened. They are however kept in MATH and LR mode as they can be used for
 * implicit brackets. (Unwinding of redundantly nested containers is done in all cases, though.)
 * <p>
 * Many other types of tokens, such as the "new paragraph" or "new list item" token will have been
 * removed, with the surrounding content grouped accordingly.
 * 
 * @see LaTeXTokeniser
 * @see StyleEvaluator
 * @see StyleRebuilder
 * @see SnuggleSession#parseInput(uk.ac.ed.ph.snuggletex.SnuggleInput)
 * 
 * @author  David McKain
 * @version $Revision$
 */
public final class TokenFixer {
    
    private final SessionContext sessionContext;
    
    public TokenFixer(final SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
    
    //-----------------------------------------

    public void fixTokenTree(RootToken rootToken) throws SnuggleParseException {
        visitSiblings(rootToken, rootToken.getContents());
    }
    
    //-----------------------------------------

    
    private void visitSiblings(Token parent, List<FlowToken> content)
            throws SnuggleParseException {
        /* Unwind fully braced groups */
        while (content.size()==1 && content.get(0).getType()==TokenType.BRACE_CONTAINER) {
            List<FlowToken> innerContent = ((BraceContainerToken) content.get(0)).getContents();
            content.clear();
            content.addAll(innerContent);
        }
        
        /* Handle content as appropriate for the current mode */
        switch (parent.getLatexMode()) {
            case PARAGRAPH:
                visitSiblingsParagraphMode(content);
                break;
                
            case LR:
                visitSiblingsLRMode(content);
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
    
    private void visitToken(Token startToken) throws SnuggleParseException {
        /* Dive into containers */
        switch (startToken.getType()) {
            case ARGUMENT_CONTAINER:
                visitContainerContent((ArgumentContainerToken) startToken);
                break;
                
            case COMMAND:
                visitCommand((CommandToken) startToken);
                break;
                
            case ENVIRONMENT:
                visitEnvironment((EnvironmentToken) startToken);
                break;
                
            case BRACE_CONTAINER:
                BraceContainerToken braceContainer = (BraceContainerToken) startToken;
                visitSiblings(braceContainer, braceContainer.getContents());
                break;
                
            case TEXT_MODE_TEXT:
            case VERBATIM_MODE_TEXT:
            case LR_MODE_NEW_PARAGRAPH:
            case MATH_NUMBER:
            case MATH_CHARACTER:
            case ERROR:
            case TAB_CHARACTER:
                /* Nothing to do here */
                break;
                
            case NEW_PARAGRAPH:
                throw new SnuggleLogicException("Unfixed " + startToken.getType() + " token: "
                        + startToken);
                
            default:
                throw new SnuggleLogicException("Unhandled/unexpected TokenType " + startToken.getType());
        }
    }
    
    private void visitContainerContent(ArgumentContainerToken parent) throws SnuggleParseException {
        visitSiblings(parent, parent.getContents());
    }
    
    private void visitChildren(List<FlowToken> tokens) throws SnuggleParseException {
        for (FlowToken token : tokens) {
            visitToken(token);
        }
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

    private void visitEnvironment(EnvironmentToken environmentToken) throws SnuggleParseException {
        /* We may do special handling for certain environments */
        BuiltinEnvironment environment = environmentToken.getEnvironment();
        if (environment.hasInterpretation(InterpretationType.LIST)) {
            fixListEnvironmentContent(environmentToken);
        }
        else if (environment.hasInterpretation(InterpretationType.TABULAR)) {
            fixTabularEnvironmentContent(environmentToken);
        }
        
        /* Visit arguments (usually)...
         * 
         * We don't drill into the arguments of ENV_BRACKETED as that will end up with an infinite
         * loop of parenthesis nesting!
         */
        if (environment!=CorePackageDefinitions.ENV_BRACKETED) {
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
        }
        
        /* Visit content */
        visitContainerContent(environmentToken.getContent());
    }
    
    private void flattenBraceContainers(List<FlowToken> tokens) {
        FlowToken token;
        for (int index=0; index<tokens.size(); ) {
            token = tokens.get(index);
            if (token.getType()==TokenType.BRACE_CONTAINER) {
                List<FlowToken> braceContent = ((BraceContainerToken) token).getContents();
                tokens.remove(index);
                tokens.addAll(index, braceContent);
                continue; /* Continue from the same index in case of deep braces */
            }
            index++;
        }
    }
    
    //-----------------------------------------
    // PARAGRAPH mode stuff
    
    private void visitSiblingsParagraphMode(List<FlowToken> tokens) throws SnuggleParseException {
        flattenBraceContainers(tokens);
        stripRedundantWhitespaceTokens(tokens);
        inferParagraphs(tokens);
        visitChildren(tokens);
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
     * {@link TokenType#NEW_PARAGRAPH} and/or {@link CorePackageDefinitions#CMD_PAR} tokens, replacing with
     * the more tree-like {@link CorePackageDefinitions#CMD_PARAGRAPH}.
     * 
     * @param tokens
     */
    private void inferParagraphs(List<FlowToken> tokens) {
        List<FlowToken> paraContentBuilder = new ArrayList<FlowToken>(); /* Builds up paragraph content */
        List<FlowToken> resultBuilder = new ArrayList<FlowToken>(); /* Builds up individual "paragraphs" */
        int paragraphCount = 0;
        boolean hasParagraphs = false;
        for (int i=0; i<tokens.size(); i++) {
            FlowToken token = tokens.get(i);
            if (token.getType()==TokenType.NEW_PARAGRAPH || token.isCommand(CorePackageDefinitions.CMD_PAR)) {
                /* This token is an explicit "end current paragraph" token */
                hasParagraphs = true;
                if (!paraContentBuilder.isEmpty()) {
                    resultBuilder.add(buildGroupedCommandToken(token, CorePackageDefinitions.CMD_PARAGRAPH,
                            paraContentBuilder, paraContentBuilder.get(0).getComputedStyle()));
                    paragraphCount++;
                }
            }
            else if (token.getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK) {
                /* This token wants to start a new block, so first end current paragraph if one is
                 * being built and then add token. */
                hasParagraphs = true;
                if (!paraContentBuilder.isEmpty()) {
                    CommandToken leftOver = buildGroupedCommandToken(tokens.get(0), CorePackageDefinitions.CMD_PARAGRAPH,
                            paraContentBuilder, paraContentBuilder.get(0).getComputedStyle());
                    resultBuilder.add(leftOver);
                    paragraphCount++;
                }
                resultBuilder.add(token);
            }
            else if (token.getTextFlowContext()==TextFlowContext.IGNORE && paraContentBuilder.isEmpty()) {
                /* This token makes no output and the current paragraph is empty so we'll just
                 * emit the token into the output grouping
                 */
                resultBuilder.add(token);
            }
            else {
                /* Normal inline token, or one which makes no output and occurs within the
                 * current paragraph, so add to this paragraph.
                 */
                paraContentBuilder.add(token);
            }
        }
        if (!hasParagraphs) {
            /* We didn't make any changes */
            return;
        }
        
        /* Finish off current paragraph */
        if (!paraContentBuilder.isEmpty()) {
            CommandToken leftOver = buildGroupedCommandToken(tokens.get(0), CorePackageDefinitions.CMD_PARAGRAPH,
                    paraContentBuilder, paraContentBuilder.get(0).getComputedStyle());
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
                if (resultToken.isCommand(CorePackageDefinitions.CMD_PARAGRAPH)) {
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
    
    private void visitSiblingsLRMode(List<FlowToken> tokens) throws SnuggleParseException {
        flattenBraceContainers(tokens);
        stripBlocks(tokens);
        visitChildren(tokens);
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
            if (token.getType()==TokenType.NEW_PARAGRAPH || token.isCommand(CorePackageDefinitions.CMD_PAR)) {
                /* We'll replace with a space */
                SimpleToken replacementToken = new SimpleToken(token.getSlice(), TokenType.LR_MODE_NEW_PARAGRAPH,
                        LaTeXMode.LR, TextFlowContext.ALLOW_INLINE);
                replaceToken(tokens, i, replacementToken);
            }
            else if (token.getType()==TokenType.ERROR) {
                /* Keep errors as-is */
            }
            else if (token.getTextFlowContext()==TextFlowContext.START_NEW_XHTML_BLOCK) {
                /* We're not allowing blocks inside LR mode, which is more prescriptive but generally
                 * consistent with LaTeX.
                 */
                replaceToken(tokens, i, createError(token, CoreErrorCode.TFEG00, token.getSlice().extract().toString()));
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
     * (and maybe include {@link CorePackageDefinitions#CMD_LIST_ITEM}s as well)
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
        CommandToken lastItemToken = null;
        for (int i=0, size=contents.size(); i<size; i++) {
            token = contents.get(i);
            if (token.isCommand(CorePackageDefinitions.CMD_ITEM)) {
                /* Old-style \item. Stop building up content (if appropriate) and replace with
                 * new LIST_ITEM command */
                if (lastItemToken!=null) {
                    CommandToken itemBefore = buildGroupedCommandToken(environmentToken, CorePackageDefinitions.CMD_LIST_ITEM,
                            itemBuilder, lastItemToken.getComputedStyle());
                    resultBuilder.add(itemBefore);
                }
                lastItemToken = (CommandToken) token;
                continue;
            }
            else if (lastItemToken==null) {
                /* Found stuff before first \item. The only thing we allow is whitespace text */
                if (token.getType()==TokenType.TEXT_MODE_TEXT && token.getSlice().isWhitespace()
                        || token.getType()==TokenType.NEW_PARAGRAPH) {
                    /* This is whitespace, so we'll just ignore this token */
                }
                else {
                    /* Error: (non-trivial) content before first \item */
                    resultBuilder.add(createError(token, CoreErrorCode.TFEL00));
                }
            }
            else {
                /* Add to current item */
                itemBuilder.add(token);
            }
        }
        /* At end, finish off last item */
        if (lastItemToken!=null) {
            resultBuilder.add(buildGroupedCommandToken(environmentToken, CorePackageDefinitions.CMD_LIST_ITEM,
                    itemBuilder, lastItemToken.getComputedStyle()));
        }
        
        /* Replace content */
        contents.clear();
        contents.addAll(resultBuilder);
    }
    
    /**
     * Helper to fix the content of tabular environments to make it more clearly demarcated
     * as rows and columns. This kills off instances of {@link CorePackageDefinitions#CMD_CHAR_BACKSLASH} and
     * {@link TokenType#TAB_CHARACTER} within tables/arrays and replaces then with
     * zero or more {@link CorePackageDefinitions#CMD_TABLE_ROW} each containing zero or more
     * {@link CorePackageDefinitions#CMD_TABLE_COLUMN}.
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
        if (entries.size()>0 && !entries.get(entries.size()-1).isCommand(CorePackageDefinitions.CMD_CHAR_BACKSLASH)) {
            entries = new ArrayList<FlowToken>(entries);
            entries.add(null);
        }
        
        /* Go through contents, building up rows and columns */
        FlowToken token;
        FlowToken lastGoodToken = null;
        ComputedStyle columnStartStyle = null; /* Will track style for first token in column */
        for (int i=0, size=entries.size(); i<size; i++) {
            token = entries.get(i);
            if (columnStartStyle==null) {
                /* New column will start here (or shortly after), so calculate current style */
                if (token!=null) {
                    columnStartStyle = token.getComputedStyle();
                }
                else {
                    /* (This is the special end of row token added above) */
                    columnStartStyle = (i>0) ? entries.get(0).getComputedStyle() : environmentToken.getComputedStyle();
                }
            }
            if (token==null || token.isCommand(CorePackageDefinitions.CMD_CHAR_BACKSLASH)) {
                /* End of a row (see above). */
                if (token==null && lastGoodToken!=null && lastGoodToken.isCommand(CorePackageDefinitions.CMD_HLINE)) {
                    /* Last good token was \\hline so leave it there */
                    break;
                }
                /* First, finish off the last column (which may be
                 * completely empty but should always exist) */
                /* Working out the initial style for this column is also not so trivial here as
                 * there are lots of corner cases to consider.
                 */
                rowBuilder.add(buildGroupedCommandToken(environmentToken, CorePackageDefinitions.CMD_TABLE_COLUMN, columnBuilder, columnStartStyle));
                
                /* Then add row */
                resultBuilder.add(buildGroupedCommandToken(environmentToken, CorePackageDefinitions.CMD_TABLE_ROW, rowBuilder, rowBuilder.get(0).getComputedStyle()));
            }
            else if (token.getType()==TokenType.TEXT_MODE_TEXT && token.getSlice().isWhitespace()) {
                /* Whitespace token - we'll ignore this */
                continue;
            }
            else if (token.getType()==TokenType.TAB_CHARACTER) {
                /* Ends the column being built. This may be null (e.g. '& &') so we need to consider
                 * that case carefully.
                 */
                rowBuilder.add(buildGroupedCommandToken(environmentToken, CorePackageDefinitions.CMD_TABLE_COLUMN, columnBuilder, columnStartStyle));
                columnStartStyle = null;
            }
            else if (token.isCommand(CorePackageDefinitions.CMD_HLINE)) {
                /* \\hline must be the only token in a row. It immediately ends the current row */
                if (!columnBuilder.isEmpty()) {
                    /* Error: \\hline must be on its own within a row */
                    resultBuilder.add(createError(columnBuilder.get(0), CoreErrorCode.TFETB0));
                    columnBuilder.clear(); 
                }
                else if (!rowBuilder.isEmpty()) {
                    /* Error: \\hline must be on its own within a row */
                    resultBuilder.add(createError(rowBuilder.get(0), CoreErrorCode.TFETB0));
                    rowBuilder.clear();
                }
                /* Add \\hline to result as a "row" */
                resultBuilder.add(token);
            }
            else {
                /* Add to current column */
                columnBuilder.add(token);
            }
            /* If we didn't "continue" above, then record this token for the next loop to help
             * us to decide what to do on the last token.
             */
            lastGoodToken = token;
        }
        /* Replace content */
        contents.clear();
        contents.addAll(resultBuilder);
    }
    
    //-----------------------------------------
    // MathML stuff
    
    private void visitSiblingsMathMode(Token parentToken, List<FlowToken> tokens)
            throws SnuggleParseException {
        if (tokens.isEmpty()) {
            return;
        }
        
        /* Perform fixes and semantic guess work as required if the tokens are in a context that would normally
         * make up some kind of expression. Examples where this is not the case is in the structural parts
         * of tabular content (after being fixed) which contain either a number of TABLE_ROW or TABLE_COLUMN
         * tokens.
         * 
         * NOTE: We may need to add things here if new types of structures need to be considered.
         */
        boolean isStructural = false;
        FlowToken firstToken = tokens.get(0);
        if (firstToken.getType()==TokenType.COMMAND) {
            Command command = ((CommandToken) firstToken).getCommand();
            if (command==CorePackageDefinitions.CMD_TABLE_ROW || command==CorePackageDefinitions.CMD_TABLE_COLUMN) {
                isStructural = true;
            }
        }
        
        /* If it looks like we've got an expression then tidy it up and perform some basic semantic inference */
        if (!isStructural) {
            /* The order below is important in order to establish precedence */
            fixLeadingNegativeNumber(tokens);
            fencePairedParentheses(parentToken, tokens); /* (Want to get parentheses first) */
            fixOverInstances(parentToken, tokens);
            inferParenthesisFences(parentToken, tokens);
            fixSubscriptAndSuperscripts(parentToken, tokens);
            fixPrimes(tokens);
        }
        
//        /* Then flatten braces */
//        flattenBraceContainers(tokens);
        
        /* Visit each sub-token */
        visitChildren(tokens);
    }
    
    /**
     * Converts leading occurrences of '-' followed by a {@link MathNumberInterpretation}
     * into a single token representing the negation of the given number.
     * 
     * @param tokens
     */
    private void fixLeadingNegativeNumber(List<FlowToken> tokens) {
        if (tokens.size() < 2) {
            return;
        }
        FlowToken firstToken = tokens.get(0);
        FlowToken secondToken = tokens.get(1);
        if (firstToken.getMathCharacterCodePoint()=='-' && secondToken.hasInterpretationType(InterpretationType.MATH_NUMBER)) {
            CharSequence negation = "-" + ((MathNumberInterpretation) secondToken.getInterpretation(InterpretationType.MATH_NUMBER)).getNumber();
            SimpleToken replacementToken = new SimpleToken(firstToken.getSlice().rightOuterSpan(secondToken.getSlice()),
                    TokenType.MATH_NUMBER, firstToken.getLatexMode(),
                    null, new MathNumberInterpretation(negation));
            replaceTokens(tokens, 0, 2, replacementToken);
        }
    }
    
    /**
     * This handles the old-fashioned "... \over ..." by refactoring the tokens into a \frac{...}{...}.
     * As with LaTeX, we only allow one \over in a single level.
     * @throws SnuggleParseException 
     */
    private void fixOverInstances(Token parentToken, List<FlowToken> tokens) throws SnuggleParseException {
        int overIndex = -1; /* Will be set to index of \over token, if found */
        FlowToken token;
        for (int i=0; i<tokens.size(); i++) { /* Note: size() may change here */
            token = tokens.get(i);
            if (token.isCommand(CorePackageDefinitions.CMD_OVER)) {
                if (overIndex!=-1) {
                    /* Multiple \over occurrence, which we're not going to allow so kill this expression */
                    tokens.clear();
                    tokens.add(createError(token, CoreErrorCode.TFEM00));
                    return;
                }
                overIndex = i;
            }
        }
        if (overIndex!=-1) {
            /* OK, we've got {... \over ...} which we'll convert into \frac{...}{...}.
             * Each argument will assume the style that was in place at the start of the original
             * \over expression.
             */
            List<FlowToken> beforeTokens = new ArrayList<FlowToken>(tokens.subList(0, overIndex));
            List<FlowToken> afterTokens = new ArrayList<FlowToken>(tokens.subList(overIndex+1, tokens.size()));
            ComputedStyle beforeStyle = tokens.get(0).getComputedStyle();
            CommandToken replacementToken = new CommandToken(parentToken.getSlice(),
                    LaTeXMode.MATH,
                    CorePackageDefinitions.CMD_FRAC,
                    null, /* No optional arg */
                    new ArgumentContainerToken[] {
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, beforeTokens, beforeStyle), /* Numerator */
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, afterTokens, beforeStyle)  /* Denominator */
            });
            replaceTokens(tokens, 0, tokens.size(), replacementToken);
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
            if (maybePrimeToken.getMathCharacterCodePoint()=='\'') {
                /* Found a prime, so combine with previous token */
                leftToken = tokens.get(i);
                replacementSlice = leftToken.getSlice().rightOuterSpan(maybePrimeToken.getSlice());
                replacementToken = new CommandToken(replacementSlice, LaTeXMode.MATH, CorePackageDefinitions.CMD_MSUP_OR_MOVER, null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, leftToken),
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, maybePrimeToken),
                });
                replaceTokens(tokens, i, i+2, replacementToken);
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
        FlowToken token;
        FlowToken t1, t2, t3;
        ArgumentContainerToken t1Result, t2Result, t3Result;
        int tokenCodePoint;
        int followingCodePoint;
        boolean isSubOrSuper;
        boolean firstIsSuper;
        for (int i=0; i<tokens.size(); i++) { /* NB: tokens.size() may decrease during this loop! */
            size = tokens.size();
            token = tokens.get(i);
            firstIsSuper = false;
            isSubOrSuper = false;
            tokenCodePoint = token.getMathCharacterCodePoint();
            firstIsSuper = tokenCodePoint=='^';
            isSubOrSuper = firstIsSuper || tokenCodePoint=='_';
            if (!isSubOrSuper) {
                continue;
            }
            /* OK, we've found a '_' or '^'. As with LaTeX, we raise an error if it is *last* token
             * amongst siblings but allow it to be the first, in which case it is applied to a fake
             * empty token.
             */
            if (i==size-1) {
                /* Error: Trailing subscript/superscript */
                replaceToken(tokens, i, createError(token, CoreErrorCode.TFEM01));
                continue;
            }
            if (i==0) {
                /* No token before sub/super, so we'll eventually create an empty container for it */
                t1 = token;
                t1Result = ArgumentContainerToken.createEmptyContainer(parentToken, LaTeXMode.MATH, tokens.get(0).getComputedStyle());
                startModifyIndex = i;
            }
            else {
                /* Found token before sub/super */
                t1 = tokens.get(i-1);
                t1Result = ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t1);
                startModifyIndex = i-1;
            }
            t2 = tokens.get(i+1);
            t2Result = ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t2);
            
            /* See if there's another '^' or '_' afterwards */
            t3 = null;
            t3Result = null;
            if (i+2<size) {
                followingCodePoint = tokens.get(i+2).getMathCharacterCodePoint();
                if (followingCodePoint=='_' || followingCodePoint=='^') {
                    /* OK, need to find the "T3" operator! */
                    if (i+3>=size) {
                        /* Trailing super/subscript */
                        replaceTokens(tokens, startModifyIndex, i+3, createError(token, CoreErrorCode.TFEM01));
                        continue;
                    }
                    t3 = tokens.get(i+3);
                    t3Result = ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, t3);
                    
                    /* Make sure we've got the right pair of operators e.g. not something like T1^T2^T3 */
                    if ((tokenCodePoint=='^' && followingCodePoint=='^')
                            || (tokenCodePoint=='_' && followingCodePoint=='_')) {
                        /* Double super/subscript */
                        replaceTokens(tokens, startModifyIndex, i+3, createError(token, CoreErrorCode.TFEM02));
                        continue;
                    }
                }
            }
            /* Now be build the replacements */
            FrozenSlice replacementSlice;
            BuiltinCommand replacementCommand;
            if (t3!=null) {
                /* Create replacement, replacing tokens at startModifyIndex up to i+3 */
                replacementSlice = t1.getSlice().rightOuterSpan(t3.getSlice());
                replacementCommand = CorePackageDefinitions.CMD_MSUBSUP_OR_MUNDEROVER;
                CommandToken replacementToken = new CommandToken(replacementSlice,
                        LaTeXMode.MATH,
                        replacementCommand,
                        null, /* No optional args */
                        new ArgumentContainerToken[] {
                            t1Result,
                            firstIsSuper ? t3Result : t2Result,
                            firstIsSuper ? t2Result : t3Result        
                });
                replaceTokens(tokens, startModifyIndex, i+4, replacementToken);
            }
            else {
                /* Just replace tokens at startModifyIndex up to i+2 */
                replacementSlice = t1.getSlice().rightOuterSpan(t2.getSlice());
                replacementCommand = firstIsSuper ? CorePackageDefinitions.CMD_MSUP_OR_MOVER : CorePackageDefinitions.CMD_MSUB_OR_MUNDER;
                CommandToken replacementToken = new CommandToken(replacementSlice, LaTeXMode.MATH,
                        replacementCommand,
                        null, /* No optional args */
                        new ArgumentContainerToken[] {
                            t1Result,
                            t2Result
                });
                replaceTokens(tokens, startModifyIndex, i+2, replacementToken);
            }
        }
    }
    
    /**
     * This looks for an outermost pairs of \left and \right and converts them to a more tree-like
     * fence token.
     * <p>
     * Mismatched pairs will cause an error, as they do in LaTeX.
     * 
     * @see #inferParenthesisFences(Token, List)
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
            if (token.isCommand(CorePackageDefinitions.CMD_RIGHT)) {
                /* Error: \right had not preceding \left */
                replaceToken(tokens, i, createError(token, CoreErrorCode.TFEM03));
                continue LEFT_SEARCH;
            }
            else if (token.isCommand(CorePackageDefinitions.CMD_LEFT)) {
                /* Now search forward for matching \right */
                List<FlowToken> innerTokens = new ArrayList<FlowToken>();
                CommandToken openBracketToken = (CommandToken) token;
                CommandToken matchingCloseBracketToken = null;
                int matchingCloseBracketIndex = -1;
                int bracketLevel = 1;
                FlowToken innerToken;
                MATCH_SEARCH: for (int j=i+1; j<tokens.size(); j++) { /* 'j' is search index from current point onwards */
                    innerToken = tokens.get(j);
                    if (innerToken.isCommand(CorePackageDefinitions.CMD_LEFT)) {
                        bracketLevel++;
                    }
                    else if (innerToken.isCommand(CorePackageDefinitions.CMD_RIGHT)) {
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
                    /* Error: We never found a match for \\left so we'll kill the whole expression off */
                    replaceTokens(tokens, i, tokens.size(), createError(token, CoreErrorCode.TFEM04));
                    break LEFT_SEARCH;
                }
                /* Now replace this bracket with a fence */
                FrozenSlice replacementSlice = openBracketToken.getSlice().rightOuterSpan(matchingCloseBracketToken.getSlice());
                EnvironmentToken replacementToken = new EnvironmentToken(replacementSlice,
                        LaTeXMode.MATH,
                        CorePackageDefinitions.ENV_BRACKETED,
                        null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, openBracketToken.getCombinerTarget().getContents(), openBracketToken.getComputedStyle()),
                            ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, matchingCloseBracketToken.getCombinerTarget().getContents(), matchingCloseBracketToken.getComputedStyle())
                        },
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, innerTokens, openBracketToken.getComputedStyle())
                );
                replaceTokens(tokens, i, matchingCloseBracketIndex+1, replacementToken);
                continue LEFT_SEARCH;
            }
        }
    }
    
    /**
     * This attempts to group (not necessarily matching) pairs of open and close brackets into a
     * corresponding {@link CorePackageDefinitions#ENV_BRACKETED} environment that's easier to handle.
     * <p>
     * It also handles brackets which are not correctly nested, using fake empty opener/closer
     * delimiters at the start/end of the group as required.
     * <p>
     * Note that this means that while $[1,2)$ will successfully be matched, the notation
     * $[1,2[$ will *not* be considered matched, even though it is common in subjects like
     * Mathematical Analysis. To get the correct semantics here, you must use
     * <tt>$\left[1,2\right[$ and let {@link #fencePairedParentheses(Token, List)}
     * take care of this for you.
     * 
     * @see #fencePairedParentheses(Token, List)
     */
    private void inferParenthesisFences(Token parentToken, List<FlowToken> tokens) {
        /* The algorithm used here is similar to groupPairedParentheses() */
        FlowToken token;
        LEFT_SEARCH: for (int i=0; i<tokens.size(); i++) { /* (List may change from 'i' onwards during loop) */
            token = tokens.get(i);
            if (!token.hasInterpretationType(InterpretationType.MATH_BRACKET)) {
                continue LEFT_SEARCH;
            }
            MathBracketInterpretation interpretation = (MathBracketInterpretation) token.getInterpretation(InterpretationType.MATH_BRACKET);
            if (!interpretation.isPairingInferencePossible()) {
                /* Too dangerous to try to pair up this type of bracket (e.g. < or |) */
                continue LEFT_SEARCH;
            }
            BracketType bracketType = interpretation.getBracketType();
            if (bracketType==BracketType.CLOSER) {
                /* First thing found is a closer, so make a fence with an empty opener closing at this point */
                FrozenSlice replacementSlice = tokens.get(0).getSlice().rightOuterSpan(token.getSlice());
                List<FlowToken> innerTokens = new ArrayList<FlowToken>(tokens.subList(0, i));
                EnvironmentToken replacementToken = new EnvironmentToken(replacementSlice,
                        LaTeXMode.MATH,
                        CorePackageDefinitions.ENV_BRACKETED,
                        null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createEmptyContainer(parentToken, LaTeXMode.MATH, tokens.get(0).getComputedStyle()),
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, token)
                        },
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, innerTokens, tokens.get(0).getComputedStyle())
                );
                replaceTokens(tokens, 0, i+1, replacementToken);
                i = 0; /* (Rewind back to this new fence) */
                continue LEFT_SEARCH;
            }
            else if (bracketType==BracketType.OPENER_OR_CLOSER) {
                /* Brackets like |...| can't be inferred so ignore but continue */
                continue LEFT_SEARCH;
            }
            /* If we're here, then we found some sort of open bracket. We'll search forward for
             * the matching close, taking care to balance up matching open/close pairs we see on our way.
             */
            List<FlowToken> innerTokens = new ArrayList<FlowToken>();
            FlowToken openBracketToken = token;
            FlowToken matchingCloseBracketToken = null;
            int matchingCloseBracketIndex = -1;
            FlowToken afterToken;
            Stack<MathBracketInterpretation> openerStack = new Stack<MathBracketInterpretation>();
            openerStack.add(interpretation);
            MATCH_SEARCH: for (int j=i+1; j<tokens.size(); j++) { /* 'j' is search index from current point onwards */
                afterToken = tokens.get(j);
                if (afterToken.hasInterpretationType(InterpretationType.MATH_BRACKET)) {
                    MathBracketInterpretation afterInterpretation = (MathBracketInterpretation) afterToken.getInterpretation(InterpretationType.MATH_BRACKET);
                    BracketType afterBracketType = afterInterpretation.getBracketType();
                    switch (afterBracketType) {
                        case OPENER:
                            openerStack.add(afterInterpretation);
                            break;
                            
                        case OPENER_OR_CLOSER:
                            /* Treat this like any other token */
                            break;
                            
                        case CLOSER:
                            /* Pop the last opener. (Note that we no longer check that it matches
                             * the closer we've just found.)
                             */
                            openerStack.pop(); /* (This will always succeed here) */
                            if (openerStack.isEmpty()) {
                                /* Yay! We've found a balance */
                                matchingCloseBracketToken = afterToken;
                                matchingCloseBracketIndex = j;
                                break MATCH_SEARCH;
                            }
                            break;
                    }
                }
                innerTokens.add(afterToken);
            }
            /* Now replace this bracket (if found) or whole expression to the end with a fence */
            EnvironmentToken replacementToken;
            FrozenSlice replacementSlice;
            if (matchingCloseBracketToken!=null) {
                replacementSlice = openBracketToken.getSlice().rightOuterSpan(matchingCloseBracketToken.getSlice());
                replacementToken = new EnvironmentToken(replacementSlice,
                        LaTeXMode.MATH,
                        CorePackageDefinitions.ENV_BRACKETED,
                        null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, openBracketToken),
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, matchingCloseBracketToken)
                        },
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, innerTokens, openBracketToken.getComputedStyle())
                );
                replaceTokens(tokens, i, matchingCloseBracketIndex+1, replacementToken);
            }
            else {
                replacementSlice = openBracketToken.getSlice().rightOuterSpan(tokens.get(tokens.size()-1).getSlice());
                replacementToken = new EnvironmentToken(replacementSlice,
                        LaTeXMode.MATH,
                        CorePackageDefinitions.ENV_BRACKETED,
                        null,
                        new ArgumentContainerToken[] {
                            ArgumentContainerToken.createFromSingleToken(LaTeXMode.MATH, openBracketToken),
                            ArgumentContainerToken.createEmptyContainer(parentToken, LaTeXMode.MATH, openBracketToken.getComputedStyle()),
                        },
                        ArgumentContainerToken.createFromContiguousTokens(parentToken, LaTeXMode.MATH, innerTokens, openBracketToken.getComputedStyle())
                );
                replaceTokens(tokens, i, tokens.size(), replacementToken);
            }
            continue LEFT_SEARCH;
        }
    }
    
    //-----------------------------------------
    // Helpers
    
    private void replaceToken(List<FlowToken> tokens, final int index, FlowToken replacementToken) {
        replacementToken.setComputedStyle(tokens.get(index).getComputedStyle());
        tokens.set(index, replacementToken);
    }
    
    /**
     * Replaces elements in the tokens List with the given replacement {@link Token}, starting
     * at the given startIndex (inclusive) up to endIndex (exclusive).
     * <p>
     * The computed style of the replacement token is set to be that of the first token it
     * replaces.
     */
    private void replaceTokens(List<FlowToken> tokens, final int startIndex, final int endIndex,
            FlowToken replacementToken) {
        replaceToken(tokens, startIndex, replacementToken);
        if (endIndex>startIndex+1) {
            tokens.subList(startIndex+1, endIndex).clear();
        }
    }
    
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
            final BuiltinCommand command, final List<? extends FlowToken> itemBuilder,
            final ComputedStyle computedStyle) {
        ArgumentContainerToken contentToken;
        if (itemBuilder.isEmpty()) {
            contentToken = ArgumentContainerToken.createEmptyContainer(parentToken, parentToken.getLatexMode(), computedStyle);
        }
        else {
            contentToken = ArgumentContainerToken.createFromContiguousTokens(parentToken, itemBuilder.get(0).getLatexMode(), itemBuilder, computedStyle);
        }
        CommandToken result = new CommandToken(contentToken.getSlice(), contentToken.getLatexMode(),
                command,
                null, /* No optional argument */
                new ArgumentContainerToken[] { contentToken } /* Single argument containing content */
        );
        result.setComputedStyle(computedStyle);
        itemBuilder.clear();
        return result;
    }

    private ErrorToken createError(final FlowToken token, final CoreErrorCode errorCode,
            final Object... arguments) throws SnuggleParseException {
        FrozenSlice slice = token.getSlice();
        InputError error = new InputError(errorCode, slice, arguments);
        sessionContext.registerError(error);
        
        ErrorToken result = new ErrorToken(error, token.getLatexMode());
        result.setComputedStyle(token.getComputedStyle());
        return result;
    }
}
