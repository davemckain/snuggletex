/* $Id:SnuggleSnapshot.java 179 2008-08-01 13:41:24Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import java.util.List;
import java.util.Map;

import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

/**
 * A partial result of parsing one or more {@link SnuggleInput}s by a {@link SnuggleSession},
 * created via {@link SnuggleSession#createSnapshot()}.
 * <p>
 * By calling {@link #createSession()}, a new {@link SnuggleSession} with exactly the same state
 * as this snapshot is created, which can then be used further.
 * <p>
 * Clients might want to use this if they always need to read in some local/configuration {@link SnuggleInput}s
 * as it allows the results of these configurations to be reused without requiring re-parsing.
 *
 * @author  David McKain
 * @version $Revision:179 $
 */
public final class SnuggleSnapshot {
    
    final SnuggleEngine engine;
    
    final SnugglePackage[] packages;
    
    /** Configuration for the owning session */
    final SessionConfiguration configuration;
    
    final DOMOutputOptions defaultDOMOutputOptions;
    
    final XMLStringOutputOptions defaultXMLStringOutputOptions;
    
    /** Errors accumulated during the owning session */
    final List<InputError> errors;
    
    /** Map of user-defined commands, keyed on name */
    final Map<String, UserDefinedCommand> userCommandMap;
    
    /** Map of user-defined environments, keyed on name */
    final Map<String, UserDefinedEnvironment> userEnvironmentMap;
    
    /** Parsing state for the owning session */
    final List<FlowToken> parsedTokens;
    
    SnuggleSnapshot(final SnuggleEngine engine, final SnugglePackage[] packages,
            final SessionConfiguration configuration,
            final DOMOutputOptions defaultDOMOutputOptions,
            final XMLStringOutputOptions defaultXMLStringOutputOptions,
            final List<InputError> errors, final Map<String, UserDefinedCommand> userCommandMap,
            final Map<String, UserDefinedEnvironment> userEnvironmentMap,
            final List<FlowToken> parsedTokens) {
        this.engine = engine;
        this.packages = packages;
        this.configuration = configuration;
        this.defaultDOMOutputOptions = defaultDOMOutputOptions;
        this.defaultXMLStringOutputOptions = defaultXMLStringOutputOptions;
        this.errors = errors;
        this.userCommandMap = userCommandMap;
        this.userEnvironmentMap = userEnvironmentMap;
        this.parsedTokens = parsedTokens;
    }

    /**
     * Creates a new {@link SnuggleSession} with exactly the same state as the
     * {@link SnuggleSession} that created this {@link SnuggleSnapshot}.
     */
    public SnuggleSession createSession() {
        return new SnuggleSession(this);
    }
}
