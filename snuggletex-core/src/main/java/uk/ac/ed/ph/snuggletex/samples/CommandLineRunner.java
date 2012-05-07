/* $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.samples;

import uk.ac.ed.ph.snuggletex.InputError;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleRuntimeException;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptionsTemplates;
import uk.ac.ed.ph.snuggletex.DOMOutputOptions.ErrorOutputOptions;
import uk.ac.ed.ph.snuggletex.SnuggleSession.EndOutputAction;
import uk.ac.ed.ph.snuggletex.WebPageOutputOptions.WebPageType;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trivial command line interface for running SnuggleTeX on a number of inputs files (or STDIN),
 * supporting most of the XML- and web-based output options.
 * 
 * @since 1.2.2
 *
 * @author  David McKain
 * @version $Revision$
 */
public class CommandLineRunner {
    
    private final String[] args;
    private final List<String> inputFiles;
    private WebPageOutputOptions snuggleOptions;
    private boolean requestedWebOutput;
    private boolean isQuiet;
    
    public CommandLineRunner(String[] args) {
        this.args = args;
        this.inputFiles = new ArrayList<String>();
        this.snuggleOptions = null;
        this.requestedWebOutput = false;
        this.isQuiet = false;
    }
    
    public int execute() {
        /* Show usage if nothing was provided on command line */
        if (args.length==0) {
            showHelp();
            return 1;
        }
        
        /* Parse arguments, show usage and exit if anything was invalid */
        try {
            if (!parseCommandLineArguments()) {
                showUsage();
                return 1;
            }
        }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            showHelp();
            return 1;
        }
        
        /* Make sure we have at least one input file */
        if (inputFiles.isEmpty()) {
            System.err.println("No input files specified");
            showHelp();
            return 1;
        }
        
        /* Now do actual SnuggleTeX work */
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        try {
            /* Process each input file in turn */
            for (String inputFile : inputFiles) {
                SnuggleInput input;
                if (inputFile.equals("-")) {
                    /* Read from STDIN */
                    input = new SnuggleInput(System.in);
                }
                else {
                    /* Read from file */
                    input = new SnuggleInput(new File(inputFile));
                }
                session.parseInput(input);
            }
            
            /* Build output */
            if (requestedWebOutput) {
                session.writeWebPage(snuggleOptions, System.out, EndOutputAction.FLUSH);
            }
            else {
                System.out.println(session.buildXMLString(snuggleOptions));
            }
            System.out.println();
        }
        catch (IOException e) {
            System.err.println("Got IOException running SnuggleTeX: " + e.getMessage());
        }

