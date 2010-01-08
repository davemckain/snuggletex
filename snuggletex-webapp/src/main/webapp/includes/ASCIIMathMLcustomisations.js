/*
 * Provides basic code for managing ASCIIMathML input widgets.
 * Feel free to use or build on this as necessary.
 *
 * NOTE:
 *
 * This code uses the lovely jQuery library, but avoids using the
 * $(...) function just in case your code also uses some other library
 * like prototype that defines its own $(...) function.
 * (In this case, you will still want to read:
 *
 * http://docs.jquery.com/Using_jQuery_with_Other_Libraries
 *
 * to make sure you do whatver is necessary to make sure that both
 * libraries co-exist correctly.)
 *
 * Requirements:
 *
 * ASCIIMathML.js
 * ASCIIMathMLeditor.js
 * jquery.js
 *
 *
 * $Id:web.xml 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2010, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

/* (Reset the default (blue) MathML colour chosen by ASCIIMathML) */
var mathcolor = "";

/**
 * Simple hash that will keep track of the current value of each
 * ASCIIMathML input box, keyed on its ID. This is used by
 * updatePreviewIfChanged() to determine whether the MathML preview
 * should be updated or not.
 */
var inputTextByIdMap = {};

/**
 * Hacked version of AMdisplay() from ASCIIMathMLeditor.js that allows
 * us to specify which element to display the resulting MathML
 * in and where the raw input is going to come from.
 *
 * @param {String} mathModeInput ASCIIMathML input string
 * @param {String} previewElementId ID of the XHTML element that will contain the
 *   resulting MathML preview, replacing any existing child Nodes.
 */
function updatePreview(mathModeInput, previewElementId) {
    /* Escape use of backquote symbol to prevent exiting math mode */
    mathModeInput = mathModeInput.replace(/`/, "\\`");
    var input = "` " + mathModeInput + " `";
    /* Do ASCIIMathML processing (essentially the same as AMdisplay() from here on) */
    var outnode = document.getElementById(previewElementId);
    var newnode = AMcreateElementXHTML("div");
    newnode.setAttribute("id", previewElementId);
    outnode.parentNode.replaceChild(newnode, outnode);
    outnode = document.getElementById(previewElementId);
    var n = outnode.childNodes.length;
    for (var i=0; i<n; i++) {
        outnode.removeChild(outnode.firstChild);
    }
    outnode.appendChild(document.createComment(input + "``"));
    AMprocessNode(outnode, true);
}

/**
 * Checks the content of the <input/> element having the given inputBoxId,
 * and calls {@link #updatePreview} if its contents have changed since the
 * last call to this.
 *
 * @param {String} inputBoxId ID of the ASCIIMath entry <input/> element
 * @param {String} previewElementId ID of the XHTML element that will contain the
 *   resulting MathML preview, replacing any existing child Nodes.
 */
function updatePreviewIfChanged(inputBoxId, previewElementId) {
    var inputSelector = jQuery("#" + inputBoxId);
    var newValue = inputSelector.get(0).value;
    var oldValue = inputTextByIdMap[inputBoxId];
    if (oldValue==null || newValue!=oldValue) {
        updatePreview(newValue, previewElementId);
    }
    inputTextByIdMap[inputBoxId] = newValue;
}

/**
 * Extracts the MathML contained within the ASCIIMath preview element
 * having the given ID, storing the results in the given hidden form
 * element.
 *
 * @param {String} previewElementId ID of the XHTML parent element
 *   containing the MathML to be extracted.
 * @param {String} formElementId ID of the <input/> element to store
 *   the resulting serialized MathML.
 */
function extractMathML(previewElementId, formElementId) {
    var previewElement = document.getElementById(previewElementId);
    var mathNode = previewElement.getElementsByTagName("math")[0];
    var cleanedMathML = AMnode2string(mathNode, "");

    var hiddenFormElement = document.getElementById(formElementId);
    hiddenFormElement.value = cleanedMathML;
}

/**
 * Sets up an ASCIIMathML input using the elements provided, binding
 * the appropriate event handlers to make everything work correctly.
 *
 * @param {String} inputBoxId ID of the ASCIIMath entry <input/> element
 * @param {String} previewElementId ID of the XHTML element that will be
 *   used to hold the resulting MathML preview. Note that all of its child
 *   Nodes will be removed.
 * @param {String} mathmlResultElementId ID of the hidden <input/> field that will
 *   hold the resulting MathML on submission.
 */
function setupASCIIMathMLInput(inputBoxId, previewElementId, mathmlResultElementId) {
    /* Set up submit handler for the form */
    jQuery("#" + inputBoxId).closest("form").bind("submit", function(evt) {
        extractMathML(previewElementId, mathmlResultElementId);
        return true;
    });
    var inputSelector = jQuery("#" + inputBoxId);
    var initialInput = inputSelector.get(0).value;

    /* Set up initial preview */
    updatePreview(initialInput, previewElementId, true);

    /* Set up handler to update preview when required */
    inputSelector.bind("change keyup keydown", function(evt) {
        updatePreviewIfChanged(inputBoxId, previewElementId);
    });

    /* TODO: Do we want to set up a timer as well? If so, we probably want
     * one to be global to a page, rather than each interaction.
     */
}
