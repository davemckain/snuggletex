/* $Id: org.eclipse.jdt.ui.prefs 3 2008-04-25 12:10:29Z davemckain $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.webapp;

import uk.ac.ed.ph.snuggletex.ErrorCode;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;
import uk.ac.ed.ph.snuggletex.utilities.SnuggleUtilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.PropertyResourceBundle;

/**
 * Builds a SnuggleTeX documentation page showing all of the error codes. This is called
 * as part of the webapp build process.
 *
 * @author  David McKain
 * @version $Revision: 3 $
 */
public final class ErrorCodeDocumentBuilder {
    
    private final File outputFile;
    
    public ErrorCodeDocumentBuilder(final File outputFile) {
        this.outputFile = outputFile;
    }
    
    public void run() throws IOException {
        PrintWriter outputWriter = new PrintWriter(outputFile);
        outputWriter.println("\\pageId{errorCodes}");
        outputWriter.println("\n\\section*{SnuggleTeX Error Codes}");
        outputWriter.println("\n(In the table below, \\{0\\} et\\ al\\ are placeholders for details "
                + "specific to each error instance)");
        outputWriter.println("\n\\begin{tabular}{|c|l|}\n\\hline");
        
        PropertyResourceBundle errorMessageBundle = MessageFormatter.ERROR_MESSAGE_BUNDLE;
        for (ErrorCode errorCode : ErrorCode.values()) {
            outputWriter.println("\\verb|"
                    + errorCode.name()
                    + "| & "
                    + SnuggleUtilities.quoteTextForInput(errorMessageBundle.getString(errorCode.name()))
                    + " \\\\ \\hline");
        }
        outputWriter.println("\\end{tabular}");
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
