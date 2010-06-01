/* $Id$
 *
 * Copyright (c) 2010, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.internal;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.MathCharacter;
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
    
    MathCharacter getMathCharacter(int codePoint);

    BuiltinCommand getBuiltinCommandByTeXName(String texName);
    
    BuiltinEnvironment getBuiltinEnvironmentByTeXName(String texName);
    
    Map<String, UserDefinedCommand> getUserCommandMap();
    
    Map<String, UserDefinedEnvironment> getUserEnvironmentMap();
    
    StylesheetManager getStylesheetManager();
    
    void registerError(InputError error) throws SnuggleParseException;
}
