/* $Id: AbstractGoodXMLTest.java 302 2009-05-11 13:46:21Z davemckain $
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import static org.easymock.EasyMock.createStrictControl;

import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.internal.DOMBuildingController;
import uk.ac.ed.ph.snuggletex.internal.LaTeXTokeniser;
import uk.ac.ed.ph.snuggletex.internal.SessionContext;
import uk.ac.ed.ph.snuggletex.internal.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.internal.TokenFixer;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumper;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.testutil.EasyMockContentHandler;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.easymock.IMocksControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Base for tests which take LaTeX input, parse it and compare the resulting XML
 * against the specified output.
 * 
 * @author  David McKain
 * @version $Revision: 302 $
 */
public abstract class AbstractGoodXMLTest {
    
    private static final Logger log = Logger.getLogger(AbstractGoodXMLTest.class.getName());
    
    protected final String inputLaTeX;
    protected final String expectedXML;
    
    protected AbstractGoodXMLTest(String inputLaTeX, String expectedXML) {
        this.inputLaTeX = inputLaTeX;
        this.expectedXML = expectedXML;
    }
    
    protected abstract void fixupDocument(Document document);
    
    protected boolean showTokensOnFailure() {
        return true;
    }
    
    /**
     * Sets up the {@link DOMOutputOptions} to use for the test.
     * <p>
     * The default is a fairly vanilla set of options, but with math variant
     * mapping turned on.
     * <p>
     * Subclasses may override as required.
     */
    protected DOMOutputOptions createDOMOutputOptions() {
        DOMOutputOptions result = new DOMOutputOptions();
        result.setMathVariantMapping(true);
        result.setPrefixingSnuggleXML(true);
        return result;
    }
    
    private void checkNoErrors(SessionContext sessionContext) {
        List<InputError> errors = sessionContext.getErrors();
        if (!errors.isEmpty()) {
            log.warning("Got " + errors.size() + " unexpected error(s). Details following...");
            for (InputError error : errors) {
                log.warning(MessageFormatter.formatErrorAsString(error));
            }
        }
        Assert.assertTrue(errors.isEmpty());
    }
    
    protected void verifyResultDocument(TransformerFactory transformerFactory, Document resultDocument) throws Throwable {
        /* Create mock to handle the SAX streams */
        IMocksControl control = createStrictControl();
        EasyMockContentHandler saxControl = new EasyMockContentHandler(control);
        
        /* Fire expected output at handler */
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        InputSource inputSource = new InputSource(new StringReader(expectedXML));
        reader.setContentHandler(saxControl);
        reader.parse(inputSource);
        
        /* Now replay and fire actual resulting XML to mock as SAX stream */
        control.replay();
        saxControl.replay();
        
        /* Finally verify everything */
        Transformer serializer = transformerFactory.newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.transform(new DOMSource(resultDocument), new SAXResult(saxControl));
        control.verify();
        saxControl.verify();
    }
    
    public void runTest() throws Throwable {
        String rawDump = null, fixedDump = null, output=null;

        /* We'll drive the process manually as that gives us richer information if something
         * goes wrong.
         */
        
        /* First set up a suitable configuration for these tests. In future, we may want to
         * have tests in different configurations. (This would be easier if configs could be
         * changed at run-time via LaTeX markup!)
         */
        SessionConfiguration configuration =  new SessionConfiguration();

        /* Set up DOMOutputOptions */
        DOMOutputOptions domOptions = createDOMOutputOptions();
        
        SnuggleSession session = new SnuggleEngine().createSession(configuration);
        SnuggleInputReader inputReader = new SnuggleInputReader(session, new SnuggleInput(inputLaTeX));
        try {
            /* Tokenise */
            LaTeXTokeniser tokeniser = new LaTeXTokeniser(session);
            ArgumentContainerToken outerToken = tokeniser.tokenise(inputReader);
            rawDump = ObjectDumper.dumpObject(outerToken, DumpMode.DEEP);
            
            /* Make sure we got no errors */
            checkNoErrors(session);
            
            /* Run token fixer */
            TokenFixer fixer = new TokenFixer(session);
            fixer.fixTokenTree(outerToken);
            fixedDump = ObjectDumper.dumpObject(outerToken, DumpMode.DEEP);
               
            /* Make sure we have still got no errors */
            checkNoErrors(session);
    
            /* Convert to XML */
            Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
            Element rootElement = resultDocument.createElementNS(Globals.XHTML_NAMESPACE, "body");
            resultDocument.appendChild(rootElement);
            
            DOMBuildingController domBuildingController = new DOMBuildingController(session, domOptions);
            domBuildingController.buildDOMSubtree(rootElement, outerToken.getContents());
               
            /* Make sure we have still got no errors */
            checkNoErrors(session);
    
            /* Let subclass fudge up the resulting document if required */
            fixupDocument(resultDocument);
            
            /* Serialize the output */
            StringWriter outputWriter = new StringWriter();
            TransformerFactory transformerFactory = XMLUtilities.createJAXPTransformerFactory();
            Transformer serializer = transformerFactory.newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.transform(new DOMSource(resultDocument), new StreamResult(outputWriter));
            output = outputWriter.toString();
            
            /* Verify result */
            verifyResultDocument(transformerFactory, resultDocument);
        }
        catch (Throwable e) {
            log.severe("Input was: " + inputLaTeX);
            if (showTokensOnFailure()) {
                if (rawDump!=null) {
                    log.severe("Raw dump was: " + rawDump);
                }
                if (fixedDump!=null) {
                    log.severe("Fixed dump was: " + fixedDump);
                }
            }
            if (output!=null) {
                log.severe("Expected output: " + expectedXML);
                log.severe("Actual output:   " + output);
            }
            log.log(Level.SEVERE, "Error was: ", e);
            throw e;
        }
    }
}