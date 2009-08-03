/* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import static uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities.extractAnnotationString;
import static uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities.isMathMLElement;
import static uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities.isolateAnnotationXML;
import static uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities.isolateFirstSemanticsBranch;
import static uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities.serializeDocument;
import static uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities.serializeElement;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SerializationOptions;
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
import uk.ac.ed.ph.snuggletex.upconversion.internal.UpConversionPackageDefinitions;
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
 * Variant of {@link UpConversionDemoServlet} that demonstrates a page fragment
 * containing similar results. This is used for showing examples inside DHTML dialog
 * boxes.
 * 
 * @author  David McKain
 * @version $Revision:158 $
 */
public final class UpConversionExampleFragmentServlet extends BaseServlet {
    
    private static final long serialVersionUID = 4376587500238353176L;
    
    /** Logger so that we can log what users are trying out to allow us to improve things */
    private static Logger logger = LoggerFactory.getLogger(UpConversionExampleFragmentServlet.class);
    
    /** Location of XSLT controlling page layout */
    private static final String DISPLAY_XSLT_LOCATION = "classpath:/upconversion-example-fragment.xsl";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doRequest(request, response);
    }
    
    private void doRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* Read in input LaTeX, which must be provided */
        String rawInputLaTeX = request.getParameter("input");
        if (rawInputLaTeX==null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No input provided");
            return;
        }
        String inputLaTeX = rawInputLaTeX.replaceAll("\\s+", " ");
        
        /* Parse the LaTeX */
        SnuggleEngine engine = createSnuggleEngine();
        engine.addPackage(UpConversionPackageDefinitions.getPackage());
        SnuggleSession session = engine.createSession();
        SnuggleInput input = new SnuggleInput("\\[ " + inputLaTeX + " \\]", "Form Input");
        session.parseInput(input);
        
        /* The next bit is exactly the same as with the full servlet.
         * FIXME: Refactor this better!
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
        UpConvertingPostProcessor upConvertingPostProcessor = new UpConvertingPostProcessor();
        boolean badInput = false;
        if (!errors.isEmpty()) {
            /* Input error occurred */
            parsingErrors = new ArrayList<Element>();
            for (InputError error : errors) {
                parsingErrors.add(MessageFormatter.formatErrorAsXML(resultDocument, error, true));
            }
        }
        else if (resultNodeList.getLength()==1 && isMathMLElement(resultNodeList.item(0), "math")) {
            /* Result is a single <math/> element, which looks correct. Note that up-conversion
             * might not have succeeded though.
             */
            mathElement = (Element) resultNodeList.item(0);
            SerializationOptions sourceSerializationOptions = createMathMLSourceSerializationOptions();
            pMathMLInitial = serializeElement(mathElement, sourceSerializationOptions);
            
            /* Do up-conversion and extract wreckage */
            MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetCache());
            Document upConvertedMathDocument = upConverter.upConvertSnuggleTeXMathML(mathElement.getOwnerDocument(), upConvertingPostProcessor.getUpconversionParameterMap());
            mathElement = (Element) upConvertedMathDocument.getDocumentElement().getFirstChild();
            parallelMathML = serializeElement(mathElement, sourceSerializationOptions);
            pMathMLUpConverted = serializeDocument(isolateFirstSemanticsBranch(mathElement), sourceSerializationOptions);
            Document cMathMLDocument = isolateAnnotationXML(mathElement, MathMLUpConverter.CONTENT_MATHML_ANNOTATION_NAME);
            cMathML = cMathMLDocument!=null ? serializeDocument(cMathMLDocument, sourceSerializationOptions) : null;
            maximaInput = extractAnnotationString(mathElement, MathMLUpConverter.MAXIMA_ANNOTATION_NAME);
        }
        else {
            /* This could have been caused by input like 'x \] hello \[ x', which would end
             * up escaping out of Math mode for a while, causing 3 Nodes to be generated in
             * this case.
             */
            badInput = true;
        }
        
        /* Only log failures, as this would normally be caused by bad documentation but could
         * also be due to a clever clogs user! */
        if (!errors.isEmpty()) {
            logger.error("Input: {}", inputLaTeX);
            logger.error("Final MathML: {}", parallelMathML);
            logger.error("Error count: {}", errors.size());
            for (InputError error : errors) {
                logger.error("Error: " + MessageFormatter.formatErrorAsString(error));
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
         * 
         * (NOTE: Actually, we're going to throw away most of the resulting web page completely
         * but following this process makes sure everything is the same as the page it's going
         * to be embedded into, which is probably a good thing here.)
         */
        WebPageOutputOptions options = WebPageOutputOptionsTemplates.createWebPageOptions(webPageType);
        options.setDOMPostProcessors(upConvertingPostProcessor);
        options.setMathVariantMapping(true);
        options.setAddingMathAnnotations(true);
        options.setIndenting(true);
        options.setIncludingStyleElement(false);
        
        /* Create XSLT to generate the resulting page */
        Transformer viewStylesheet = getStylesheet(request, DISPLAY_XSLT_LOCATION);
        viewStylesheet.setParameter("is-mathml-capable", Boolean.valueOf(mathMLCapable));
        viewStylesheet.setParameter("is-internet-explorer", isInternetExplorer(request));
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