/* $Id: SnuggleTeXEngine.java,v 1.2 2008/04/18 09:44:05 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.DefinitionMap;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main entry point into SnuggleTeX.
 * 
 * <h2>Usage</h2>
 * 
 * <ul>
 *   <li>
 *     Create an instance of this engine and register any custom command/environment definitions
 *     you may want to support using {@link #registerDefinitions(DefinitionMap)}.
 *   </li>
 *   <li>
 *     Use {@link #createSession()} to create a "session" that will take one (or more) input
 *     documents and produce a DOM.
 *   </li>
 *   <li>
 *     Once configured, an instance of this class can be shared by multiple Threads.
 *   </li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision: 1.2 $
 */
public final class SnuggleTeXEngine {
    
    private final List<DefinitionMap> definitionMaps;
  
    public SnuggleTeXEngine() {
        this.definitionMaps = new ArrayList<DefinitionMap>();
        
        /* Add in global definitions */
        definitionMaps.add(GlobalBuiltins.getDefinitionMap());
    }
    
    public void registerDefinitions(DefinitionMap definitionMap) {
        definitionMaps.add(definitionMap);
    }
    
    public SnuggleTeXSession createSession() {
        return createSession(null);
    }
    
    public SnuggleTeXSession createSession(SnuggleTeXConfiguration configuration) {
        return new SnuggleTeXSession(this, configuration);
    }
    
    public BuiltinCommand getCommandByTeXName(String texName) {
        BuiltinCommand result = null;
        for (DefinitionMap map : definitionMaps) {
            result = map.getCommandByTeXName(texName);
            if (result!=null) {
                break;
            }
        }
        return result;
    }
    
    public BuiltinEnvironment getEnvironmentByTeXName(String texName) {
        BuiltinEnvironment result = null;
        for (DefinitionMap map : definitionMaps) {
            result = map.getEnvironmentByTeXName(texName);
            if (result!=null) {
                break;
            }
        }
        return result;
    }
    
    
}
