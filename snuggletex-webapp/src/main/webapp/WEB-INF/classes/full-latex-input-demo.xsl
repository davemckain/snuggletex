<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
the full LaTeX input demo.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="m xs">

  <xsl:import href="demo-utilities.xsl"/>
  <xsl:import href="format-output.xsl"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'fullLaTeXInputDemo'" as="xs:string"/>

  <!-- LaTeX input - this will be put into a textarea -->
  <xsl:param name="latex-input" as="xs:string" required="yes"/>

  <xsl:template match="body" mode="make-content">
    <!-- Do input form -->
    <h3>Input</h3>
    <p>
      This demo lets you enter a chunk of LaTeX for SnuggleTeX to convert into
      XHTML and MathML.
    </p>
    <p>
      Simply enter some LaTeX into the box below and hit <tt>Go!</tt> to see
      the results.
    </p>
    <form method="post" class="input">
      <textarea id="inputBox" name="input" style="width:100%" rows="20">
        <xsl:value-of select="$latex-input"/>
      </textarea>
      <input type="submit" value="Go!" />
      <input type="button" value="Clear Form" onclick="document.getElementById('inputBox').value=''" />
    </form>

    <!-- Output -->
    <h3>Output </h3>
    <xsl:call-template name="maybe-make-mathml-legacy-output-warning"/>
    <div class="result">
      <xsl:copy-of select="node()"/>
    </div>
  </xsl:template>

</xsl:stylesheet>
