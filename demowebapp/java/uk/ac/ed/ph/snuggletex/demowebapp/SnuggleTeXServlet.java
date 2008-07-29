/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.demowebapp;

import uk.ac.ed.ph.aardvark.commons.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.MathMLWebPageBuilderOptions;
import uk.ac.ed.ph.snuggletex.MessageFormatter;
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.Snapshot;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleTeXEngine;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageBuilderOptions.WebPageType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleTeXServlet extends BaseServlet {
    
    private static final long serialVersionUID = 7013613625143346274L;
    
    /** Directory in which SnuggleTeX pages will be loaded from (relative to webapp)*/
    public static final String CONTENT_BASE_DIRECTORY = "/WEB-INF/docs";
    
    /** Location of the global LaTeX macros file (relative to webapp) */
    public static final String MACROS_RESOURCE_PATH = "/WEB-INF/macros.tex";
    
    /** Location of XSLT applied to resulting page to add in headers and footers */
    public static final String WEBPAGE_XSLT_RESOURCE_PATH = "/WEB-INF/format-output.xsl";
    
    /** 
     * The XSLT {@link Templates} Object created from {@link #WEBPAGE_XSLT_RESOURCE_PATH}
     * will be saved into the {@link ServletContext} as an attribute having this name.
     */
    public static final String WEBPAGE_XSLT_ATTRIBUTE_NAME = "webpageXslt";
    
    private Snapshot snapshot;
    
    /** Flag to denote whether to cache XSLT or not (set via <init-param/>) */
    boolean cachingXSLT;
    
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
        
        /* Read in <init-params/> */
        ServletContext servletContext = getServletContext();
        cachingXSLT = Boolean.valueOf(servletContext.getInitParameter("cachingXSLT"));
        
        /* Parse common macros and create a snapshot to reuse on each request */
        snapshot = createPostMacrosSnapshot();

        /* If caching, compile XSLT now */
        if (cachingXSLT) {
            getStylesheet(WEBPAGE_XSLT_ATTRIBUTE_NAME, WEBPAGE_XSLT_RESOURCE_PATH);
        }
    }
    
    private Templates getStylesheet(final String attributeName, final String resourcePath)
            throws ServletException {
        Templates result = null;
        if (cachingXSLT) {
            result = (Templates) getServletContext().getAttribute(attributeName);
        }
        if (result==null) {
            result = compileStylesheet(resourcePath);
            if (cachingXSLT) {
                getServletContext().setAttribute(attributeName, result);
            }
        }
        return result;
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
            throw new ServletException("IOException whilst reading in macros file at "
                    + MACROS_RESOURCE_PATH, e);
        }
        return session.createSnapshot();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); /* This is "/path/page.ext" */
        int dotPos = pathInfo.lastIndexOf(".");
        if (dotPos==-1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not extract file extension below path info");
            return;
        }
        String pathName = pathInfo.substring(0, dotPos);
        String extension = pathInfo.substring(dotPos+1);
        
        /* Work out where the source TeX is coming from and read it in, if found */
        String contentResourceName = CONTENT_BASE_DIRECTORY + pathName + ".tex";
        InputStream contentStream = getServletContext().getResourceAsStream(contentResourceName);
        if (contentStream==null) {
            getServletContext().log("Could not read in LaTeX source content stream " + contentResourceName);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        /* Handle request for page source now, if appropriate */
        if (extension.equals("tex")) {
            response.setContentType("text/plain");
            IOUtilities.transfer(contentStream, response.getOutputStream());
            return;
        }

        /* Parse the TeX */
        SnuggleTeXSession session = snapshot.createSession();
        SnuggleInput input = new SnuggleInput(contentStream, contentResourceName);
        session.parseInput(input);
        
        /* Log any errors as they are caused by bad documentation and need fixed! */
        List<InputError> errors = session.getErrors();
        if (!errors.isEmpty()) {
            for (InputError error : errors) {
                getServletContext().log(MessageFormatter.formatErrorAsString(error));
            }
        }
        
        /* Create stylesheet to format the resulting web page */
        Transformer stylesheet;
        try {
            stylesheet = getStylesheet(WEBPAGE_XSLT_ATTRIBUTE_NAME, WEBPAGE_XSLT_RESOURCE_PATH).newTransformer();
            stylesheet.setParameter("context-path", request.getContextPath());
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create stylesheet from Templates", e);
        }
        
        /* Decide what type of page to generate from the file extension */
        WebPageType webPageType = null;
        if (extension.equals("xhtml")) {
            webPageType = WebPageType.MOZILLA;
        }
        else if (extension.equals("xml")) {
            webPageType = WebPageType.CROSS_BROWSER_XHTML;
        }
        else if (extension.equals("html")) {
            webPageType = WebPageType.MATHPLAYER_HTML;
        }
        else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unexpected file extension " + extension);
            return;
        }
        
        /* Generate and serve the resulting web page */
        MathMLWebPageBuilderOptions options = new MathMLWebPageBuilderOptions();
        options.setPageType(webPageType);
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setStylesheet(stylesheet);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setIndenting(true);
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
}
