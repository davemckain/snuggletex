/*
 * FIXME: Document this!
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
 * Copyright (c) 2009 The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

/**
 * Hacked version of AMdisplay() from ASCIIMathMLeditor.js that allows
 * us to specify which element to display the resulting MathML
 * in and where the raw input is going to come from.
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
    LMprocessNode(outnode, true);
    AMprocessNode(outnode, true);
}

/* Simple hash that will keep track of the current value of each
 * ASCIIMathML input box, keyed on its ID. This is used by
 * updatePreviewIfChanged() to determine whether the MathML preview
 * should be updated or not.
 */
var inputTextByIdMap = {};

function updatePreviewIfChanged(inputBoxId, previewElementId) {
    var inputSelector = $j("#" + inputBoxId);
    var newValue = inputSelector.get(0).value;
    var oldValue = inputTextByIdMap[inputBoxId];
    if (oldValue==null || newValue!=oldValue) {
        updatePreview(newValue, previewElementId);
    }
    inputTextByIdMap[inputBoxId] = newValue;
}

function extractMathML(previewElementId, formElementId) {
    var previewElement = document.getElementById(previewElementId);
    var mathNode = previewElement.getElementsByTagName("math")[0];
    var cleanedMathML = AMnode2string(mathNode, "");

    var hiddenFormElement = document.getElementById(formElementId);
    hiddenFormElement.value = cleanedMathML;
}

function setupASCIIMathMLInput(inputBoxId, mathmlFieldId, previewElementId) {
    /* Set up submit handler for the form */
    $j("#" + inputBoxId).closest("form").bind("submit", function(evt) {
        extractMathML(previewElementId, mathmlFieldId);
        return true;
    });
    var inputSelector = $j("#" + inputBoxId);
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
