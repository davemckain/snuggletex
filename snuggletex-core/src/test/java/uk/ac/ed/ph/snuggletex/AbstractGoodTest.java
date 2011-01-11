package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.internal.DOMBuildingController;
import uk.ac.ed.ph.snuggletex.internal.LaTeXTokeniser;
import uk.ac.ed.ph.snuggletex.internal.SessionContext;
import uk.ac.ed.ph.snuggletex.internal.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.internal.StyleEvaluator;
import uk.ac.ed.ph.snuggletex.internal.StyleRebuilder;
import uk.ac.ed.ph.snuggletex.internal.TokenFixer;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumper;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.tokens.RootToken;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base for tests which take LaTeX input, parse it assuming success.
 * 
 * @author  David McKain
 * @version $Revision$
 */
public abstract class AbstractGoodTest {
    
    private static final Logger log = Logger.getLogger(AbstractGoodTest.class.getName());
    
    protected final String inputLaTeX;
    
    protected String rawDump = null;
    protected String styledDump = null;
    protected String fixedDump = null;
    protected String rebuiltDump = null;
    
    /**
     * Subclasses should call this to initialise {@link #inputLaTeX} as appropriate.
     */
    protected AbstractGoodTest(String inputLaTeX) {
        this.inputLaTeX = inputLaTeX;
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
    
    protected void checkNoErrors(SessionContext sessionContext) {
        List<InputError> errors = sessionContext.getErrors();
        if (!errors.isEmpty()) {
            log.warning("Got " + errors.size() + " unexpected error(s). Details following...");
            for (InputError error : errors) {
                log.warning(MessageFormatter.formatErrorAsString(error));
            }
        }
        Assert.assertTrue(errors.isEmpty());
    }
    
    protected SnuggleSession createSnuggleSession() {
        return new SnuggleEngine().createSession();
    }
    
    protected Document runSnuggleProcessSuccessfully() throws Throwable {
        /* We'll drive the process manually as that gives us richer information if something
         * goes wrong.
         */
        
        SnuggleSession session = createSnuggleSession();
        SnuggleInputReader inputReader = new SnuggleInputReader(session, new SnuggleInput(TestUtilities.massageInputLaTeX(inputLaTeX)));
        
        /* Tokenise */
        LaTeXTokeniser tokeniser = new LaTeXTokeniser(session);
        RootToken rootToken = tokeniser.tokenise(inputReader);
        rawDump = ObjectDumper.dumpObject(rootToken, DumpMode.DEEP);
        
        /* Make sure we got no errors */
        checkNoErrors(session);
        
        /* Evaluate styles */
        StyleEvaluator styleEvaluator = new StyleEvaluator(session);
        styleEvaluator.evaluateStyles(rootToken);
        styledDump = ObjectDumper.dumpObject(rootToken, DumpMode.DEEP);
        
        /* Run token fixer */
        TokenFixer fixer = new TokenFixer(session);
        fixer.fixTokenTree(rootToken);
        fixedDump = ObjectDumper.dumpObject(rootToken, DumpMode.DEEP);
        
        /* Rebuild styles */
        StyleRebuilder styleRebuilder = new StyleRebuilder(session);
        styleRebuilder.rebuildStyles(rootToken);
        rebuiltDump = ObjectDumper.dumpObject(rootToken, DumpMode.DEEP);
           
        /* Make sure we have still got no errors */
        checkNoErrors(session);

        /* Convert to XML */
        Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element rootElement = resultDocument.createElementNS(W3CConstants.XHTML_NAMESPACE, "body");
        resultDocument.appendChild(rootElement);
        
        DOMOutputOptions domOptions = createDOMOutputOptions();
        DOMBuildingController domBuildingController = new DOMBuildingController(session, domOptions);
        domBuildingController.buildDOMSubtree(rootElement, rootToken.getContents());
           
        /* Make sure we have still got no errors */
        checkNoErrors(session);
        return resultDocument;
    }
}