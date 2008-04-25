/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.util.List;
import java.util.Map;

import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

/**
 * A partial result of parsing one or more {@link SnuggleInput}s by a {@link SnuggleTeXSession},
 * created via {@link SnuggleTeXSession#createSnapshot()}.
 * <p>
 * By calling {@link #createSession()}, a new {@link SnuggleTeXSession} with exactly the same state
 * as this snapshot is created, which can then be used further.
 * <p>
 * Clients might want to use this if they always need to read in some local/configuration {@link SnuggleInput}s
 * as it allows the results of these configurations to be reused without requiring re-parsing.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class Snapshot {
	
	final SnuggleTeXEngine engine;
	
    /** Configuration for this session */
	final SnuggleTeXConfiguration configuration;
    
    /** Errors accumulated during this session */
	final List<InputError> errors;
    
    /** Map of user-defined commands, keyed on name */
	final Map<String, UserDefinedCommand> userCommandMap;
    
    /** Map of user-defined environments, keyed on name */
	final Map<String, UserDefinedEnvironment> userEnvironmentMap;
    
	final List<FlowToken> parsedTokens;
    
    Snapshot(final SnuggleTeXEngine engine, final SnuggleTeXConfiguration configuration,
    		final List<InputError> errors, final Map<String, UserDefinedCommand> userCommandMap,
    		final Map<String, UserDefinedEnvironment> userEnvironmentMap,
    		final List<FlowToken> parsedTokens) {
		this.engine = engine;
		this.configuration = configuration;
		this.errors = errors;
		this.userCommandMap = userCommandMap;
		this.userEnvironmentMap = userEnvironmentMap;
		this.parsedTokens = parsedTokens;
	}

    /**
     * Creates a new (and open) {@link SnuggleTeXSession} with exactly the same state as the
     * {@link SnuggleTeXSession} that created this {@link Snapshot}.
     */
	public SnuggleTeXSession createSession() {
    	return new SnuggleTeXSession(this);
    }
}
