/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.CorePackageDefinitions;
import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
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
 *     Create an instance of this engine and register any extra {@link SnugglePackage}s you have
 *     created or want to use by calling {@link #addPackage(SnugglePackage)}.
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
    
    /** List of all currently registered {@link SnugglePackage}s used by this Engine. */
    private final List<SnugglePackage> packages;

    /** Helper class to manage XSLT Stylesheets */
    private final StylesheetManager stylesheetManager;

    /** Default {@link SessionConfiguration} */
    private SessionConfiguration defaultSessionConfiguration;
    
    /** Default {@link DOMOutputOptions} */
    private DOMOutputOptions defaultDOMOutputOptions;
    
    /** Default {@link XMLOutputOptions} */
    private DOMOutputOptions defaultXMLOutputOptions;

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
        this.packages = new ArrayList<SnugglePackage>();
        this.defaultSessionConfiguration = null; /* (Lazy init) */
        this.defaultDOMOutputOptions = null; /* (Lazy init) */
        this.defaultXMLOutputOptions = null; /* (Lazy init) */
        
        /* Create manager for XSLT stlyesheets using the given cache */
        this.stylesheetManager = new StylesheetManager(stylesheetCache);
        
        /* Add in core package */
        packages.add(CorePackageDefinitions.getPackage());
    }
    
    //-------------------------------------------------
    
    public void addPackage(SnugglePackage snugglePackage) {
        packages.add(snugglePackage);
    }
    
    //-------------------------------------------------
    
    public SnuggleSession createSession() {
        return createSession(getDefaultSessionConfiguration());
    }
    
    public SnuggleSession createSession(SessionConfiguration configuration) {
        ConstraintUtilities.ensureNotNull(configuration, "configuration");
        return new SnuggleSession(this, configuration);
    }

    //-------------------------------------------------
    
    public BuiltinCommand getBuiltinCommandByTeXName(String texName) {
        ConstraintUtilities.ensureNotNull(texName, "texName");
        BuiltinCommand result = null;
        for (SnugglePackage map : packages) {
            result = map.getBuiltinCommandByTeXName(texName);
            if (result!=null) {
                break;
            }
        }
        return result;
    }
    
    public BuiltinEnvironment getBuiltinEnvironmentByTeXName(String texName) {
        ConstraintUtilities.ensureNotNull(texName, "texName");
        BuiltinEnvironment result = null;
        for (SnugglePackage map : packages) {
            result = map.getBuiltinEnvironmentByTeXName(texName);
            if (result!=null) {
                break;
            }
        }
        return result;
    }
    
    //-------------------------------------------------
    
    /**
     * Convenience method to extract the underlying {@link StylesheetManager} used by
     * this engine. Most people won't care about this, but it might be useful if you're
     * trying to integrate stylesheet caching.
     */
    public StylesheetManager getStylesheetManager() {
        return stylesheetManager;
    }
    
    //-------------------------------------------------

    public SessionConfiguration getDefaultSessionConfiguration() {
        if (defaultSessionConfiguration==null) {
            defaultSessionConfiguration = new SessionConfiguration();
        }
        return defaultSessionConfiguration;
    }
    
    public void setDefaultSessionConfiguration(SessionConfiguration defaultSessionConfiguration) {
        ConstraintUtilities.ensureNotNull(defaultSessionConfiguration, "defaultSessionConfiguration");
        this.defaultSessionConfiguration = defaultSessionConfiguration;
    }
    
    
    public DOMOutputOptions getDefaultDOMOutputOptions() {
        if (defaultDOMOutputOptions==null) {
            defaultDOMOutputOptions = new DOMOutputOptions();
        }
        return defaultDOMOutputOptions;
    }

    
    public void setDefaultDOMOutputOptions(DOMOutputOptions defaultDOMOutputOptions) {
        ConstraintUtilities.ensureNotNull(defaultDOMOutputOptions, "defaultDOMOutputOptions");
        this.defaultDOMOutputOptions = defaultDOMOutputOptions;
    }

    
    public DOMOutputOptions getDefaultXMLOutputOptions() {
        if (defaultXMLOutputOptions==null) {
            defaultXMLOutputOptions = new XMLOutputOptions();
        }
        return defaultXMLOutputOptions;
    }

    public void setDefaultXMLOutputOptions(DOMOutputOptions defaultXMLOutputOptions) {
        ConstraintUtilities.ensureNotNull(defaultXMLOutputOptions, "defaultXMLOutputOptions");
        this.defaultXMLOutputOptions = defaultXMLOutputOptions;
    }


    /**
     * @deprecated Use {@link #getDefaultDOMOutputOptions()}
     */
    @Deprecated
    public DOMOutputOptions getDefaultDOMOptions() {
        return defaultDOMOutputOptions;
    }

    /**
     * @deprecated Use {@link #setDefaultDOMOutputOptions(DOMOutputOptions)}
     */
    @Deprecated
    public void setDefaultDOMOptions(DOMOutputOptions defaultDOMOptions) {
        ConstraintUtilities.ensureNotNull(defaultDOMOptions, "defaultDOMOptions");
        this.defaultDOMOutputOptions = defaultDOMOptions;
    }

}
