/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.demowebapp;

import uk.ac.ed.ph.aardvark.commons.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.Snapshot;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleTeXEngine;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;
import uk.ac.ed.ph.snuggletex.WebPageBuilderOptions;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageBuilderOptions.WebPageType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

/**
 * Trivial servlet used to serve up the sample SnuggleTeX pages within the documentation
 * webapp.
 * <p>
 * This handles URLs of the form: <tt>/contextPath/content/pageName/file.ext</tt>
 * <p>
 * The <tt>pageName</tt> is mapped to a file <tt>pageName.tex</tt> under {@link #MACROS_RESOURCE_PATH}
 * and the <tt>type.ext</tt> is used to determine which type of web page to produce.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleTeXServlet extends BaseServlet {
    
    private static final long serialVersionUID = 7013613625143346274L;
    
    /** Directory in which SnuggleTeX pages will be loaded from (relative to webapp)*/
    public static final String CONTENT_BASE_DIRECTORY =  "/WEB-INF/content/";
    
    /** Location of the global LaTeX macros file (relative to webapp) */
    public static final String MACROS_RESOURCE_PATH = "/WEB-INF/macros.tex";
    
    /** Location of the list of content pages (which maps each name to its title) */
    public static final String PAGES_RESOURCE_PATH = "/WEB-INF/pages.properties";
    
    /** Location of XSLT applied to resulting page to add in headers and footers */
    public static final String WEBPAGE_XSLT_RESOURCE_PATH = "/WEB-INF/webpage.xsl";
    
    /** 
     * The XSLT {@link Templates} Object created from {@link #WEBPAGE_XSLT_RESOURCE_PATH}
     * will be saved into the {@link ServletContext} as an attribute having this name.
     */
    public static final String WEBPAGE_XSLT_ATTRIBUTE_NAME = "webpageXslt";
    
    /**
     * Special <tt>file.ext</tt> used to request the original LaTeX source of a page, rather
     * than a finished web page.
     */
    public static final String SOURCE_TYPE_EXT = "source.txt";
    
    /** Maps <tt>file.ext</tt> to a {@link WebPageType} (set up in {@link #init()}) */
    private Map<String, WebPageType> webPageTypeMap = new HashMap<String, WebPageType>();
    
    /** "Map" of all supported content pages (name -> title) */
    private Properties pagesProperties;
    
    private Snapshot snapshot;
    private Templates webPageTemplates;
    
    /**
     * Initialises certain things that won't change over the lifetime of the web application, such
     * as the XSLT for generating web pages.
     * <p>
     * This also creates a SnuggleTeX {@link Snapshot} Object from the {@link #MACROS_RESOURCE_PATH}
     * file, which is a useful example of how this can be used.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        
        /* Make up list of "filenames" to match */
        webPageTypeMap = new HashMap<String, WebPageType>();
        webPageTypeMap.put("default.xml", WebPageType.DEFAULT);
        webPageTypeMap.put("crossbrowser.xml", WebPageType.CROSS_BROWSER_XHTML);
        webPageTypeMap.put("mathplayer.html", WebPageType.MATHPLAYER_HTML);
        
        /* Read in list of "content pages" */
        InputStream propertiesStream = ensureReadResource(PAGES_RESOURCE_PATH);
        pagesProperties = new Properties();
        try {
            pagesProperties.load(propertiesStream);
        }
        catch (IOException e) {
            throw new ServletException("IOException occurred when reading in " + PAGES_RESOURCE_PATH, e);
        }

        /* Parse common macros and create a snapshot to reuse on each request */
        snapshot = createPostMacrosSnapshot();
        
        /* Read in the XSLT stylesheet that will be used to make the resulting web page pretty.
         * We also save this into the ServletContext as the Static XSLT Servlet will use it as well.
         */
        webPageTemplates = compileStylesheet(WEBPAGE_XSLT_RESOURCE_PATH);
        getServletContext().setAttribute(WEBPAGE_XSLT_ATTRIBUTE_NAME, webPageTemplates);
    }
    
    /**
     * Uses SnuggleTeX to parse the {@link #MACROS_RESOURCE_PATH} and create a {@link Snapshot}
     * of the results. This can be reused for every web request, which saves having to re-parse
     * the macros each time.
     * 
     * @throws ServletException
     */
    private Snapshot createPostMacrosSnapshot() throws ServletException {
        /* Read in common macros */
        InputStream macrosStream = ensureReadResource(MACROS_RESOURCE_PATH);
        
        /* Create engine, read in macros and then create a snapshot to reuse for each request */
        SessionConfiguration configuration = new SessionConfiguration();
        configuration.setInferringMathStructure(true);
        
        SnuggleTeXEngine engine = new SnuggleTeXEngine();
        SnuggleTeXSession session = engine.createSession(configuration);
        try {
            session.parseInput(new SnuggleInput(macrosStream));
        }
        catch (IOException e) {
            throw new ServletException("IOException whilst reading in macros file at " + MACROS_RESOURCE_PATH, e);
        }
        return session.createSnapshot();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); /* This is "/pageName/type.ext" */
        String[] bits = pathInfo.substring(1).split("/", 2); /* Should give { "pathName", "type.ext" } */
        if (bits.length!=2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "PathInfo not of required form");
            return;
        }
        String pageName = bits[0];
        String typeExt = bits[1];
        
        /* Check that the page requested is listed */
        String pageTitle = pagesProperties.getProperty(pageName);
        if (pageTitle==null) {
            getServletContext().log("Page " + pageName + " is not listed in " + PAGES_RESOURCE_PATH);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        /* Work out where the source TeX is coming from and read it in, if found */
        String contentResourceName = CONTENT_BASE_DIRECTORY + pageName + ".tex";
        InputStream contentStream = getServletContext().getResourceAsStream(contentResourceName);
        if (contentStream==null) {
            getServletContext().log("Could not read in content stream " + contentResourceName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        /* Handle request for page source now, if appropriate */
        if (typeExt.equals(SOURCE_TYPE_EXT)) {
            response.setContentType("text/plain");
            IOUtilities.transfer(contentStream, response.getOutputStream());
            return;
        }

        /* Decide what type of page we're making */
        WebPageType pageType = webPageTypeMap.get(typeExt);
        if (pageType==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not deduce type of resulting page");
            return;
        }
        
        /* Parse the TeX */
        SnuggleTeXSession session = snapshot.createSession();
        
        SnuggleInput input = new SnuggleInput(contentStream, contentResourceName);
        session.parseInput(input);
        
        /* Create stylesheet to format the resulting web page */
        Transformer stylesheet;
        try {
            stylesheet = webPageTemplates.newTransformer();
            stylesheet.setParameter("context-path", request.getContextPath());
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create stylesheet from Templates", e);
        }
        
        /* Generate and serve the resulting web page */
        WebPageBuilderOptions options = new WebPageBuilderOptions();
        options.setAddingMathAnnotations(true);
        options.setPageType(pageType);
        options.setStylesheet(stylesheet);
        options.setErrorOptions(ErrorOutputOptions.XHTML);
        options.setTitle(pageTitle);
        options.setAddingTitleHeading(true);
        options.setIndenting(true);
        options.setCSSStylesheetURLs(
                request.getContextPath() + "/includes/physics.css",
                request.getContextPath() + "/includes/snuggletex.css"
        );
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
}
