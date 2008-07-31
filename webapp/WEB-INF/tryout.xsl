<?xml version="1.0"?>
<!--

$Id: header.jspf 2712 2008-03-10 17:01:01Z davemckain $

Overrides format-output.xsl to add in functionality for
the "try out" page.

Copyright (c) 2008 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="h xs">

  <!-- Import basic formatting stylesheet -->
  <xsl:import href="/WEB-INF/format-output.xsl"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'tryout'" as="xs:string"/>

  <!-- Override title -->
  <xsl:variable name="title" select="'Try Out'" as="xs:string"/>

  <!-- LaTeX input - this will be put into a textarea -->
  <xsl:param name="latex-input" as="xs:string" required="yes"/>

  <xsl:template match="h:body" mode="make-content">
    <h2><xsl:value-of select="$title"/></h2>

    <!-- Now do input form -->
    <h3>Input</h3>
    <p>
      Enter some LaTeX into the box below and hit <tt>Go!</tt> to see the resulting
      XHTML+MathML.
    </p>
    <form method="POST" action="{$context-path}/tryout.xml" id="inputForm">
      <textarea id="input" name="input" style="width:100%" rows="20">
        <xsl:value-of select="$latex-input"/>
      </textarea>
      <input type="submit" value="Go!" />
      <input type="button" value="Clear Form" onclick="document.getElementById('input').value=''" />
    </form>

    <!-- Output -->
    <h3>Output </h3>
    <div class="tryoutOutput">
      <xsl:copy-of select="node()"/>
    </div>
  </xsl:template>

</xsl:stylesheet>
