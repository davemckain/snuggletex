<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
doing a simple Math Input -> MathML web page result.

Copyright (c) 2009 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:mu="ext://uk.ac.ed.ph.snuggletex.utilities.MathMLUtilities"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs m s mu">

  <!-- Import basic formatting stylesheet -->
  <xsl:import href="format-output.xsl"/>
  <xsl:import href="demo-utilities.xsl"/>

  <xsl:param name="mathml-capable" as="xs:boolean" required="yes"/>
  <xsl:param name="latex-input" as="xs:string" required="yes"/>
  <xsl:param name="add-annotations" as="xs:boolean" required="yes"/>
  <xsl:param name="is-bad-input" as="xs:boolean" required="yes"/>
  <xsl:param name="parsing-errors" as="element(s:error)*"/>
  <xsl:param name="result-mathml" as="xs:string?"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'mathInputDemo'" as="xs:string"/>

  <!-- Override title -->
  <xsl:variable name="title" select="'Simple Math Input Demo'" as="xs:string"/>

  <xsl:template match="body" mode="make-content">
    <h2><xsl:value-of select="$title"/></h2>

    <!-- Now do input form -->
    <h3>Input</h3>
    <p>
      Enter a LaTeX math mode expression
      into the box below and hit <tt>Go!</tt> to see the resulting output and MathML.
    </p>
    <form method="POST" id="inputForm">
      <div>
        LaTeX Math Mode Input: \[ <input id="input" name="input" type="text" value="{$latex-input}"/> \]
        <input type="submit" value="Go!" />
        <input type="button" value="Clear" onclick="document.getElementById('input').value=''" />
      </div>
      <div>
        <input type="checkbox" id="annotate" name="annotate">
          <xsl:if test="$add-annotations">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
        </input>
        <label for="annotate">Annotate MathML with input LaTeX</label>
      </div>
    </form>
    <xsl:choose>
      <xsl:when test="$is-bad-input">
        <!-- Bad input -->
        <xsl:apply-templates select="." mode="handle-bad-input"/>
      </xsl:when>
      <xsl:when test="exists($parsing-errors)">
        <!-- SnuggleTeX Parsing Error(s) -->
        <xsl:apply-templates select="." mode="handle-failed-input"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Successful Parsing -->
        <xsl:apply-templates select="." mode="handle-successful-input"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  This template shows the details when the input has been
  successfully passed through SnuggleTeX, though with the
  possibility that up-conversion has not been entirely successful.
  -->
  <xsl:template match="body" mode="handle-successful-input">
    <xsl:choose>
      <xsl:when test="$mathml-capable">
        <h3>MathML Output (rendered by your browser)</h3>
        <div class="result">
          <xsl:copy-of select="node()"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <h3>MathML Output (converted to an image)</h3>
        <p>
          (Your browser does not support MathML so SnuggleTeX has converted the result
          to an image instead.)
        </p>
        <div class="result">
          <div class="mathml-math">
            <img src="{$context-path}/MathInputToImage.png?input={encode-for-uri($latex-input)}"
              alt="{$latex-input}" />
          </div>
        </div>
      </xsl:otherwise>
    </xsl:choose>

    <h3>MathML Source</h3>
    <pre class="result">
      <xsl:value-of select="$result-mathml"/>
    </pre>
  </xsl:template>

  <!-- Show SnuggleTeX failure details. -->
  <xsl:template match="body" mode="handle-failed-input">
    <h3>Result: LaTeX Errors in Input</h3>
    <p>
      The input you entered contained LaTeX errors as follows:
    </p>
    <xsl:call-template name="format-parsing-errors">
      <xsl:with-param name="parsing-errors" select="$parsing-errors"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Show bad input details -->
  <xsl:template match="body" mode="handle-bad-input">
    <h3>Result: Bad Input</h3>
    <p>
      Sorry, your input was not successfully parsed as Math Mode input.
    </p>
  </xsl:template>

</xsl:stylesheet>
