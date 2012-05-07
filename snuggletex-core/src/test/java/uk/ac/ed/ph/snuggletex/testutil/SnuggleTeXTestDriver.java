/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.testutil;

import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.definitions.W3CConstants;
import uk.ac.ed.ph.snuggletex.internal.DOMBuildingController;
import uk.ac.ed.ph.snuggletex.internal.LaTeXTokeniser;
import uk.ac.ed.ph.snuggletex.internal.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.internal.StyleEvaluator;
import uk.ac.ed.ph.snuggletex.internal.StyleRebuilder;
import uk.ac.ed.ph.snuggletex.internal.TokenFixer;
import uk.ac.ed.ph.snuggletex.internal.util.DumpMode;
import uk.ac.ed.ph.snuggletex.internal.util.ObjectDumper;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.tokens.RootToken;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class to drive tests that expect successful parsing and DOM Building,
 * carefully logging things if they go wrong.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleTeXTestDriver {
    
    static final Logger log = Logger.getLogger(SnuggleTeXTestDriver.class.getName());
    
    public static interface DriverCallback {
        void verifyDOM(Document document) throws Throwable;
    }
    
    private final SnuggleEngine snuggleEngine;
    private DriverCallback driverCallback;
    
    
    public SnuggleTeXTestDriver(final SnuggleEngine snuggleEngine, final DriverCallback driverCallback) {
        this.snuggleEngine = snuggleEngine;
        this.driverCallback = driverCallback;
    }
    
    public void run(String inputLaTeX) throws Throwable {
        String rawDump = null, styledDump = null, fixedDump = null, rebuiltDump = null;
        try {
            /* We'll drive the process manually as that gives us richer information if something
             * goes wrong.
             */
            SnuggleSession session = snuggleEngine.createSession();
            SnuggleInputReader inputReader = new SnuggleInputReader(session, new SnuggleInput(TestUtilities.massageInputLaTeX(inputLaTeX)));
            
            /* Tokenise */
            LaTeXTokeniser tokeniser = new LaTeXTokeniser(session);
            RootToken rootToken = tokeniser.tokenise(inputReader);
            rawDump = ObjectDumper.dumpObject(rootToken, DumpMode.DEEP);
            
            /* Make sure we got no errors */
            TestUtilities.assertNoErrors(session);
            
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
            TestUtilities.assertNoErrors(session);

            /* Convert to XML */
            Document resultDocument = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
            Element rootElement = resultDocument.createElementNS(W3CConstants.XHTML_NAMESPACE, "body");
            resultDocument.appendChild(rootElement);
            
            DOMOutputOptions domOutputOptions = new DOMOutputOptions();
            domOutputOptions.setMathVariantMapping(true);
            domOutputOptions.setPrefixingSnuggleXML(true);
            
            DOMBuildingController domBuildingController = new DOMBuildingController(session, domOutputOptions);
            domBuildingController.buildDOMSubtree(rootElement, rootToken.getContents());
               
            /* Make sure we have still got no errors */
            TestUtilities.assertNoErrors(session);
            
            /* Now verify */
            driverCallback.verifyDOM(resultDocument);
        }
        catch (Throwable e) {
            log.severe("SnuggleTeXCaller failure. Input was: " + inputLaTeX);
            if (rawDump!=null) {
                log.severe("Raw dump was: " + rawDump);
            }
            if (styledDump!=null) {
                log.severe("Style evaluated dump was: " + styledDump);
            }
            if (fixedDump!=null) {
                log.severe("Fixed dump was: " + fixedDump);
            }
            if (rebuiltDump!=null) {
                log.severe("Rebuilt dump was: " + rebuiltDump);
            }
            log.log(Level.SEVERE, "Error was: ", e);
            throw e;
        }
    }

}
