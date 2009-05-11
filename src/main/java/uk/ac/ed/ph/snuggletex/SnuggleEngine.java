/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.DefinitionMap;
import uk.ac.ed.ph.snuggletex.definitions.GlobalBuiltins;
import uk.ac.ed.ph.snuggletex.utilities.SimpleStylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

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

    /**
     * Creates a new {@link SnuggleEngine} using a very simple cache for any
     * XSLT stylesheets required that simply stores them internally for the lifetime
     * of the engine. This will be fine in most cases. If you want more control over
     * this, consider the alternative constructor.
     */
    public SnuggleEngine() {
        this(new SimpleStylesheetCache());
    }

    /**
     * Creates a new {@link SnuggleEngine} using the given {@link StylesheetCache}
     * for managing stylesheets. Use this if you want to integrate XSLT caching
     * with your own code or want moe control over how things get cached.
     */
    public SnuggleEngine(StylesheetCache stylesheetCache) {
        this.definitionMaps = new ArrayList<DefinitionMap>();
        this.defaultSessionConfiguration = new SessionConfiguration();
        this.defaultDOMOptions = new DOMOutputOptions();
        
        /* Create manager for XSLT stlyesheets using the given cache */
        this.stylesheetManager = new StylesheetManager(stylesheetCache);
        
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
}
