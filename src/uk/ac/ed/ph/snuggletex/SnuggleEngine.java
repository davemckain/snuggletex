/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.aardvark.commons.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.DefinitionMap;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;

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
 *     Once configured, an instance of this Class can be shared by multiple Threads.
 *   </li>
 *   <li>
 *     Don't let the usual connotations associated with the name of this Class worry you that
 *     instantiating it is going to be expensive!
 *   </li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleEngine {
    
    /** List of all currently registered {@link DefinitionMap}s used by this Engine. */
    private final List<DefinitionMap> definitionMaps;

    /** Helper class to manage XSLT Stylesheets */
    private final StylesheetManager stylesheetManager;

    /** Default {@link SessionConfiguration} */
    private SessionConfiguration defaultSessionConfiguration;
    
    /** Default {@link DOMOutputOptions} */
    private DOMOutputOptions defaultDOMOptions;
  
    public SnuggleEngine() {
        this.definitionMaps = new ArrayList<DefinitionMap>();
        this.defaultSessionConfiguration = new SessionConfiguration();
        this.defaultDOMOptions = new DOMOutputOptions();
        this.stylesheetManager = new StylesheetManager(new DefaultStylesheetCache());
        
        /* Add in global definitions */
        definitionMaps.add(GlobalBuiltins.getDefinitionMap());
    }
    
    //-------------------------------------------------
    
    public void registerDefinitions(DefinitionMap definitionMap) {
        definitionMaps.add(definitionMap);
    }
    
    //-------------------------------------------------
    
    public SnuggleSession createSession() {
        return createSession(defaultSessionConfiguration);
    }
    
    public SnuggleSession createSession(SessionConfiguration configuration) {
        ConstraintUtilities.ensureNotNull(configuration, "configuration");
        return new SnuggleSession(this, configuration);
    }

    //-------------------------------------------------
    
    public BuiltinCommand getCommandByTeXName(String texName) {
        ConstraintUtilities.ensureNotNull(texName, "texName");
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
        ConstraintUtilities.ensureNotNull(texName, "texName");
        BuiltinEnvironment result = null;
        for (DefinitionMap map : definitionMaps) {
            result = map.getEnvironmentByTeXName(texName);
            if (result!=null) {
                break;
            }
        }
        return result;
    }
    
    //-------------------------------------------------

    public SessionConfiguration getDefaultSessionConfiguration() {
        return defaultSessionConfiguration;
    }
    
    public void setDefaultSessionConfiguration(SessionConfiguration defaultSessionConfiguration) {
        ConstraintUtilities.ensureNotNull(defaultSessionConfiguration, "defaultSessionConfiguration");
        this.defaultSessionConfiguration = defaultSessionConfiguration;
    }

    
    public DOMOutputOptions getDefaultDOMOptions() {
        return defaultDOMOptions;
    }
    
    public void setDefaultDOMOptions(DOMOutputOptions defaultDOMOptions) {
        ConstraintUtilities.ensureNotNull(defaultDOMOptions, "defaultDOMOptions");
        this.defaultDOMOptions = defaultDOMOptions;
    }
    
    public StylesheetManager getStylesheetManager() {
        return stylesheetManager;
    }
    
    /**
     * Default implementation of {@link StylesheetCache} that simply caches all stylesheets
     * for the lifetime of the engine. This is reasonable since we currently don't have many
     * of these. In future, this behaviour may change.
     */
    public static class DefaultStylesheetCache implements StylesheetCache {
        
        private final Map<String, Templates> cacheMap;
        
        public DefaultStylesheetCache() {
            this.cacheMap = new HashMap<String, Templates>();
        }
        
        public Templates getStylesheet(String resourceName) {
            return cacheMap.get(resourceName);
        }
        
        public void putStylesheet(String resourceName, Templates stylesheet) {
            cacheMap.put(resourceName, stylesheet);
        }
    }
}
