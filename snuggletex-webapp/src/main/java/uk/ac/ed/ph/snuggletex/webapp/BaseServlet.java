/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.SerializationOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.utilities.ClassPathURIResolver;
import uk.ac.ed.ph.snuggletex.utilities.StandaloneSerializationOptions;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Trivial base class for servlets in the demo webapp
 *
 * @author  David McKain
 * @version $Revision$
 */
abstract class BaseServlet extends HttpServlet {
    
    private static final long serialVersionUID = -2577813908466694931L;
    
    public String ensureGetContextInitParam(String propertyName) throws ServletException {
        String result = getServletContext().getInitParameter(propertyName);
        if (result==null) {
            throw new ServletException("Context init-param " + propertyName + " is not set");
        }
        return result;
    }

    /**
     * Helper that reads in a resource from the webapp hierarchy, throwing a {@link ServletException}
     * if the resource could not be found.
     * 
     * @param resourcePathInsideWebpp path of Resource to load, relative to base of webapp.
     * @return resulting {@link InputStream}, which will not be null
     * @throws ServletException
     */
    protected InputStream ensureReadResource(String resourcePathInsideWebpp) throws ServletException {
        InputStream result = getServletContext().getResourceAsStream(resourcePathInsideWebpp);
        if (result==null) {
            throw new ServletException("Could not read in required web resource at " + resourcePathInsideWebpp);
        }
        return result;
    }
    
    protected StylesheetManager getStylesheetManager() {
        return (StylesheetManager) getServletContext().getAttribute(ContextInitialiser.STYLESHEET_MANAGER_ATTRIBUTE_NAME);
    }
    
    protected StylesheetCache getStylesheetCache() {
        return getStylesheetManager().getStylesheetCache();
    }
    
    protected TransformerFactory getTransformerFactory() {
        return getStylesheetManager().getTransformerFactory(true);
    }
    
    protected SnuggleEngine createSnuggleEngine() {
        return new SnuggleEngine(getStylesheetManager());
    }
    
    /**
     * Compiles the XSLT stylesheet at the given location within the webapp,
     * using {@link ClassPathURIResolver} to locate the stylesheet and anything it wants to import.
     * <p>
     * It also sets some core parameters based on certain properties set for the webapp.
     * 
     * @param request Request being processed (so we can pass the context path to the XSLT)
     * @param classPathUri location of XSLT to compile.
     * 
     * @return resulting {@link Templates} representing the compiled stylesheet.
     * @throws ServletException if XSLT could not be found or could not be compiled.
     */
    protected Transformer getStylesheet(HttpServletRequest request, String classPathUri) throws ServletException {
        Transformer result;
        try {
            result = getStylesheetManager().getStylesheet(classPathUri).newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create Transformer from Templates", e);
        }
        result.setParameter("context-path", request.getContextPath());
        result.setParameter("snuggletex-version", ensureGetContextInitParam(ContextInitialiser.SNUGGLETEX_VERSION_PROPERTY_NAME));
        result.setParameter("maven-site-url", ensureGetContextInitParam(ContextInitialiser.MAVEN_SITE_URL_PROPERTY_NAME));
        return result;
    }
    
    protected SerializationOptions createMathMLSourceSerializationOptions() {
        SerializationOptions result = new StandaloneSerializationOptions();
        result.setIndenting(true);
        result.setUsingNamedEntities(true);
        return result;
    }

    /**
     * Convenience method which picks the most appropriate MathML-based {@link WebPageType}
     * for the current UserAgent, returning null if the UserAgent does not appear to support
     * MathML.
     * 
     * @param request
     */
    protected WebPageType chooseBestWebPageType(final HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        WebPageType result = null;
        if (userAgent!=null) {
            if (userAgent.contains("MathPlayer ")) {
                result  = WebPageType.MATHPLAYER_HTML;
            }
            else if (userAgent.contains("Gecko/")) {
                result  = WebPageType.MOZILLA;
            }
        }
        return result;
    }
    
    protected boolean isMathMLCapable(final HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent!=null && (userAgent.contains("MathPlayer ") || userAgent.contains("Gecko/"));
    }
    
    protected boolean isInternetExplorer(final HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent!=null && userAgent.contains("MSIE");
    }
}