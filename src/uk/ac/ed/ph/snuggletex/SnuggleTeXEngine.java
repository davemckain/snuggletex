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
public final class SnuggleTeXEngine {
    
    /** List of all currently registered {@link DefinitionMap}s used by this Engine. */
    private final List<DefinitionMap> definitionMaps;
    
    /** Cache of XSLT Stylesheets to use for certain built-in processes */
    private StylesheetCache stylesheetCache;
    
    private SessionConfiguration defaultSessionConfiguration;
    private DOMBuilderOptions defaultDOMBuilderOptions;
  
    public SnuggleTeXEngine() {
        this.definitionMaps = new ArrayList<DefinitionMap>();
        this.defaultSessionConfiguration = new SessionConfiguration();
        this.defaultDOMBuilderOptions = new DOMBuilderOptions();
        this.stylesheetCache = new DefaultStylesheetCache();
        
        /* Add in global definitions */
        definitionMaps.add(GlobalBuiltins.getDefinitionMap());
    }
    
    //-------------------------------------------------
    
    public void registerDefinitions(DefinitionMap definitionMap) {
        definitionMaps.add(definitionMap);
    }
    
    //-------------------------------------------------
    
    public SnuggleTeXSession createSession() {
        return createSession(defaultSessionConfiguration);
    }
    
    public SnuggleTeXSession createSession(SessionConfiguration configuration) {
        ConstraintUtilities.ensureNotNull(configuration, "configuration");
        return new SnuggleTeXSession(this, configuration);
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

    
    public DOMBuilderOptions getDefaultDOMBuilderOptions() {
        return defaultDOMBuilderOptions;
    }
    
    public void setDefaultDOMBuilderOptions(DOMBuilderOptions defaultDOMBuilderOptions) {
        ConstraintUtilities.ensureNotNull(stylesheetCache, "defaultDOMBuilderOptions");
        this.defaultDOMBuilderOptions = defaultDOMBuilderOptions;
    }
    
    
    public StylesheetCache getStylesheetCache() {
        return stylesheetCache;
    }
    
    public void setStylesheetCache(StylesheetCache stylesheetCache) {
        ConstraintUtilities.ensureNotNull(stylesheetCache, "stylesheetCache");
        this.stylesheetCache = stylesheetCache;
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
