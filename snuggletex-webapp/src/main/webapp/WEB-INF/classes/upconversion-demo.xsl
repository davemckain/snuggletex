<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
demonstrating LaTeX -> Presentation MathML -> Content MathML -> Maxima
up-covnersion process.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs">

  <!-- Import basic formatting stylesheet -->
  <xsl:import href="format-output.xsl"/>

  <xsl:param name="latex-input" as="xs:string" required="yes"/>
  <xsl:param name="is-parsing-success" as="xs:boolean" required="yes"/>
  <xsl:param name="parallel-mathml" as="xs:string?"/>
  <xsl:param name="pmathml-initial" as="xs:string?"/>
  <xsl:param name="pmathml-upconverted" as="xs:string?"/>
  <xsl:param name="cmathml" as="xs:string?"/>
  <xsl:param name="maxima-input" as="xs:string?"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'latexinput'" as="xs:string"/>

  <!-- Override title -->
  <xsl:variable name="title" select="'MathML Up-Conversion Demo'" as="xs:string"/>

  <xsl:template match="body" mode="make-content">
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
      <xsl:value-of select="$parallel-mathml"/>
    </pre>

    <h3>Initial PMathML</h3>
    <pre class="result">
      <xsl:value-of select="$pmathml-initial"/>
    </pre>

    <h3>Up-Converted PMathML</h3>
    <pre class="result">
      <xsl:value-of select="$pmathml-upconverted"/>
    </pre>

    <h3>Content MathML</h3>
    <pre class="result">
      <xsl:value-of select="$cmathml"/>
    </pre>

    <h3>Maxima Input Form</h3>
    <pre class="result">
      <xsl:value-of select="$maxima-input"/>
    </pre>
  </xsl:template>

</xsl:stylesheet>
