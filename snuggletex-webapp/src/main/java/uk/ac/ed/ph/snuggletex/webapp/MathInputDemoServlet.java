/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsTemplates;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Basic servlet demonstrating the SnuggleTeX process on simple MATH Mode LaTeX Inputs.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class MathInputDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 6381049839030645425L;

    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(MathInputDemoServlet.class);
    
    /** Default input to use when first showing the page */
    private static final String DEFAULT_INPUT = "1+x";
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "classpath:/math-input-demo.xsl";
    
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
            /* Normalise any input space */
            inputLaTeX = rawInputLaTeX.replaceAll("\\s+", " ");
        }
        else {
            inputLaTeX = DEFAULT_INPUT;
        }
        
        /* Parse the LaTeX */
        SnuggleEngine engine = new SnuggleEngine(getStylesheetCache());
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput("\\[ " + inputLaTeX + " \\]", "Form Input");
        session.parseInput(input);
        
        /* Create raw DOM, without any up-conversion for the time being. I've done this
         * so that we can show how much the PMathML hopefully improves after up-conversion!
         */
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element resultRoot = resultDocument.createElement("root");
        resultDocument.appendChild(resultRoot);
        DOMOutputOptions domOptions = new DOMOutputOptions();
        domOptions.setMathVariantMapping(true);
        domOptions.setAddingMathAnnotations(true);
        domOptions.setErrorOutputOptions(ErrorOutputOptions.NO_OUTPUT);
        session.buildDOMSubtree(resultRoot);
        
        /* See if parsing succeeded and generated a single <math/> element. We'll only continue
         * up-converting if this happened.
         */
        NodeList resultNodeList = resultRoot.getChildNodes();
        List<InputError> errors = session.getErrors();
        Element resultMathElement = null;
        String resultMathML = null;
        List<Element> parsingErrors = null;
        boolean badInput = false;
        if (!errors.isEmpty()) {
            /* Input error occurred */
            parsingErrors = new ArrayList<Element>();
            for (InputError error : errors) {
                parsingErrors.add(MessageFormatter.formatErrorAsXML(resultDocument, error, true));
            }
        }
        else if (resultNodeList.getLength()==1 && MathMLUtilities.isMathMLElement(resultNodeList.item(0), "math")) {
            /* Result is a single <math/> element, which looks correct. */
            resultMathElement = (Element) resultNodeList.item(0);
            resultMathML = MathMLUtilities.serializeElement(resultMathElement);
        }
        else {
            /* This could have been caused by input like 'x \] hello \[ x', which would end
             * up escaping out of Math mode for a while, causing 3 Nodes to be generated in
             * this case.
             */
            badInput = true;
        }
        
        /* Log things nicely */
        if (rawInputLaTeX!=null) {
            if (errors.isEmpty()) {
                logger.info("Input: {}", inputLaTeX);
                logger.info("Resulting MathML: {}", resultMathML);
            }
            else {
                logger.warn("Input: {}", inputLaTeX);
                logger.warn("Resulting MathML: {}", resultMathML);
                logger.warn("Error count: {}", errors.size());
                for (InputError error : errors) {
                    logger.warn("Error: " + MessageFormatter.formatErrorAsString(error));
                }
            }
        }
        
        /* We'll cheat slightly and bootstrap off the SnuggleTeX web page generation process,
         * even though most of the interesting page content is going to be fed in as stylesheet
         * parameters.
         * 
         * (The actual content passed to the XSLT here will be the final MathML Document that
         * we produced manually above, though this will actually be recreated using the standard
         * SnuggleTeX process.)
         */
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(WebPageType.CROSS_BROWSER_XHTML);
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setIndenting(true);
        options.setIncludingStyleElement(false);
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("context-path", request.getContextPath());
        viewStylesheet.setParameter("latex-input", inputLaTeX);
        viewStylesheet.setParameter("is-bad-input", Boolean.valueOf(badInput));
        viewStylesheet.setParameter("parsing-errors", parsingErrors);
        viewStylesheet.setParameter("result-mathml", resultMathML);
        options.setStylesheets(viewStylesheet);
        
        /* Generate and serve the resulting web page */
        try {
            session.writeWebPage(options, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
}