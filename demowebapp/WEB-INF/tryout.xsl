<?xml version="1.0"?>
<!--

$Id: header.jspf 2712 2008-03-10 17:01:01Z davemckain $

Stylesheet to apply UoE headers and footers to SnuggleTeX web page output

Copyright (c) 2008 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="html">

  <xsl:import href="/WEB-INF/webpage.xsl"/>

  <!-- LaTeX input - this will be put into a textarea -->
  <xsl:param name="latex-input"/>

  <xsl:template match="html:body" mode="make-content">
    <h1>SnuggleTeX - Try It Out</h1>

    <!-- Now do input form -->
    <h2>Input</h2>
    <p>
      Enter some LaTeX into the box below and hit <tt>Go!</tt> to see the resulting
      XHTML+MathML.
    </p>
    <form method="POST" action="{$context-path}/tryout.xml">
      <textarea id="input" name="input" style="width:100%" rows="20">
        <xsl:value-of select="$latex-input"/>
      </textarea>
      <input type="submit" value="Go!" />
    </form>

    <!-- Output -->
    <h2>Output </h2>
    <xsl:copy-of select="node()"/>
  </xsl:template>

</xsl:stylesheet>
