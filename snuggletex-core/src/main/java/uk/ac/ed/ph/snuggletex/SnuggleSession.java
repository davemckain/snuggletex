/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.internal.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.StringUtilities;
import uk.ac.ed.ph.snuggletex.internal.util.XMLUtilities;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.Globals;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;
import uk.ac.ed.ph.snuggletex.internal.AbstractWebPageBuilder;
import uk.ac.ed.ph.snuggletex.internal.DOMBuildingController;
import uk.ac.ed.ph.snuggletex.internal.LaTeXTokeniser;
import uk.ac.ed.ph.snuggletex.internal.MathMLWebPageBuilder;
import uk.ac.ed.ph.snuggletex.internal.SessionContext;
import uk.ac.ed.ph.snuggletex.internal.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.internal.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.internal.TokenFixer;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This represents a single "job" for SnuggleTeX.
 * 
 * <h2>Usage</h2>
 * 
 * <ul>
 *   <li>
 *     Create a session with {@link SnuggleEngine#createSession()}, optionally passing
 *     configuration details for this session.
 *   </li>
 *   <li>
 *     Call {@link #parseInput(SnuggleInput)} on one or more input documents to tokenise and
 *     fix-up the LaTeX contained therein.
 *   </li>
 *   <li>
 *     Call one or more of the DOM and/or web page building methods to generate the appropriate
 *     type of output.
 *   </li>
 *   <li>
 *     Call {@link #getErrors()} to get at the errors that have arisen during this session.
 *   </li>
 *   <li>
 *     Call {@link #createSnapshot()} at any time to make a "snapshot" of the current state for
 *     later reuse.
 *   </li>
 *   <li>
 *     An instance of this Class should only be used by one Thread at a time. It is stateful and
 *     intended to be discarded after use.
 *   </li>
 * </ul>
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class SnuggleSession implements SessionContext {
    
    /** Engine that created this Session */
    private final SnuggleEngine engine;

    /** {@link LaTeXTokeniser} used to parse inputs */
    private final LaTeXTokeniser tokeniser;
    
    /** {@link TokenFixer} used to massage inputs after parsing */
    private final TokenFixer tokenFixer;
    
    //-------------------------------------------------
    // Stateful stuff
    
    /** Configuration for this session */
    private final SessionConfiguration configuration;
    
    /** Errors accumulated during this session */
    private final List<InputError> errors;
    
    /** Map of user-defined commands, keyed on name */
    private final Map<String, UserDefinedCommand> userCommandMap;
    
    /** Map of user-defined environments, keyed on name */
    private final Map<String, UserDefinedEnvironment> userEnvironmentMap;
    
    private final List<FlowToken> parsedTokens; 
    
    /**
     * (This package-private constructor is used when creating a new session via
     * {@link SnuggleEngine#createSession()} et al.)
     */
    SnuggleSession(final SnuggleEngine engine, final SessionConfiguration configuration) {
        this.engine = engine;
        
        /* We'll clone the supplied configuration, if supplied, so that
         * any run-time changes made to it do not affect the caller's version
         * of the configuration.
         */
        this.configuration = configuration;

        /* Set up main worker Objects */
        this.tokeniser = new LaTeXTokeniser(this);
        this.tokenFixer = new TokenFixer(this);
        
        /* Initialise session state */
        this.errors = new ArrayList<InputError>();
        this.userCommandMap = new HashMap<String, UserDefinedCommand>();
        this.userEnvironmentMap = new HashMap<String, UserDefinedEnvironment>();
        this.parsedTokens = new ArrayList<FlowToken>();
    }
    
    /**
     * (This package-private constructor is used when creating a session from an existing
     * {@link SnuggleSnapshot} via {@link SnuggleSnapshot#createSession()}.)
     */
    SnuggleSession(final SnuggleSnapshot snapshot) {
        /* Set up main worker Objects */
        this.tokeniser = new LaTeXTokeniser(this);
        this.tokenFixer = new TokenFixer(this);
        
        /* Copy stuff from the template */
        this.engine = snapshot.engine;
        this.configuration = (SessionConfiguration) snapshot.configuration.clone();
        this.errors = new ArrayList<InputError>(snapshot.errors);
        this.userCommandMap = new HashMap<String, UserDefinedCommand>(snapshot.userCommandMap);
        this.userEnvironmentMap = new HashMap<String, UserDefinedEnvironment>(snapshot.userEnvironmentMap);
        this.parsedTokens = new ArrayList<FlowToken>(snapshot.parsedTokens);
    }
    
    //-------------------------------------------------
    
    public SessionConfiguration getConfiguration() {
        return configuration;
    }

    public List<InputError> getErrors() {
        return errors;
    }
    
    public List<FlowToken> getParsedTokens() {
        return parsedTokens;
    }
    
    //---------------------------------------------
    // Public "business" methods
    
    /**
     * Parses the data contained within the given {@link SnuggleInput}, and performs fixing
     * on the resulting tokens.
     * 
     * @return true if parsing finished, false if it was terminated by an error in the
     *   input LaTeX and if the session was configured to fail on the first error.
     */
    public boolean parseInput(SnuggleInput input) throws IOException {
        /* Perform tokenisation, then fix up and store the results */
        try {
            SnuggleInputReader reader = new SnuggleInputReader(this, input);
            ArgumentContainerToken result = tokeniser.tokenise(reader);
            tokenFixer.fixTokenTree(result);
            parsedTokens.addAll(result.getContents());
        }
        catch (SnuggleParseException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Creates a {@link SnuggleSnapshot} Object holding the current state of this session that can be later
     * used to recreate a session having exactly the same state.
     * <p>
     * This may only be called whilst the session is open.
     * 
     * @throws IllegalStateException if the session has been closed.
     */
    public SnuggleSnapshot createSnapshot() {
        return new SnuggleSnapshot(engine,
                (SessionConfiguration) configuration.clone(),
                new ArrayList<InputError>(errors), 
                new HashMap<String, UserDefinedCommand>(userCommandMap),
                new HashMap<String, UserDefinedEnvironment>(userEnvironmentMap),
                new ArrayList<FlowToken>(parsedTokens));
    }
    
    //---------------------------------------------
    
    /**
     * Builds a DOM sub-tree based on the currently parsed tokens, appending the results as
     * children of the given target root Element. The given {@link DOMOutputOptions} Object
     * is used to configure the process.
     * <p>
     * If the {@link DOMOutputOptions} specifies that MathML should be down-converted to
     * XHTML where possible, then this will also happen.
     * 
     * @return true if completed successfully, false if the process was terminated by an error in the
     *   input LaTeX and if the session was configured to fail on the first error. 
     */
    public boolean buildDOMSubtree(final Element targetRoot, final DOMOutputOptions options) {
        ConstraintUtilities.ensureNotNull(targetRoot, "targetRoot");
        ConstraintUtilities.ensureNotNull(options, "options");
        try {
            new DOMBuildingController(this, options).buildDOMSubtree(targetRoot, parsedTokens);
            return true;
        }
        catch (SnuggleParseException e) {
            return false;
        }
    }
    
    /**
     * Builds a DOM sub-tree based on the currently parsed tokens, appending the results as
     * children of the given target root Element. This uses the default {@link DOMOutputOptions}
     * associated with this engine.
     * 
     * @return true if completed successfully, false if the process was terminated by an error in the
     *   input LaTeX and if the session was configured to fail on the first error. 
     */
    public boolean buildDOMSubtree(final Element targetRoot) {
        return buildDOMSubtree(targetRoot, engine.getDefaultDOMOptions());
    }
    
    /**
     * Convenience method to build a DOM {@link NodeList} representing the converted Tokens.
     * These Nodes will belong to a "fake root" element in the
     * {@link SnuggleConstants#SNUGGLETEX_NAMESPACE} namespace called "root".
     * <p>
     * The default {@link DOMOutputOptions} specified in the {@link SnuggleEngine} will be
     * used.
     * 
     * @return resulting {@link NodeList} if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error. 
     */
    public NodeList buildDOMSubtree() {
        return buildDOMSubtree(engine.getDefaultDOMOptions());
    }
    
    /**
     * Convenience method to build a DOM {@link NodeList} representing the converted Tokens.
     * These Nodes will belong to a "fake root" element in the
     * {@link SnuggleConstants#SNUGGLETEX_NAMESPACE} namespace called "root".
     * <p>
     * The given {@link DOMOutputOptions} Object is used to configure the process.
     * 
     * @return resulting {@link NodeList} if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error. 
     */
    public NodeList buildDOMSubtree(final DOMOutputOptions options) {
        Document document = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element temporaryRoot = document.createElementNS(SnuggleConstants.SNUGGLETEX_NAMESPACE, "root");
        document.appendChild(temporaryRoot);
        if (!buildDOMSubtree(temporaryRoot, options)) {
            return null;
        }
        return temporaryRoot.getChildNodes();
    }
    
    //---------------------------------------------
    
    /**
     * Convenience method to create a well-formed external general parsed entity out of the
     * currently parsed tokens.
     * <p>
     * The default {@link DOMOutputOptions} specified in the {@link SnuggleEngine} will be
     * used.
     *
     * @return resulting XML if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error.  
     */
    public String buildXMLString() {
        return buildXMLString(engine.getDefaultDOMOptions(), false);
    }
    
    /**
     * Convenience method to create a well-formed external general parsed entity out of the
     * currently parsed tokens.
     * <p>
     * The default {@link DOMOutputOptions} specified in the {@link SnuggleEngine} will be
     * used.
     * 
     * @param indent whether to indent the resulting XML or not
     *
     * @return resulting XML if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error.  
     */
    public String buildXMLString(boolean indent) {
        return buildXMLString(engine.getDefaultDOMOptions(), indent);
    }
    
    /**
     * Convenience method to create a well-formed external general parsed entity out of the
     * currently parsed tokens.
     * <p>
     * The given {@link DOMOutputOptions} Object is used to configure the process.
     * 
     * @return resulting XML if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error. 
     */
    public String buildXMLString(final DOMOutputOptions options) {
        return buildXMLString(options, false);
    }
    
    /**
     * Convenience method to create a well-formed external general parsed entity out of the
     * currently parsed tokens.
     * <p>
     * The given {@link DOMOutputOptions} Object is used to configure the process.
     * 
     * @param indent whether to indent the resulting XML or not
     * 
     * @return resulting XML if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error. 
     */
    public String buildXMLString(final DOMOutputOptions options, final boolean indent) {
        DocumentBuilder documentBuilder = XMLUtilities.createNSAwareDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element temporaryRoot = document.createElement("root");
        document.appendChild(temporaryRoot);
        if (!buildDOMSubtree(temporaryRoot, options)) {
            return null;
        }
        StringWriter resultWriter = new StringWriter();
        try {
            Transformer serializer = getStylesheetManager()
                .getStylesheet(Globals.XML_STRING_XSL_RESOURCE_NAME).newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.setOutputProperty(OutputKeys.INDENT, StringUtilities.toYesNo(indent));
            serializer.transform(new DOMSource(document), new StreamResult(resultWriter));
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Could not serialize", e);
        }
        return resultWriter.toString();
    }
    
    //---------------------------------------------

    /**
     * Builds a complete web page based on the currently parsed tokens, returning a DOM
     * {@link Document} Object.
     * <p>
     * The provided {@link BaseWebPageOptions} Object is
     * used to determine which type of web page to generate and how it should be configured.
     * <p>
     * Any XSLT stylesheet specified by {@link BaseWebPageOptions#getStylesheet()}
     * will have been applied to the result before it is returned. On the other hand, serialisation
     * options in the {@link BaseWebPageOptions} (such as Content Type and encoding) 
     * will not have been applied when this method returns.
     * 
     * @return resulting Document if the process completed successfully, null if the process was
     *   terminated by an error in the input LaTeX and if the session was configured to fail on
     *   the first error. 
     */
    public Document createWebPage(final BaseWebPageOptions options) {
        ConstraintUtilities.ensureNotNull(options, "options");
        try {
            AbstractWebPageBuilder<?> webBuilder = createWebPageBuilder(options);
            return webBuilder.createWebPage(parsedTokens);
        }
        catch (SnuggleParseException e) {
            return null;
        }
    }
    
    /**
     * Builds a complete web page based on the currently parsed tokens, sending the results
     * to the given {@link OutputStream}.
     * <p>
     * The provided {@link BaseWebPageOptions} Object is
     * used to determine which type of web page to generate and how it should be configured.
     * 
     * @return true if completed successfully, false if the process was terminated by an error in the
     *   input LaTeX and if the session was configured to fail on the first error. 
     */
    public boolean writeWebPage(final BaseWebPageOptions options, final OutputStream outputStream)
            throws IOException {
        return writeWebPage(options, null, outputStream);
    }
    
    /**
     * Builds a complete web page based on the currently parsed tokens, sending the results
     * to the given {@link OutputStream}.
     * <p>
     * The provided {@link BaseWebPageOptions} Object is
     * used to determine which type of web page to generate and how it should be configured.
     * <p>
     * If the <tt>contentTypeSettable</tt> Object has a
     * property called <tt>contentType</tt>, then it is set in advance to the appropriate HTTP
     * <tt>Content-Type</tt> header for the resulting page before the web page data is written.
     * 
     * @return true if completed successfully, false if the process was terminated by an error in the
     *   input LaTeX and if the session was configured to fail on the first error. 
     * 
     * @throws IOException if an I/O problem arose whilst writing out the web page data.
     * @throws SnuggleRuntimeException if calling <tt>setContentType()</tt> on the contentTypeSettable
     *   Object failed, with the underlying Exception wrapped up.
     */
    public boolean writeWebPage(final BaseWebPageOptions options, final Object contentTypeSettable,
            final OutputStream outputStream) throws IOException {
        ConstraintUtilities.ensureNotNull(options, "options");
        ConstraintUtilities.ensureNotNull(outputStream, "outputStream");
        try {
            AbstractWebPageBuilder<?> webBuilder = createWebPageBuilder(options);
            webBuilder.writeWebPage(parsedTokens, contentTypeSettable, outputStream);
            return true;
        }
        catch (SnuggleParseException e) {
            return false;
        }
    }
    
    /**
     * Creates the appropriate instance of {@link AbstractWebPageBuilder} that will build
     * web pages supporting the given {@link BaseWebPageOptions} Object.
     * 
     * @param options
     */
    private AbstractWebPageBuilder<?> createWebPageBuilder(BaseWebPageOptions options) {
        AbstractWebPageBuilder<?> result = null;
        if (options instanceof MathMLWebPageOptions) {
            result = new MathMLWebPageBuilder(this, (MathMLWebPageOptions) options);
        }
        else if (options.getClass().getName().equals("uk.ac.ed.ph.snuggletex.jeuclid.JEuclidWebPageOptions")) {
            /* Use reflection to instantiate as this is an "extension" as we don't want to
             * hard-wire a dependency on it just in case it's not being used.
             */
            try {
                Class<?> builderClass = Class.forName("uk.ac.ed.ph.snuggletex.jeuclid.JEuclidWebPageBuilder");
                Class<?> optionsClass = Class.forName("uk.ac.ed.ph.snuggletex.jeuclid.JEuclidWebPageOptions");
                Constructor<?> constructor = builderClass.getConstructor(SessionContext.class, optionsClass);
                result = (AbstractWebPageBuilder<?>) constructor.newInstance(this, options);
            }
            catch (Exception e) {
                throw new SnuggleRuntimeException("Could not load SnuggleTeX JEuclid Extensions - please check your ClassPath", e);
            }
        }
        else {
            throw new SnuggleRuntimeException("SnuggleTeX doesn't know how to build web pages using options of type "
                    + options.getClass().getName());
        }
        return result;
    }
    
    //---------------------------------------------
    // Business helpers
    
    public BuiltinCommand getCommandByTeXName(String texName) {
        return engine.getCommandByTeXName(texName);
    }
    
    public BuiltinEnvironment getEnvironmentByTeXName(String texName) {
        return engine.getEnvironmentByTeXName(texName);
    }
    
    public Map<String, UserDefinedCommand> getUserCommandMap() {
        return userCommandMap;
    }
    
    public Map<String, UserDefinedEnvironment> getUserEnvironmentMap() {
        return userEnvironmentMap;
    }
    
    public StylesheetManager getStylesheetManager() {
        return engine.getStylesheetManager();
    }

    /**
     * Records a new error, throwing a {@link SnuggleParseException} if
     * the current {@link SessionConfiguration} deems that we should
     * fail on the first error.
     * 
     * @param error
     * @throws SnuggleParseException 
     */
    public void registerError(InputError error) throws SnuggleParseException {
        errors.add(error);
        if (configuration.isFailingFast()) {
            throw new SnuggleParseException(error);
        }
    }
}
