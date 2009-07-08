/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.DownConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsTemplates;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
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
public final class FullLaTeXInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    private Logger logger = LoggerFactory.getLogger(FullLaTeXInputDemoServlet.class);
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "classpath:/full-latex-input-demo.xsl";
    
    /** Location of default input to use when visiting the page for the first time */
    private static final String DEFAULT_INPUT_LOCATION = "/WEB-INF/full-latex-input-demo-default.tex";
    
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
        
        /* Decide what type of page to output based on UserAgent */
        WebPageType webPageType= chooseBestWebPageType(request);
        boolean mathMLCapable = webPageType!=null;
        
        /* If UserAgent can't handle MathML then we'll use HTML output and get the XSLT
         * to replace the MathML with a reference to an image rendition of it.
         */
        if (webPageType==null) {
            webPageType = WebPageType.PROCESSED_HTML;
        }
        
        /* Set up web output options */
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(webPageType);
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        options.setIndenting(true);
        options.setIncludingStyleElement(false);
        
        /* If browser can't handle MathML, we'll add post-processors to down-convert
         * simple expressions to XHTML + CSS and replace the remaining MathML islands
         * with dynamically generated images.
         */
        if (webPageType==WebPageType.PROCESSED_HTML) {
            options.setDOMPostProcessors(
                    new DownConvertingPostProcessor(),
                    new MathMLToImageLinkPostProcessor(request.getContextPath())
            );
        }
        
        /* Log things nicely if input was specified by user */
        if (rawInputLaTeX!=null) {
            String xmlString = session.buildXMLString(options);
            List<InputError> errors = session.getErrors();
            if (errors.isEmpty()) {
                logger.info("Input:  {}", inputLaTeX);
                logger.info("Output: {}", xmlString);
            }
            else {
                logger.warn("Input:  {}", inputLaTeX);
                logger.warn("Output: {}", xmlString);
                logger.warn("Errors: #{}", errors.size());
                for (InputError error : errors) {
                    logger.warn("Error:  " + MessageFormatter.formatErrorAsString(error));
                }
            }
        }
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(request, DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("is-mathml-capable", Boolean.valueOf(mathMLCapable));
        viewStylesheet.setParameter("is-internet-explorer", isInternetExplorer(request));
        viewStylesheet.setParameter("latex-input", inputLaTeX);
        options.setStylesheets(viewStylesheet);
        
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
