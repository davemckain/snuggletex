/* $Id$
 *
 * Copyright 2009 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;
import uk.ac.ed.ph.snuggletex.utilities.SnuggleUtilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;

/**
 * Builds a SnuggleTeX documentation page showing all of the error codes. This is called
 * as part of the webapp build process.
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class ErrorCodeDocumentBuilder {
    
    private final File outputFile;
    
    private static final Map<String, String> categoryHeadingMap;
    
    private static final String[] categoryData = {
        "TTE", "LaTeX Parsing/Tokenisation Errors",
        "TFE", "Token Fix-up Errors",
        "TDE", "DOM Building Errors",
        "UCF", "Failures during Up-Conversion to Content MathML",
        "UMF", "Failures during Up-Conversion to Maxima syntax"
    };
    
    static {
        categoryHeadingMap = new HashMap<String, String>();
        for (int i=0; i<categoryData.length; i+=2) {
            categoryHeadingMap.put(categoryData[i], categoryData[i+1]);
        }
    }
    
    public ErrorCodeDocumentBuilder(final File outputFile) {
        this.outputFile = outputFile;
    }
    
    public void run() throws IOException {
        IOUtilities.ensureFileCreated(outputFile);
        PrintWriter outputWriter = new PrintWriter(outputFile);
        outputWriter.println("\\pageId{errorCodes}");
        outputWriter.println("\n(In the tables below, \\{0\\} et\\ al\\ are placeholders for details "
                + "specific to each error instance that are substituted in when formatting error messages)");
        
        /* We just loop over each ErrorCode. Note that I'm relying on the fact that they've
         * been entered in a sensible order which makes grouping trivial here.
         */
        String currentCategory = null;
        String category;
        String codeName;
        PropertyResourceBundle errorMessageBundle = MessageFormatter.ERROR_MESSAGE_BUNDLE;
        for (ErrorCode errorCode : ErrorCode.values()) {
            codeName = errorCode.name();
            category = codeName.substring(0,3);
            if (!category.equals(currentCategory)) {
                /* Starting new category; finish current table if open */
                if (currentCategory!=null) {
                    outputWriter.println("\\end{tabular}");                    
                }
                currentCategory = category;
                outputWriter.println("\n\\subsection*{"
                        + category
                        + "xxx: "
                        + categoryHeadingMap.get(category)
                        + "}");
                outputWriter.println("\\begin{tabular}{|c|l|}\n\\hline");
            }
            outputWriter.println("\\anchor{"
                    + codeName
                    + "}\\verb|"
                    + codeName
                    + "| & "
                    + SnuggleUtilities.quoteTextForInput(errorMessageBundle.getString(codeName))
                    + " \\\\ \\hline");
        }
        if (currentCategory!=null) {
            outputWriter.println("\\end{tabular}");
        }
        outputWriter.close();
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length!=1) {
            System.err.println("Please supply an output file");
            System.exit(1);
        }
        new ErrorCodeDocumentBuilder(new File(args[0])).run();
    }
}
