/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.aardvark.commons.util.ConstraintUtilities;
import uk.ac.ed.ph.snuggletex.conversion.DOMBuilder;
import uk.ac.ed.ph.snuggletex.conversion.LaTeXTokeniser;
import uk.ac.ed.ph.snuggletex.conversion.SessionContext;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleParseException;
import uk.ac.ed.ph.snuggletex.conversion.TokenFixer;
import uk.ac.ed.ph.snuggletex.conversion.WebPageBuilder;
import uk.ac.ed.ph.snuggletex.conversion.XMLUtilities;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinCommand;
import uk.ac.ed.ph.snuggletex.definitions.BuiltinEnvironment;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedCommand;
import uk.ac.ed.ph.snuggletex.definitions.UserDefinedEnvironment;
import uk.ac.ed.ph.snuggletex.tokens.ArgumentContainerToken;
import uk.ac.ed.ph.snuggletex.tokens.FlowToken;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
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
 *     Create a session with {@link SnuggleTeXEngine#createSession()}, optionally passing
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
public final class SnuggleTeXSession implements SessionContext {
    
    private final SnuggleTeXEngine engine;
    private final LaTeXTokeniser tokeniser;
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
     * {@link SnuggleTeXEngine#createSession()} et al.)
     */
    SnuggleTeXSession(final SnuggleTeXEngine engine, final SessionConfiguration configuration) {
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
     * {@link Snapshot} via {@link Snapshot#createSession()}.)
     */
    SnuggleTeXSession(final Snapshot snapshot) {
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
     * @return true if parsing finished, false if it was terminated by an error.
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
     * Creates a {@link Snapshot} Object holding the current state of this session that can be later
     * used to recreate a session having exactly the same state.
     * <p>
     * This may only be called whilst the session is open.
     * 
     * @throws IllegalStateException if the session has been closed.
     */
    public Snapshot createSnapshot() {
    	return new Snapshot(engine,
    			(SessionConfiguration) configuration.clone(),
    			new ArrayList<InputError>(errors), 
    			new HashMap<String, UserDefinedCommand>(userCommandMap),
    			new HashMap<String, UserDefinedEnvironment>(userEnvironmentMap),
    			new ArrayList<FlowToken>(parsedTokens));
    }
    
    /**
     * Builds a DOM sub-tree based on the currently parsed tokens, appending the results as
     * children of the given target root Element. This uses the default {@link DOMBuilderOptions}
     * associated with this engine.
     * 
     * @param targetRoot
     * @return true if successful, false if a failure caused the process to terminate.
     */
    public boolean buildDOMSubtree(final Element targetRoot) {
        return buildDOMSubtree(targetRoot, engine.getDefaultDOMBuilderOptions());
    }
    
    /**
     * Builds a DOM sub-tree based on the currently parsed tokens, appending the results as
     * children of the given target root Element. The given {@link DOMBuilderOptions} Object
     * is used to configure the process.
     * 
     * @param targetRoot
     * @return true if successful, false if a failure caused the process to terminate.
     */
    public boolean buildDOMSubtree(final Element targetRoot, final DOMBuilderOptions options) {
        ConstraintUtilities.ensureNotNull(targetRoot, "targetRoot");
        ConstraintUtilities.ensureNotNull(options, "options");
        try {
            DOMBuilder domBuilder = new DOMBuilder(this, targetRoot, options);
            domBuilder.buildDOMSubtree(parsedTokens);
        }
        catch (SnuggleParseException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Convenience method to build a DOM {@link NodeList} representing the converted Tokens.
     * These Nodes will belong to a "fake root" element in the
     * {@link SnuggleTeX#SNUGGLETEX_NAMESPACE} namespace called "root".
     * <p>
     * The default {@link DOMBuilderOptions} specified in the {@link SnuggleTeXEngine} will be
     * used.
     * 
     * @return resulting {@link NodeList} or null if failure caused the process to terminate
     */
    public NodeList buildDOMSubtree() {
        return buildDOMSubtree(engine.getDefaultDOMBuilderOptions());
    }
    
    /**
     * Convenience method to build a DOM {@link NodeList} representing the converted Tokens.
     * These Nodes will belong to a "fake root" element in the
     * {@link SnuggleTeX#SNUGGLETEX_NAMESPACE} namespace called "root".
     * <p>
     * The given {@link DOMBuilderOptions} Object is used to configure the process.
     * 
     * @return resulting {@link NodeList} or null if failure caused the process to terminate
     */
    public NodeList buildDOMSubtree(final DOMBuilderOptions options) {
        Document document = XMLUtilities.createNSAwareDocumentBuilder().newDocument();
        Element temporaryRoot = document.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, "root");
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
     * The default {@link DOMBuilderOptions} specified in the {@link SnuggleTeXEngine} will be
     * used.
     * 
     * @return resulting XML or null if a failure caused the process the terminate
     */
    public String buildXMLString() {
        return buildXMLString(engine.getDefaultDOMBuilderOptions());
    }
    
    /**
     * Convenience method to create a well-formed external general parsed entity out of the
     * currently parsed tokens.
     * <p>
     * The given {@link DOMBuilderOptions} Object is used to configure the process.
     * 
     * @return resulting XML or null if a failure caused the process the terminate
     */
    public String buildXMLString(final DOMBuilderOptions options) {
        DocumentBuilder documentBuilder = XMLUtilities.createNSAwareDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element temporaryRoot = document.createElement("root");
        if (!buildDOMSubtree(temporaryRoot, options)) {
            return null;
        }
        NodeList nodes = temporaryRoot.getChildNodes();
        for (int i=0, length=nodes.getLength(); i<length; i++) {
            document.appendChild(nodes.item(i));
        }
        TransformerFactory transformerFactory = XMLUtilities.createTransformerFactory();
        StringWriter resultWriter = new StringWriter();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(resultWriter));
        }
        catch (Exception e) {
            throw new SnuggleRuntimeException("Could not serialize DOM document", e);
        }
        return resultWriter.toString();
    }
    
    //---------------------------------------------

    /**
     * Builds a complete web page based on the currently parsed tokens, returning a DOM
     * {@link Document} Object. The provided {@link WebPageBuilderOptions} are used to
     * configure the process.
     */
    public Document createWebPage(final WebPageBuilderOptions options) {
        ConstraintUtilities.ensureNotNull(options, "options");
        try {
            WebPageBuilder webBuilder = new WebPageBuilder(this, options);
            return webBuilder.createWebPage(parsedTokens);
        }
        catch (SnuggleParseException e) {
            return null;
        }
    }
    
    /**
     * Builds a complete web page based on the currently parsed tokens, sending the results
     * to the given {@link OutputStream}.
     */
    public boolean writeWebPage(final WebPageBuilderOptions options, final OutputStream outputStream)
            throws IOException {
        return writeWebPage(options, null, outputStream);
    }
    
    /**
     * Builds a complete web page based on the currently parsed tokens, sending the results
     * to the given {@link OutputStream}. If the <tt>contentTypeSettable</tt> Object has a
     * property called <tt>contentType</tt>, then it is set in advance to the appropriate HTTP
     * <tt>Content-Type</tt> header for the resulting page before the web page data is written.
     */
    public boolean writeWebPage(final WebPageBuilderOptions options, final Object contentTypeSettable,
            final OutputStream outputStream) throws IOException {
        ConstraintUtilities.ensureNotNull(options, "options");
        ConstraintUtilities.ensureNotNull(outputStream, "outputStream");
        try {
            WebPageBuilder webBuilder = new WebPageBuilder(this, options);
            webBuilder.writeWebPage(parsedTokens, contentTypeSettable, outputStream);
            return true;
        }
        catch (SnuggleParseException e) {
            return false;
        }
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
