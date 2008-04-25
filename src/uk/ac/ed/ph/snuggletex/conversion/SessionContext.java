/* $Id: SessionContext.java,v 1.4 2008/04/18 09:44:05 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleTeXConfiguration;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;

import java.util.List;
import java.util.Map;

/**
 * Provides access to session-related Objects used during the various parts of the snuggle process.
 *
 * @author  David McKain
 * @version $Revision: 1.4 $
 */
public interface SessionContext {

    SnuggleTeXConfiguration getConfiguration();

    List<InputError> getErrors();
    
    Map<String, UserDefinedCommand> getUserCommandMap();
    
    Map<String, UserDefinedEnvironment> getUserEnvironmentMap();
    
    BuiltinCommand getCommandByTeXName(String texName);
    
    BuiltinEnvironment getEnvironmentByTeXName(String texName);

    void registerError(InputError error) throws SnuggleParseException;
}
