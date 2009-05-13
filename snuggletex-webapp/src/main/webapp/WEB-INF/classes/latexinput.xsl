<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
demonstrating LaTeX -> Presentation MathML -> Content MathML -> Maxima.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="h xs">

  <!-- Import basic formatting stylesheet -->
  <xsl:import href="format-output.xsl"/>

  <!-- LaTeX input - this will be put into a textarea -->
  <xsl:param name="latex-input" as="xs:string" required="yes"/>

  <!-- Various text outputs -->
  <xsl:param name="mathml" as="xs:string" select="'(Failed)'" required="no"/>
  <xsl:param name="maxima-input" as="xs:string" select="'(Failed)'" required="no"/>
  <xsl:param name="maxima-output" as="xs:string" select="'(Failed)'" required="no"/>
  <xsl:param name="maxima-mathml-output" as="xs:string" select="'(Failed)'" required="no"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'latexinput'" as="xs:string"/>

  <!-- Override title -->
  <xsl:variable name="title" select="'LaTeX Conversion Demo'" as="xs:string"/>

  <xsl:template match="h:body" mode="make-content">
    <h2><xsl:value-of select="$title"/></h2>

    <!-- Now do input form -->
    <h3>Input</h3>
    <p>
      Enter a LaTeX math mode expression
      into the box below and hit <tt>Go!</tt> to see the resulting outputs.
    </p>
    <form method="POST" id="inputForm">
        LaTeX Math Mode Input: \[ <input id="input" name="input" type="text" value="{$latex-input}"/> \]
        <input type="submit" value="Go!" />
        <input type="button" value="Clear" onclick="document.getElementById('input').value=''" />
    </form>

    <!-- Output -->
    <h3>Presentation MathML Rendered by Browser</h3>
    <div class="result">
      <xsl:copy-of select="node()"/>
    </div>

    <h3>Up-converted MathML</h3>
    <pre class="result">
      <xsl:value-of select="$mathml"/>
    </pre>

    <h3>Maxima Input</h3>
    <pre class="result">
      <xsl:value-of select="$maxima-input"/>
    </pre>

    <h3>(Raw) Maxima Output</h3>
    <pre class="result">
      <xsl:value-of select="$maxima-output"/>
    </pre>

    <h3>Up-converted Maxima MathML Output</h3>
    <pre class="result">
      <xsl:value-of select="$maxima-mathml-output"/>
    </pre>

  </xsl:template>

</xsl:stylesheet>
