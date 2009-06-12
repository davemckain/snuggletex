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
    /* Attach handlers to dialog popups links */
    $(".dialog").bind("click", function(e) {
        var popup = $("#popup");
        popup.empty();
        popup.load(this.href);
        popup.dialog({
          title: 'Example',
          width: 500,
          height: 300
        });
        return false;
    });
});
