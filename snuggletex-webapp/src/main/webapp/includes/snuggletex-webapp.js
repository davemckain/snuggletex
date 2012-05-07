/* Random JavaScript for the SnuggleTeX webapp
 *
 * Prerequisites:
 *
 * jquery.js
 *
 * $Id$
 *
 * Copyright (c) 2008-2011, The University of Edinburgh.
 * All Rights Reserved
 */

/* ============================================================ */

var popup = null;

jQuery(document).ready(function() {
    /* Attach handlers to dialog popups links for example links */
    jQuery(".dialog").bind("click", function() {
        var latexInput = this.getAttribute('title');
        var popup = jQuery("#popup");
        popup.load(this.href + " .exampleResult", null, function() {
            jQuery(".exampleResult").tabs();
            popup.dialog({
                title: 'Example: ' + latexInput,
                width: 600,
                height: 400
            });
        });
        return false;
    });
});
