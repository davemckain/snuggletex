/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.demowebapp;

import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleTeXEngine;
import uk.ac.ed.ph.snuggletex.SnuggleTeXSession;
import uk.ac.ed.ph.snuggletex.WebPageBuilderOptions;
import uk.ac.ed.ph.snuggletex.DOMBuilderOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageBuilderOptions.WebPageType;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

/**
 * Trivial servlet to provide the functionality for the "try out" page.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class TryOutServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Location of XSLT controlling page layout */
    public static final String TRYOUT_XSLT_LOCATION = "/WEB-INF/tryout.xsl";
    
    /** Compiled XSLT */
    public static Templates templates;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        /* Pre-compile special XSLT that builds on the standard XSLT to put the
         * input LaTeX into a simple form.
         */       
        templates = compileStylesheet(TRYOUT_XSLT_LOCATION);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }
    
    private void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Read in input LaTeX, using some placeholder text if nothing was provided */
        String inputLaTeX = request.getParameter("input");
        if (inputLaTeX!=null) {
            /* Tidy up line endings */
            inputLaTeX = inputLaTeX.replaceAll("(\r\n|\r|\n)", "\n");
        }
        else {
            inputLaTeX = "Hello!";
        }
        
        /* Parse the TeX */
        SnuggleTeXEngine engine = new SnuggleTeXEngine();
        SnuggleTeXSession session = engine.createSession();
        
        SnuggleInput input = new SnuggleInput(inputLaTeX, "Form Input");
        session.parseInput(input);

        
        /* Create XSLT to generate the resulting page */
        Transformer stylesheet;
        try {
            stylesheet = templates.newTransformer();
            stylesheet.setParameter("context-path", request.getContextPath());
            stylesheet.setParameter("latex-input", inputLaTeX);
        }
        catch (TransformerConfigurationException e) {
            throw new ServletException("Could not create stylesheet from Templates", e);
        }
        
        /* Generate and serve the resulting web page */
        WebPageBuilderOptions options = new WebPageBuilderOptions();
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setPageType(WebPageType.CROSS_BROWSER_XHTML);
        options.setStylesheet(stylesheet);
        options.setErrorOptions(ErrorOutputOptions.XHTML);
        options.setTitle("SnuggleTeX - Try Out");
        options.setAddingTitleHeading(false); /* We'll put our own title in */
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
