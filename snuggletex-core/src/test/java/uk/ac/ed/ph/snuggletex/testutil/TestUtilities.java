/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.testutil;

import static org.easymock.EasyMock.createStrictControl;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.internal.SessionContext;
import uk.ac.ed.ph.snuggletex.internal.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.WorkingDocument;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;
import uk.ac.ed.ph.snuggletex.utilities.SerializationOptions;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import junit.framework.Assert;

import org.easymock.IMocksControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.CountingErrorHandler;

/**
 * Some random utility methods for tests. (Also useful for standalone messing about with
 * certain classes.)
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class TestUtilities {
    
    private static final Logger log = Logger.getLogger(TestUtilities.class.getName());
    
    public static final String MATHML_30_SCHEMA_LOCATION = "classpath:/mathml3.rnc";
    
    public static void verifyXML(String expectedXML, Document document) throws Throwable {
        try {
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
            Transformer serializer = XMLUtilities.createJAXPTransformerFactory().newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.transform(new DOMSource(document), new SAXResult(saxControl));
            control.verify();
            saxControl.verify();
        }
        catch (Throwable e) {
            log.severe("XML Verification failed");
            log.severe("Expected output: " + expectedXML);
            log.severe("Actual output:   " + MathMLUtilities.serializeDocument(document, new SerializationOptions()));
            throw e;
        }

    }
    
    public static void assertNoErrors(SessionContext sessionContext) {
        List<InputError> errors = sessionContext.getErrors();
        if (!errors.isEmpty()) {
            log.warning("Got " + errors.size() + " unexpected error(s). Details following...");
            for (InputError error : errors) {
                log.warning(MessageFormatter.formatErrorAsString(error));
            }
        }
        Assert.assertTrue(errors.isEmpty());
    }

    /**
     * Overridden to perform RELAX-NG validation against the MathML 3.0 schema
     * to ensure that there are no warning, errors or fatal errors in the resulting XML.
     */
    public static void assertMathMLValid(Document mathmlDocument) throws Throwable {
        ClassPathResolver resolver = new ClassPathResolver();
        PropertyMapBuilder builder = new PropertyMapBuilder();
        builder.put(ValidateProperty.RESOLVER, resolver);
        PropertyMap schemaProperties = builder.toPropertyMap();
        
        CountingErrorHandler errorHandler = new CountingErrorHandler();
        builder = new PropertyMapBuilder();
        builder.put(ValidateProperty.ERROR_HANDLER, errorHandler);
        PropertyMap validationProperties = builder.toPropertyMap();
        
        SchemaReader sr = CompactSchemaReader.getInstance();
        InputSource schemaSource = new InputSource(MATHML_30_SCHEMA_LOCATION);
        Schema schema = sr.createSchema(schemaSource, schemaProperties);
        Validator validator = schema.createValidator(validationProperties);
        
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(validator.getContentHandler());
        xmlReader.parse(new InputSource(new StringReader(MathMLUtilities.serializeDocument(mathmlDocument))));
        
        try {
            Assert.assertEquals("Warning count", 0, errorHandler.getWarningCount());
            Assert.assertEquals("Error count", 0, errorHandler.getErrorCount());
            Assert.assertEquals("Fatal error count", 0, errorHandler.getFatalErrorCount());
        }
        catch (Throwable e) {
            log.severe("Relax NG validation failed - see error details");
            log.severe("Output was: " + MathMLUtilities.serializeDocument(mathmlDocument, new SerializationOptions()));
            throw e;
        }
    }

    /* (NB: Does fix-in-place!) */
    public static void promoteMathElement(Document document) {
        /* Should only have 1 child of doc root (<body/>) element here, which should be <math/>.
         * We'll make that the new root Node */
        Node rootElement = document.getChildNodes().item(0);
        
        Element firstMathElement = null;
        NodeList childNodes = rootElement.getChildNodes();
        for (int i=0, length=childNodes.getLength(); i<length; i++) {
            Node childNode = childNodes.item(i);
            if (MathMLUtilities.isMathMLElement(childNode, "math")) {
                if (firstMathElement!=null) {
                    Assert.fail("Found more than one <math/> children");
                }
                firstMathElement = (Element) childNode;
            }
            else if (childNode.getNodeType()==Node.ELEMENT_NODE) {
                Assert.fail("Found unexpected element under root");
            }
            else if (childNode.getNodeType()==Node.TEXT_NODE && childNode.getNodeValue().matches("\\S")) {
                Assert.fail("Found non-whitespace text Node");
            }
        }
        if (firstMathElement==null) {
            Assert.fail("No <math/> child found");
        }
        document.removeChild(rootElement);
        document.appendChild(firstMathElement);
    }
    
    public static final WorkingDocument createWorkingDocument(String input)
            throws IOException, SnuggleParseException {
        SnuggleInput snuggleInput = new SnuggleInput(input);
        SnuggleEngine engine = new SnuggleEngine();
        return new SnuggleInputReader(engine.createSession(), snuggleInput).createWorkingDocument();
    }

    public static String massageInputLaTeX(String inputLaTeX) {
        /* Allow %n for newlines */
        String result = inputLaTeX.replace("%n", "\n");
        
        /* Allow %unn up to %unnnnn for arbitrary Unicode characters */
        Pattern unicodePattern = Pattern.compile("%u([0-9a-fA-F]{2,5})");
        Matcher matcher = unicodePattern.matcher(result);
        StringBuffer resultBuilder = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            /* (Note that this may go past U+FFFF so require multiple UTF-16 chars) */
            matcher.appendReplacement(resultBuilder, new String(Character.toChars(Integer.parseInt(hex, 16))));
        }
        matcher.appendTail(resultBuilder);
        return resultBuilder.toString();
    }

    /**
     * Wraps in the MathML test input data, by adding in the enclosing <tt>math</tt>
     * element in the correct namespace. It also removes indentation whitespace.
     */
    public static String wrapMathMLTestData(String mathmlTestData) {
        return "<math xmlns='" + W3CConstants.MATHML_NAMESPACE + "'>"
            + mathmlTestData.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\s+$", "").replace("\n", "")
            + "</math>";
    }
}
