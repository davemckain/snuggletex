/* $Id: SnuggleTeXSession.java,v 1.5 2008/04/22 16:07:28 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.SnuggleTeXConfiguration.ErrorOptions;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.LaTeXTokeniser;
import uk.ac.ed.ph.snuggletex.conversion.SessionContext;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.conversion.TokenFixer;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * This represents a single "job" for SnuggleTeX.
 * 
 * <h2>Usage</h2>
 * 
 * <ul>
 *   <li>
 *     Create a session with {@link SnuggleTeXEngine#createSession()}, optionally passing
 *     configuration details for this session.
 *   </li>
 *   <li>
 *     Call {@link #parseInput(SnuggleInput)} on one or more input documents to tokenise and
 *     fix-up the LaTeX contained therein.
 *   </li>
 *   <li>
 *     Call {@link #buildDOM(Element)} to convert the resulting tokens into a DOM sub-tree.
 *   </li>
 *   <li>
 *     Call {@link #getErrors()} to get at the errors that have arisen during this job.
 *   </li>
 *   <li>
 *     Discard!
 *   </li>
 *   <li>
 *     An instance of this class should only be used by one Thread at a time. It is stateful and
 *     intended to be discarded after use.
 *   </li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision: 1.5 $
 */
public final class SnuggleTeXSession implements SessionContext {
    
    private final SnuggleTeXEngine engine;
    private final LaTeXTokeniser tokeniser;
    private final TokenFixer tokenFixer;
    
    //-------------------------------------------------
    // Stateful stuff
    
    /** Configuration for this session */
    private final SnuggleTeXConfiguration configuration;
    
    /** Errors accumulated during this session */
    private final List<InputError> errors;
    
    /** Map of user-defined commands, keyed on name */
    private final Map<String, UserDefinedCommand> userCommandMap;
    
    /** Map of user-defined environments, keyed on name */
    private final Map<String, UserDefinedEnvironment> userEnvironmentMap;
    
    private final List<FlowToken> parsedTokens; 
    private boolean finished;
    
    SnuggleTeXSession(final SnuggleTeXEngine engine, final SnuggleTeXConfiguration configuration) {
        this.engine = engine;
        
        /* We'll clone the supplied configuration, if supplied, so that
         * any run-time changes made to it do not affect the caller's version
         * of the configuration.
         */
        this.configuration = configuration!=null ? (SnuggleTeXConfiguration) configuration.clone() : new SnuggleTeXConfiguration();

        /* Set up main worker Objects */
        this.tokeniser = new LaTeXTokeniser(this);
        this.tokenFixer = new TokenFixer(this);
        
        /* Initialise session state */
        this.errors = new ArrayList<InputError>();
        this.userCommandMap = new HashMap<String, UserDefinedCommand>();
        this.userEnvironmentMap = new HashMap<String, UserDefinedEnvironment>();
        this.parsedTokens = new ArrayList<FlowToken>();
        this.finished = false;
    }
    
    SnuggleTeXSession(final Snapshot template) {
        /* Set up main worker Objects */
        this.tokeniser = new LaTeXTokeniser(this);
        this.tokenFixer = new TokenFixer(this);
        
        /* Copy stuff from the template */
    	this.engine = template.engine;
    	this.configuration = (SnuggleTeXConfiguration) template.configuration.clone();
    	this.errors = new ArrayList<InputError>(template.errors);
    	this.userCommandMap = new HashMap<String, UserDefinedCommand>(template.userCommandMap);
    	this.userEnvironmentMap = new HashMap<String, UserDefinedEnvironment>(template.userEnvironmentMap);
    	this.parsedTokens = new ArrayList<FlowToken>(template.parsedTokens);
    	
    	this.finished = false;
    }
    
    //-------------------------------------------------
    
    public SnuggleTeXConfiguration getConfiguration() {
        return configuration;
    }

    public List<InputError> getErrors() {
        return errors;
    }
    
    public List<FlowToken> getParsedTokens() {
        return parsedTokens;
    }
    
    //---------------------------------------------
    // Public "business" methods
    
    public boolean parseInput(SnuggleInput input) throws IOException {
        ensureSessionOpen();
        
        /* Perform tokenisation, then fix up and store the results */
        boolean indicateFailure = false;
        try {
            SnuggleInputReader reader = new SnuggleInputReader(this, input);
            ArgumentContainerToken result = tokeniser.tokenise(reader);
            tokenFixer.fixTokenTree(result);
            parsedTokens.addAll(result.getContents());
        }
        catch (SnuggleParseException e) {
            indicateFailure = true;
        }
        return indicateFailure;
    }
    
    /**
     * Creates a {@link Snapshot} Object holding the current state of this session that can be later
     * used to recreate a session having exactly the same state.
     * <p>
     * This may only be called whilst the session is open.
     * 
     * @throws IllegalStateException if the session has been closed.
     */
    public Snapshot createSnapshot() {
    	ensureSessionOpen();
    	return new Snapshot(engine,
    			(SnuggleTeXConfiguration) configuration.clone(),
    			new ArrayList<InputError>(errors), 
    			new HashMap<String, UserDefinedCommand>(userCommandMap),
    			new HashMap<String, UserDefinedEnvironment>(userEnvironmentMap),
    			new ArrayList<FlowToken>(parsedTokens));
    }
    
    public boolean buildDOM(Element targetRoot) {
        ensureSessionOpen();
        
        boolean indicateFailure = false;
        try {
            DOMBuilder domBuilder = new DOMBuilder(targetRoot.getOwnerDocument(), this);
            domBuilder.handleTokens(targetRoot, parsedTokens, true);
        }
        catch (SnuggleParseException e) {
            indicateFailure = true;
        }
        finally {
        	/* Mark session as closed */
        	finished = true;
        }
        return indicateFailure;
    }
    
    private void ensureSessionOpen() {
        if (finished) {
            throw new IllegalStateException("Session is finished");
        }
    }
    
    //---------------------------------------------
    // Business helpers
    
    public BuiltinCommand getCommandByTeXName(String texName) {
        return engine.getCommandByTeXName(texName);
    }
    
    public BuiltinEnvironment getEnvironmentByTeXName(String texName) {
        return engine.getEnvironmentByTeXName(texName);
    }
    
    public Map<String, UserDefinedCommand> getUserCommandMap() {
        return userCommandMap;
    }
    
    public Map<String, UserDefinedEnvironment> getUserEnvironmentMap() {
        return userEnvironmentMap;
    }

    /**
     * Records a new error, throwing a {@link SnuggleParseException} if
     * the current {@link SnuggleTeXConfiguration} deems that we should
     * fail on this error.
     * 
     * @param error
     * @throws SnuggleParseException 
     */
    public void registerError(InputError error) throws SnuggleParseException {
        errors.add(error);
        if (configuration.getErrorOptions()==ErrorOptions.FAIL_FAST) {
            throw new SnuggleParseException(error);
        }
    }
}
