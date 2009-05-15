/* $Id:TryOutServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.MathMLWebPageOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Servlet demonstrating the up-conversion process on user-entered MATH mode
 * inputs.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class UpConversionDemoServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private Logger log = Logger.getLogger(UpConversionDemoServlet.class.getName());
    
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
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput("\\[ " + inputLaTeX + " \\]", "Form Input");
        session.parseInput(input);
        
        /* Create raw DOM, without any up-conversion for the time being. I've done this
         * so that we can show how much the PMathML hopefully "improves".
         */
        DOMOutputOptions domOptions = new DOMOutputOptions();
        domOptions.setMathVariantMapping(true);
        domOptions.setAddingMathAnnotations(true);
        domOptions.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        NodeList result = session.buildDOMSubtree(domOptions);
        
        /* See if parsing succeeded and generated a single <math/> element. We'll only continue
         * up-converting if this happened.
         */
        boolean isParsingSuccess = result.getLength()==1
            && result.item(0).getNodeType()==Node.ELEMENT_NODE
            && session.getErrors().isEmpty();
        Element mathElement = null;
        String parallelMathML = null;
        String pMathMLInitial = null;
        String pMathMLUpconverted = null;
        String cMathML = null;
        String maximaInput = null;
        if (isParsingSuccess) {
            mathElement = (Element) result.item(0);
            pMathMLInitial = MathMLUtilities.serializeElement(mathElement);
            
            /* Do up-conversion and extract wreckage */
            MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetCache());
            Map<String, Object> upConversionOptions = new HashMap<String, Object>();
            Document upConvertedMathDocument = upConverter.upConvertSnuggleTeXMathML(mathElement.getOwnerDocument(), upConversionOptions);
            mathElement = (Element) upConvertedMathDocument.getDocumentElement().getFirstChild();
            parallelMathML = MathMLUtilities.serializeElement(mathElement);
            pMathMLUpconverted = MathMLUtilities.serializeElement(MathMLUtilities.extractFirstSemanticsBranch(mathElement));
            NodeList cMathMLElement = MathMLUtilities.extractAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
            cMathML = cMathMLElement!=null ? MathMLUtilities.serializeElement((Element) cMathMLElement.item(0)) : null;
            maximaInput = MathMLUtilities.extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        }
        
        /* Log things nicely */
        if (rawInputLaTeX!=null) {
            List<InputError> errors = session.getErrors();
            Level level = errors.isEmpty() ? Level.INFO : Level.WARNING;
            log.log(level, "Input: " + inputLaTeX);
            log.log(level, "Final Math Output: " + parallelMathML);
            log.log(level, "Error count: " + errors.size());
            for (InputError error : errors) {
                log.log(level, "Error: " + MessageFormatter.formatErrorAsString(error));
            }
        }
        
        /* We'll cheat slightly and bootstrap off the SnuggleTeX web page generation process,
         * even though most of the interesting page content is going to be fed in as stylesheet
         * parameters.
         */
        MathMLWebPageOptions webOutputOptions = new MathMLWebPageOptions();
        webOutputOptions.setMathVariantMapping(true);
        webOutputOptions.setAddingMathAnnotations(true);
        webOutputOptions.setPageType(WebPageType.CROSS_BROWSER_XHTML);
        webOutputOptions.setErrorOutputOptions(ErrorOutputOptions.XHTML);
        webOutputOptions.setTitle("LaTeX to MathML and Maxima");
        webOutputOptions.setAddingTitleHeading(false); /* We'll put our own title in */
        webOutputOptions.setIndenting(true);
        webOutputOptions.setCSSStylesheetURLs(
                request.getContextPath() + "/includes/physics.css"
        );
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("context-path", request.getContextPath());
        viewStylesheet.setParameter("latex-input", inputLaTeX);
        viewStylesheet.setParameter("is-parsing-success", Boolean.valueOf(isParsingSuccess));
        viewStylesheet.setParameter("parallel-mathml", parallelMathML);
        viewStylesheet.setParameter("pmathml-initial", pMathMLInitial);
        viewStylesheet.setParameter("pmathml-upconverted", pMathMLUpconverted);
        viewStylesheet.setParameter("cmathml", cMathML);
        viewStylesheet.setParameter("maxima-input", maximaInput);
        webOutputOptions.setStylesheet(viewStylesheet);
        
        /* Generate and serve the resulting web page */
        try {
            session.writeWebPage(webOutputOptions, response, response.getOutputStream());
        }
        catch (Exception e) {
            throw new ServletException("Unexpected Exception", e);
        }
    }
}