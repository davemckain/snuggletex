/*
 * This provides some basic code for managing the ASCIIMathML input widget
 * used in the ASCIIMathML input demo in SnuggleTeX.
 *
 * The general ideas may be useful in other scenarios, so feel free to use
 * and/or build on this as is necessary.
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
 * jquery.js (at least version 1.5.0)
 *
 * Author: David McKain
 *
 * $Id:web.xml 158 2008-07-31 10:48:14Z davemckain $
 *
 * Copyright (c) 2008-2011, The University of Edinburgh
 * All Rights Reserved
 */

/************************************************************/

/* (Reset certain defaults chosen by ASCIIMathML) */
var mathcolor = "";
var mathfontfamily = "";

/************************************************************/

var ASCIIMathInputController = (function() {

    var validatorServiceUrl = null; /* Caller must fill in */
    var delay = 300;
    var newline = "\r\n";
    var doMathJax = true;

    /************************************************************/
    /* ASCIIMath calling helpers */

    var callASCIIMath = function(mathModeInput) {
        /* Escape use of backquote symbol to prevent exiting math mode */
        mathModeInput = mathModeInput.replace(/`/g, "\\`");

        var span = AMparseMath(mathModeInput); // This is <span><math>...</math></span>
        return span.childNodes[0]; /* This is <math>...</math> */
    };

    /**
     * Extracts the source MathML contained within the ASCIIMath-generated
     * <math> element.
     */
    var extractMathML = function(asciiMathElement) {
        return AMnode2string(asciiMathElement, "")
            .substring(newline.length); /* Trim off leading newline */
    };

    /* Fixed up version of the function of the same name in ASCIIMathMLeditor.js,
     * with the following changes:
     *
     * * Used newline variable for line breaks
     * * Attribute values are escape correctly
     */
    var AMnode2string = function(inNode, indent) {
        var i, str = "";
        if (inNode.nodeType == 1) {
            var name = inNode.nodeName.toLowerCase(); // (IE fix)
            str = newline + indent + "<" + name;
            for (i=0; i < inNode.attributes.length; i++) {
                var attrValue = inNode.attributes[i].nodeValue;
                if (attrValue!="italic" &&
                        attrValue!="" &&  //stop junk attributes
                        attrValue!="inherit" && // (mostly IE)
                        attrValue!=undefined) {
                    str += " " + inNode.attributes[i].nodeName
                        + "=\"" + AMescapeValue(inNode.attributes[i].nodeValue) + "\"";
                }
            }
            if (name == "math") str += " xmlns=\"http://www.w3.org/1998/Math/MathML\"";
            str += ">";
            for(i=0; i<inNode.childNodes.length; i++) {
                str += AMnode2string(inNode.childNodes[i], indent+"  ");
            }
            if (name != "mo" && name != "mi" && name != "mn") {
                str += newline + indent;
            }
            str += "</" + name + ">";
        }
        else if (inNode.nodeType == 3) {
            str += AMescapeValue(inNode.nodeValue);
        }
        return str;
    };

    var AMescapeValue = function(value) {
        var str = "";
        for (i=0; i<value.length; i++) {
            if (value.charCodeAt(i)<32 || value.charCodeAt(i)>126) str += "&#"+value.charCodeAt(i)+";";
            else if (value.charAt(i)=="<") str += "&lt;";
            else if (value.charAt(i)==">") str += "&gt;";
            else if (value.charAt(i)=="&") str += "&amp;";
            else str += value.charAt(i);
        }
        return str;
    };

    /************************************************************/
    /* Utility helpers */

    var replaceContainerContent = function(containerQuery, content) {
        containerQuery.empty();
        if (content!=null) {
            containerQuery.append(content);

            /* Maybe schedule MathJax update if this is a MathML Element */
            if (doMathJax && content instanceof Element && content.nodeType==1 && content.nodeName=="math") {
                MathJax.Hub.Queue(["Typeset", MathJax.Hub, containerQuery.get(0)]);
            }
        }
    };


    /************************************************************/

    var Widget = function(_asciiMathInputId, _asciiMathOutputId) {
        this.asciiMathInputControlId = _asciiMathInputId;
        this.asciiMathOutputControlId = _asciiMathOutputId;
        this.mathJaxRenderingContainerId = null;
        this.validatedRenderingContainerId = null;
        this.pmathSourceContainerId = null;
        this.cmathSourceContainerId = null;
        this.maximaSourceContainerId = null;
        var lastInput = null;
        var currentXHR = null;
        var currentTimeoutId = null;
        var widget = this;

        this.getASCIIMathInput= function() {
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            return inputSelector.get(0).value;
        };

        /**
         * Checks the content of the <input/> element having the given asciiMathInputControlId,
         * and calls {@link #updatePreview} if its contents have changed since the
         * last call to this.
         */
        this.updatePreviewIfChanged = function() {
            var asciiMathInput = this.getASCIIMathInput();
            if (lastInput==null || asciiMathInput!=lastInput) {
                /* Something has changed */
                lastInput = asciiMathInput;
                if (currentTimeoutId!=null) {
                    window.clearTimeout(currentTimeoutId);
                }
                else {
                    updateValidationContainer(0); /* Show waiting animation */
                }
                currentTimeoutId = window.setTimeout(function() {
                    widget.updatePreview();
                    currentTimeoutId = null;
                }, delay);
            }
        };

        /**
         * Hacked version of AMdisplay() from ASCIIMathMLeditor.js that allows
         * us to specify which element to display the resulting MathML
         * in and where the raw input is going to come from.
         */
        this.updatePreview = function() {
            /* Get ASCIIMathML to generate a <math> element */
            var asciiMathElement = callASCIIMath(this.getASCIIMathInput());
            var source = extractMathML(asciiMathElement);

            /* Maybe update preview source box */
            if (this.pmathSourceContainerId!=null) {
                jQuery("#" + this.pmathSourceContainerId).text(source);
            }

            /* Maybe insert MathML into the DOM for display */
            if (this.mathJaxRenderingContainerId!=null) {
                replaceContainerContent(jQuery("#" + this.mathJaxRenderingContainerId),
                    asciiMathElement);
            }

            /* Maybe validate the input */
            if (this.validatedRenderingContainerId!=null && validatorServiceUrl!=null) {
                currentXHR = jQuery.ajax({
                    type: 'POST',
                    url: validatorServiceUrl,
                    dataType: 'json',
                    data: source,
                    success: function(data, textStatus, jqXHR) {
                        if (currentXHR==jqXHR) {
                            currentXHR = null;
                            showValidationResult(data);
                        }
                    }
                });
            }
        };

        var showValidationResult = function(jsonData) {
            var validatedRenderingContainer = jQuery("#" + widget.validatedRenderingContainerId);

            /* We consider "valid" to mean "getting as far as CMathML" here */
            var cmath = jsonData['cmath'];
            if (cmath!=null) {
                updateValidationContainer(Widget.STATUS_SUCCESS, jsonData['pmathBracketed']);
            }
            else if (jsonData['cmathFailures']!=null) {
                updateValidationContainer(Widget.STATUS_FAILURE);
            }
            else {
                updateValidationContainer(Widget.STATUS_ERROR);
            }

            /* Maybe show CMath source */
            if (widget.cmathSourceContainerId!=null) {
                jQuery("#" + widget.cmathSourceContainerId).text(cmath);
            }
            /* Maybe show Maxima is we got it */
            if (widget.maximaSourceContainerId!=null) {
                jQuery("#" + widget.maximaSourceContainerId).text(jsonData['maxima'] || 'Could not get Maxima');
            }
        };

        var updateValidationContainer = function(status, mathmlString) {
            if (widget.validatedRenderingContainerId!=null) {
                var validatedRenderingContainer = jQuery("#" + widget.validatedRenderingContainerId);
                /* Set up children if not done already */
                if (validatedRenderingContainer.children().size()==0) {
                    validatedRenderingContainer.html("<div class='asciiMathWidgetStatus'></div>"
                        + "<div class='asciiMathWidgetMessage'></div>"
                        + "<div class='asciiMathWidgetResult'></div>");
                }
                var statusContainer = validatedRenderingContainer.children().first();
                var messageContainer = statusContainer.next();
                var resultContainer = messageContainer.next();
                switch(status) {
                    case Widget.STATUS_WAITING:
                        statusContainer.attr('class', 'asciiMathWidgetStatus waiting');
                        replaceContainerContent(messageContainer, "Checking...");
                        replaceContainerContent(resultContainer, "\xa0");
                        break;

                    case Widget.STATUS_SUCCESS:
                        statusContainer.attr('class', 'asciiMathWidgetStatus success');
                        replaceContainerContent(messageContainer, "Your input makes sense. It has been interpreted as:");
                        replaceContainerContent(resultContainer, jQuery.parseXML(mathmlString).childNodes[0]);
                        break;

                    case Widget.STATUS_FAILURE:
                        statusContainer.attr('class', 'asciiMathWidgetStatus failure');
                        replaceContainerContent(messageContainer, "I could not understand your input");
                        replaceContainerContent(resultContainer, "\xa0");
                        break;

                    case Widget.STATUS_ERROR:
                        statusContainer.attr('class', 'asciiMathWidgetStatus error');
                        replaceContainerContent(messageContainer, "Unexpected error");
                        replaceContainerContent(resultContainer, "\xa0");
                        break;
                }
            }
        };

        this.doInit = function() {
            /* Set up submit handler for the form */
            var inputSelector = jQuery("#" + this.asciiMathInputControlId);
            inputSelector.closest("form").bind("submit", function(evt) {
                /* We'll redo the ASCIIMathML process, just in case we want to allow auto-preview to be disabled in future */
                var asciiMathInput = inputSelector.get(0).value;
                var asciiMathElement = callASCIIMath(asciiMathInput);
                var asciiMathSource = extractMathML(asciiMathElement);
                var mathmlResultControl = document.getElementById(widget.asciiMathOutputControlId);
                mathmlResultControl.value = asciiMathSource;
                return true;
            });

            /* Set up initial preview */
            widget.updatePreview();

            /* Set up handler to update preview when required */
            inputSelector.bind("change keyup keydown", function() {
                widget.updatePreviewIfChanged();
            });
        };

    };

    Widget.prototype.setMathJaxRenderingContainerId = function(id) {
        this.mathJaxRenderingContainerId = id;
    };

    Widget.prototype.init = function() {
        this.doInit();
    };

    Widget.STATUS_WAITING = 0;
    Widget.STATUS_SUCCESS = 1;
    Widget.STATUS_FAILURE = 2;
    Widget.STATUS_ERROR = 3;

    return {
        createInputWidget: function(inputId, outputId) {
            return new Widget(inputId, outputId);
        },

        getValidatorServiceUrl: function() { return validatorServiceUrl },
        setValidatorServiceUrl: function(url) { validatorServiceUrl = url },

        getDelay: function() { return delay },
        setDelay: function(newDelay) { delay = newDelay },
    };

})();
