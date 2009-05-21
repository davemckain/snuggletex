/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trivial servlet to provide the functionality for the "try out" page.
 *
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class TryOutServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private Logger logger = LoggerFactory.getLogger(TryOutServlet.class);
    
    /** Location of XSLT controlling page layout */
    public static final String TRYOUT_XSLT_LOCATION = "classpath:/tryout.xsl";
    
    /** Location of default input to use when visiting the page for the first time */
    public static final String DEFAULT_INPUT_LOCATION = "/WEB-INF/tryout-default.tex";
    
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
        String rawInputLaTeX = request.getParameter("input");
        String inputLaTeX;
        if (rawInputLaTeX!=null) {
            /* Tidy up line endings */
            inputLaTeX = rawInputLaTeX.replaceAll("(\r\n|\r|\n)", "\n");
        }
        else {
            inputLaTeX = readDefaultInput();
        }
        
        /* Parse the LaTeX */
        SnuggleEngine engine = new SnuggleEngine(getStylesheetCache());
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput(inputLaTeX, "Form Input");
        session.parseInput(input);
        
        /* Set up web output options */
        MathMLWebPageOptions options = new MathMLWebPageOptions();
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setPageType(WebPageType.CROSS_BROWSER_XHTML);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setTitle("SnuggleTeX - Try Out");
        options.setAddingTitleHeading(false); /* We'll put our own title in */
        options.setIndenting(true);
        options.setCSSStylesheetURLs(
                request.getContextPath() + "/includes/physics.css",
                request.getContextPath() + "/includes/snuggletex.css"
        );
        
        /* Create output for logging purposes */
        String xmlString = session.buildXMLString(options);
        
        /* Log things nicely */
        if (rawInputLaTeX!=null) {
            List<InputError> errors = session.getErrors();
            if (errors.isEmpty()) {
                logger.info("Input:  {}", inputLaTeX);
                logger.info("Output: {}", xmlString);
            }
            else {
                logger.warn("Input:  {}" + inputLaTeX);
                logger.warn("Output: {}", xmlString);
                logger.warn("Errors: #{}", errors.size());
                for (InputError error : errors) {
                    logger.warn("Error:  " + MessageFormatter.formatErrorAsString(error));
                }
            }
        }
        
        /* Create XSLT to generate the resulting page */
        Transformer stylesheet = getStylesheet(TRYOUT_XSLT_LOCATION);
        stylesheet.setParameter("context-path", request.getContextPath());
        stylesheet.setParameter("latex-input", inputLaTeX);
        options.setStylesheet(stylesheet);
        
        /* Generate and serve the resulting web page */
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }

    /**
     * Creates the initial sample input to use when we first visit the page.
     * <p>
     * This simply loads up data from {@link #DEFAULT_INPUT_LOCATION}.
     * <p>
     * We could maybe cache this in future, but there's probably no real point at the moment.
     * 
     * @throws ServletException 
     * @throws IOException 
     */
    private String readDefaultInput() throws ServletException, IOException {
        InputStream resourceStream = ensureReadResource(DEFAULT_INPUT_LOCATION);
        return IOUtilities.readUnicodeStream(resourceStream);
    }
}
