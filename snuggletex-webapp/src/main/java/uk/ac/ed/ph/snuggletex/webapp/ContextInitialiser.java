/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.DoNothingStylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.SimpleStylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.transform.TransformerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fairly typical {@link ServletContextListener} that just sets up a few shared resources
 * and sticks them in the {@link ServletContext} for access by servlets.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ContextInitialiser implements ServletContextListener {
    
    private static final Logger log = LoggerFactory.getLogger(ContextInitialiser.class);
    
    public static final String CACHE_XSLT_PROPERTY_NAME = "caching.xslt";
    
    public static final String STYLESHEET_MANAGER_ATTRIBUTE_NAME = "stylesheetManager";
    public static final String STYLESHEET_CACHE_ATTRIBUTE_NAME = "stylesheetCache";
    public static final String TRANSFORMER_FACTORY_THREAD_LOCAL = "transformerFactoryThreadLocal";
    
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        
        /* Create appropriate implementation of StylesheetCache, as specified by init param */
        boolean cacheXSLT = "true".equals(servletContext.getInitParameter(CACHE_XSLT_PROPERTY_NAME));
        StylesheetCache stylesheetCache = cacheXSLT ? new SimpleStylesheetCache() : new DoNothingStylesheetCache();
        log.info("Created new StylesheetCache of type " + stylesheetCache.getClass());
        
        /* Create various other shared resources */
        servletContext.setAttribute(STYLESHEET_CACHE_ATTRIBUTE_NAME, stylesheetCache);
        servletContext.setAttribute(STYLESHEET_MANAGER_ATTRIBUTE_NAME, new StylesheetManager(stylesheetCache));
        servletContext.setAttribute(TRANSFORMER_FACTORY_THREAD_LOCAL, new ThreadLocal<TransformerFactory>() {
            @Override
            protected TransformerFactory initialValue() {
                return XMLUtilities.createSaxonTransformerFactory();
            }
        });
        log.info("Context initialised");
    }
    
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.info("Context destroyed");
    }

}