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
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Servlet demonstrating the up-conversion process on user-entered MATH mode
 * SnuggleTeX input.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class UpConversionDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(UpConversionDemoServlet.class);
    
    /** Default input to use when first showing the page */
    private static final String DEFAULT_INPUT = "\\frac{2x-y^2}{\\sin xy(x-2)}";
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "classpath:/upconversion-demo.xsl";
    
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
        session.buildDOMSubtree(resultRoot, domOptions);
        
        /* See if parsing succeeded and generated a single <math/> element. We'll only continue
         * up-converting if this happened.
         */
        NodeList resultNodeList = resultRoot.getChildNodes();
        List<InputError> errors = session.getErrors();
        Element mathElement = null;
        String parallelMathML = null;
        String pMathMLInitial = null;
        String pMathMLUpConverted = null;
        String cMathML = null;
        String maximaInput = null;
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
            /* Result is a single <math/> element, which looks correct. Note that up-conversion
             * might not have succeeded though.
             */
            mathElement = (Element) resultNodeList.item(0);
            pMathMLInitial = MathMLUtilities.serializeElement(mathElement);
            
            /* Do up-conversion and extract wreckage */
            MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetCache());
            Map<String, Object> upConversionOptions = new HashMap<String, Object>();
            Document upConvertedMathDocument = upConverter.upConvertSnuggleTeXMathML(mathElement.getOwnerDocument(), upConversionOptions);
            mathElement = (Element) upConvertedMathDocument.getDocumentElement().getFirstChild();
            parallelMathML = MathMLUtilities.serializeElement(mathElement, "ASCII");
            pMathMLUpConverted = MathMLUtilities.serializeElement(MathMLUtilities.extractFirstSemanticsBranch(mathElement), "ASCII");
            NodeList cMathMLElement = MathMLUtilities.extractAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
            cMathML = cMathMLElement!=null && cMathMLElement.getLength()>0 ? MathMLUtilities.serializeElement((Element) cMathMLElement.item(0), "ASCII") : null;
            maximaInput = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
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
                logger.info("Final MathML: {}", parallelMathML);
            }
            else {
                logger.warn("Input: {}", inputLaTeX);
                logger.warn("Final MathML: {}", parallelMathML);
                logger.warn("Error count: {}", errors.size());
                for (InputError error : errors) {
                    logger.warn("Error: " + MessageFormatter.formatErrorAsString(error));
                }
            }
        }
        
        /* Decide what type of page to output based on UserAgent, following
         * same logic as MathInputDemoServlet
         */
        WebPageType webPageType= chooseBestWebPageType(request);
        boolean mathMLCapable = webPageType!=null;
        if (webPageType==null) {
            webPageType = WebPageType.PROCESSED_HTML;
        }
        
        /* We'll cheat slightly and bootstrap off the SnuggleTeX web page generation process,
         * even though most of the interesting page content is going to be fed in as stylesheet
         * parameters.
         * 
         * (The actual content passed to the XSLT here will be the final MathML Document that
         * we produced manually above, though this will actually be recreated using the standard
         * SnuggleTeX process.)
         */
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(webPageType);
        options.setDOMPostProcessors(new UpConvertingPostProcessor());
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setIndenting(true);
        options.setIncludingStyleElement(false);
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(request, DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("mathml-capable", Boolean.valueOf(mathMLCapable));
        viewStylesheet.setParameter("latex-input", inputLaTeX);
        viewStylesheet.setParameter("is-bad-input", Boolean.valueOf(badInput));
        viewStylesheet.setParameter("parsing-errors", parsingErrors);
        viewStylesheet.setParameter("parallel-mathml", parallelMathML);
        viewStylesheet.setParameter("pmathml-initial", pMathMLInitial);
        viewStylesheet.setParameter("pmathml-upconverted", pMathMLUpConverted);
        viewStylesheet.setParameter("cmathml", cMathML);
        viewStylesheet.setParameter("maxima-input", maximaInput);
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