        /* Maybe show any errors the were generated */
        if (!isQuiet) {
            for (InputError error : session.getErrors()) {
                System.err.println(MessageFormatter.formatErrorAsString(error));
            }
        }
        return 0;
    }
    
    private void showHelp() {
        System.out.println("For help and usage, use the -? option");
    }
    
    private void showUsage() {
        InputStream usageStream = getClass().getClassLoader().getResourceAsStream("uk/ac/ed/ph/snuggletex/command-line-usage.txt");
        try {
            IOUtilities.transfer(usageStream, System.out);
        }
        catch (IOException e) {
            throw new SnuggleRuntimeException("Unexpected Exception printing out usage info", e);
        }
    }
    
    /**
     * (Returns true to continue executing, false to show usage and exit.
     * Throw an {@link IllegalArgumentException} for an error message.)
     */
    private boolean parseCommandLineArguments() {
        /* First sweep of the arguments is to determine whether we're doing plain old XML output
         * or generating a web page
         */
        WebPageType webPageType = null;
        String arg, nextArg;
        for (int i=0; i<args.length; i++) {
            arg = args[i];
            nextArg = i<args.length-1 ? args[i+1] : null;
            if ("-web".equals(arg)) {
                String webPageTypeName = nextArg;
                if (webPageTypeName==null) {
                    throw new IllegalArgumentException("No value provided for -web option");
                }
                try {
                    webPageType = WebPageType.valueOf(webPageTypeName);
                    requestedWebOutput = true;
                }
                catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown web page type " + webPageTypeName
                            + ". Legal values are"
                            + Arrays.asList(WebPageType.values()));
                }
                i++; /* Skip over argument value */
            }
        }
        /* Now create appropriate Snuggle Options object. We're be slightly cheaty here
         * and use a WebPageOutputOptions even if doing XML output, as the required class is
         * just a proper subclass of this. 
         */
        snuggleOptions = requestedWebOutput ? WebPageOutputOptionsTemplates.createWebPageOptions(webPageType) : new WebPageOutputOptions();
        
        /* Now do second sweep over the arguments to read in everything else */ 
        for (int i=0; i<args.length; i++) {
            arg = args[i];
            nextArg = i<args.length-1 ? args[i+1] : null;
            if ("-".equals(arg)) {
                /* Represents STDIN here */
                inputFiles.add(arg);
            }
            else if ("-?".equals(arg) || "-h".equals(arg) || "-help".equals(arg)) {
                /* Show usage and exit */
                return false;
            }
            else if ("-web".equals(arg)) {
                /* Did this earlier */
                i++; /* (Skip over value) */
                continue;
            }
            else if ("-quiet".equals(arg)) {
                /* Global option */
                isQuiet = true;
            }
            else if (arg.startsWith("-")) {
                /* It's an '-option value' pair */
                if (nextArg==null) {
                    throw new IllegalArgumentException("No value provided for " + arg + " option");
                }
                String name = arg.substring(1);
                String value = nextArg;
                if ("indent".equals(name)) {
                    int indentAmount = 0;
                    boolean badIndent = false;
                    try {
                        indentAmount = Integer.parseInt(value);
                        badIndent = indentAmount < 0;
                    }
                    catch (NumberFormatException e) {
                        badIndent = true;
                    }
                    if (badIndent) {
                        throw new IllegalArgumentException("Indent amount must be a non-negative integer");
                    }
                    if (indentAmount==0) {
                        snuggleOptions.setIndenting(false);
                    }
                    else {
                        snuggleOptions.setIndenting(true);
                        snuggleOptions.setIndent(indentAmount);
                    }
                }
                else if ("errors".equals(name)) {
                    try {
                        snuggleOptions.setErrorOutputOptions(ErrorOutputOptions.valueOf(value));
                    }
                    catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unknown errors option " + value
                                + ". Legal values are "
                                + Arrays.asList(ErrorOutputOptions.values()));
                    }
                }
                else if ("inlinecss".equals(name)) {
                    snuggleOptions.setInliningCSS(parseBoolean(name, value));
                }
                else if ("xhtmlprefix".equals(name)) {
                    if (value.length()>0) {
                        snuggleOptions.setXHTMLPrefix(value);
                        snuggleOptions.setPrefixingXHTML(true);
                    }
                    else {
                        snuggleOptions.setPrefixingXHTML(false);
                    }
                }
                else if ("mathmlprefix".equals(name)) {
                    if (value.length()>0) {
                        snuggleOptions.setMathMLPrefix(value);
                        snuggleOptions.setPrefixingMathML(true);
                    }
                    else {
                        snuggleOptions.setPrefixingMathML(false);
                    }
                }
                else if ("snugglexmlprefix".equals(name)) {
                    if (value.length()>0) {
                        snuggleOptions.setSnuggleXMLPrefix(value);
                        snuggleOptions.setPrefixingSnuggleXML(true);
                    }
                    else {
                        snuggleOptions.setPrefixingSnuggleXML(false);
                    }
                }
                else if ("annotatemathml".equals(name)) {
                    snuggleOptions.setAddingMathSourceAnnotations(parseBoolean(name, value));
                }
                else if ("mathvariantmapping".equals(name)) {
                    snuggleOptions.setMathVariantMapping(parseBoolean(name, value));
                }
                else if ("enc".equals(name)) {
                    snuggleOptions.setEncoding(value);
                }
                else if ("xmldecl".equals(name)) {
                    snuggleOptions.setIncludingXMLDeclaration(parseBoolean(name, value));
                }
                else if ("entities".equals(name)) {
                    snuggleOptions.setUsingNamedEntities(parseBoolean(name, value));
                }
                else if ("dtpublic".equals(name)) {
                    snuggleOptions.setDoctypePublic(value);
                }
                else if ("dtsystem".equals(name)) {
                    snuggleOptions.setDoctypeSystem(value);
                }
                else if ("ctype".equals(name)) {
                    snuggleOptions.setContentType(value);
                }
                else if ("lang".equals(name)) {
                    snuggleOptions.setLang(value);
                }
                else if ("title".equals(name)) {
                    snuggleOptions.setTitle(value);
                }
                else if ("head".equals(name)) {
                    snuggleOptions.setAddingTitleHeading(parseBoolean(name, value));
                }
                else if ("style".equals(name)) {
                    snuggleOptions.setIncludingStyleElement(parseBoolean(name, value));
                }
                else if ("css".equals(name)) {
                    snuggleOptions.addCSSStylesheetURLs(value);
                }
                else if ("clientxsl".equals(name)) {
                    snuggleOptions.addClientSideXSLTStylesheetURLs(value);
                }
                else {
                    throw new IllegalArgumentException("Unknown option " + arg);
                }
                i++; /* (Skip over value) */
            }
            else {
                /* Must be an input file */
                inputFiles.add(arg);
            }
        }
        return true;
    }
    
    private boolean parseBoolean(final String name, final String value) {
        if ("true".equals(value) || "on".equals(value) || "1".equals(value)) {
            return true;
        }
        else if ("false".equals(value) || "off".equals(value) || "0".equals(value)) {
            return false;
        }
        else {
            throw new IllegalArgumentException("Expected option " + name + " to have value true, false, on, off, 1 or 0, but got " + value);
        }
    }
    
    public static void main(String[] args) {
        System.exit(new CommandLineRunner(args).execute());
    }
}
