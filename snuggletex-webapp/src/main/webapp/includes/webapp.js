/* Random JavaScript for the SnuggleTeX webapp
 *
 * Prerequisites:
 *
 * jquery.js
 *
 * $Id$
 *
 * Copyright (c) 2009 University of Edinburgh.
 * All Rights Reserved
 */

/* ============================================================ */

var popup = null;

$(document).ready(function() {
    /* Set up dialog box */
    popup = $("#popup");
    popup.dialog({
      autoOpen: false,
      title: 'Example',
      width: 600,
      height: 400
    });
    /* Attach handlers to dialog popups links */
    $(".dialog").bind("click", function(event) {
        var latexInput = this.getAttribute('title');
        popup.load(this.href + " .exampleResult", null, function() {
            $(".exampleResult").tabs();
            popup.dialog('option', 'title', 'Example: ' + latexInput);
            popup.dialog('open');
        });
        return false;
    });
});
