/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.util.List;
import java.util.Map;

/**
 * Provides access to session-related Objects used during the various parts of the snuggle process.
 *
 * @author  David McKain
 * @version $Revision$
 */
public interface SessionContext {

    SessionConfiguration getConfiguration();

    List<InputError> getErrors();
    
    Map<String, UserDefinedCommand> getUserCommandMap();
    
    Map<String, UserDefinedEnvironment> getUserEnvironmentMap();
    
    BuiltinCommand getCommandByTeXName(String texName);
    
    BuiltinEnvironment getEnvironmentByTeXName(String texName);
    
    StylesheetManager getStylesheetManager();
    
    void registerError(InputError error) throws SnuggleParseException;
}
