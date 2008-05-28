/* $Id$
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.conversion.FrozenSlice;
import uk.ac.ed.ph.snuggletex.conversion.SnuggleInputReader;
import uk.ac.ed.ph.snuggletex.conversion.WorkingDocument;
import uk.ac.ed.ph.snuggletex.conversion.WorkingDocument.CharacterSource;
import uk.ac.ed.ph.snuggletex.conversion.WorkingDocument.IndexResolution;
import uk.ac.ed.ph.snuggletex.conversion.WorkingDocument.Slice;
import uk.ac.ed.ph.snuggletex.conversion.WorkingDocument.SourceContext;
import uk.ac.ed.ph.snuggletex.definitions.Globals;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class that formats an {@link InputError} in various ways.
 * 
 * NOTE: We're using {@link MessageFormat} extensively here, which hasn't been updated to use
 * {@link StringBuilder} so we'll sadly have to make do with {@link StringBuffer}
 *
 * @author  David McKain
 * @version $Revision$
 */
public final class MessageFormatter {
    
    private static final PropertyResourceBundle errorMessageBundle;
    private static final PropertyResourceBundle generalMessageBundle;
    
    static {
        try {
            errorMessageBundle = (PropertyResourceBundle) ResourceBundle.getBundle(Globals.ERROR_MESSAGES_PROPERTIES_BASENAME);
            generalMessageBundle = (PropertyResourceBundle) ResourceBundle.getBundle(Globals.GENERAL_MESSAGES_PROPERTIES_BASENAME);
        }
        catch (MissingResourceException e) {
            throw new SnuggleLogicException(e);
        }
    }
    
    /** Constructs an error message for the given {@link InputError}. */
    public static String getErrorMessage(InputError error) {
        return MessageFormat.format(errorMessageBundle.getString(error.getErrorCode().toString()),
                error.getArguments());
    }
    
    /** Creates a full diagnosis of the given error */
    public static String formatErrorAsString(InputError error) {
        StringBuffer resultBuilder = new StringBuffer();
        appendErrorAsString(resultBuilder, error);
        return resultBuilder.toString();
    }
    
    /**
     * Creates a DOM {@link Element} containing information about the given error, including
     * either just the {@link ErrorCode} or full details.
     * 
     * @param ownerDocument {@link Document} that will contain the resulting element.
     * @param error {@link InputError} to format
     * @param fullDetails false if you just want the error code, true for full details.
     */
    public static Element formatErrorAsXML(Document ownerDocument, InputError error, boolean fullDetails) {
        Element result = ownerDocument.createElementNS(SnuggleTeX.SNUGGLETEX_NAMESPACE, "error");
        result.setAttribute("code", error.getErrorCode().name());
        
        if (fullDetails) {
            /* Nicely format XML error content */
            StringBuffer messageBuilder = new StringBuffer(getErrorMessage(error));
            FrozenSlice errorSlice = error.getSlice();
            if (errorSlice!=null) {
                appendSliceContext(messageBuilder, errorSlice);
            }
            
            /* Add message as child node */
            result.appendChild(ownerDocument.createTextNode(messageBuilder.toString()));
        }
        /* That's it! */
        return result;
    }
    
    public static Element formatErrorAsXHTML(Document ownerDocument, InputError error) {
        Element result = ownerDocument.createElementNS(Globals.XHTML_NAMESPACE, "div");
        result.setAttribute("class", "error");
        
        Element heading = ownerDocument.createElementNS(Globals.XHTML_NAMESPACE, "h2");
        heading.appendChild(ownerDocument.createTextNode("SnuggleTeX Error (" + error.getErrorCode() + ")"));
        
        Element pre = ownerDocument.createElementNS(Globals.XHTML_NAMESPACE, "pre");
        
        /* Nicely format XML error content */
        StringBuffer messageBuilder = new StringBuffer(getErrorMessage(error));
        FrozenSlice errorSlice = error.getSlice();
        if (errorSlice!=null) {
            appendSliceContext(messageBuilder, errorSlice);
        }
        
        /* Add message as child node */
        pre.appendChild(ownerDocument.createTextNode(messageBuilder.toString()));
        
        /* That's it! */
        result.appendChild(heading);
        result.appendChild(pre);
        return result;
    }
    
    public static void appendErrorAsString(StringBuffer messageBuilder, InputError error) {
        new MessageFormat(generalMessageBundle.getString("error_as_string")).format(new Object[] {
                error.getErrorCode().toString(), /* Error code */
                getErrorMessage(error) /* Error Message */
        }, messageBuilder, null);
        FrozenSlice errorSlice = error.getSlice();
        if (errorSlice!=null) {
            appendSliceContext(messageBuilder, errorSlice);
        }
    }
    
    private static void appendNewlineIfRequired(StringBuffer messageBuilder) {
        if (messageBuilder.length()>0) {
            messageBuilder.append('\n');
        }
    }

    public static void appendSliceContext(StringBuffer messageBuilder, FrozenSlice slice) {
        WorkingDocument document = slice.getDocument();
        
        /* Work out where the error occurred */
        IndexResolution errorResolution = document.resolveIndex(slice.startIndex, false);
        if (errorResolution==null) {
            /* (If this happens, then most likely the error occurred at the end of the document) */
            errorResolution = document.resolveIndex(slice.startIndex, true);
        }
        if (errorResolution==null) {
            throw new SnuggleLogicException("Could not resolve component containing error slice starting at "
                    + slice.startIndex);
        }
        Slice errorSlice = errorResolution.slice;
        CharacterSource errorComponent = errorSlice.resolvedComponent;
        int errorIndex = errorResolution.indexInComponent;
        appendFrame(messageBuilder, errorComponent, errorIndex);
    }
    
    private static void appendFrame(StringBuffer messageBuilder, CharacterSource source, int offsetInSource) {
        SourceContext context = source.context;
        System.out.println("source=" + source + ",offsetInSource=" + offsetInSource + ",context=" + context);
        if (context instanceof SnuggleInputReader) {
            SnuggleInputReader inputContext = (SnuggleInputReader) context;
            int[] location = inputContext.getLineAndColumn(offsetInSource);
            appendNewlineIfRequired(messageBuilder);
            new MessageFormat(generalMessageBundle.getString("input_context")).format(new Object[] {
                  location[0], /* Line */
                  location[1], /* Column */
                  inputContext.getInput().getIdentifier() /* Input description */
            }, messageBuilder, null);
        }
        else if (context instanceof WorkingDocument.SubstitutionContext) {
            WorkingDocument.SubstitutionContext substitutionContext = (WorkingDocument.SubstitutionContext) context;
            appendNewlineIfRequired(messageBuilder);
            
            /* The replacement text can be a bit long and span multiple lines so we'll tidy it up
             * a bit first.
             */
            String tidiedReplacement = substitutionContext.replacement.toString().replaceAll("\\s", " ");
            if (tidiedReplacement.length()>20) {
                tidiedReplacement = tidiedReplacement.substring(0, 20) + "...";
            }
            new MessageFormat(generalMessageBundle.getString("subs_context")).format(new Object[] {
                    offsetInSource, /* Character index */
                    source.substitutedText, /* Before subs */
                    tidiedReplacement /* After subs */
            }, messageBuilder, null);
        }
        else {
            throw new SnuggleLogicException("Unexpected SourceContext " + context.getClass().getName());
        }
        if (source.substitutedSource!=null) {
            appendFrame(messageBuilder, source.substitutedSource, source.substitutionOffset);
        }
    }
}
