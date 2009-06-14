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

$(document).ready(function() {
    /* Set up dialog box */
    $("#popup").dialog({
      autoOpen: false,
      title: 'Example',
      width: 600,
      height: 400
    });
    /* Attach handlers to dialog popups links */
    $(".dialog").bind("click", function(event) {
        /* FIXME: The following code only lets the dialog appear once. Read the docs when back online! */
        var popup = $("#popup");
        popup.empty();
        popup.load(this.href);
        popup.dialog('option', 'title', 'Example: ' + this.getAttribute('title'));
        popup.dialog('open');
        return false;
    });
});
