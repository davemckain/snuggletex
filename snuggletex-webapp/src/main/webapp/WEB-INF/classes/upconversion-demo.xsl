<?xml version="1.0"?>
<!--

$Id$

Overrides format-output.xsl to add in functionality for
demonstrating LaTeX -> Presentation MathML -> Content MathML -> Maxima
up-conversion process.

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

  <xsl:param name="latex-input" as="xs:string" required="yes"/>
  <xsl:param name="is-bad-input" as="xs:boolean" required="yes"/>
  <xsl:param name="parsing-errors" as="element(s:error)*"/>
  <xsl:param name="parallel-mathml" as="xs:string?"/>
  <xsl:param name="pmathml-initial" as="xs:string?"/>
  <xsl:param name="pmathml-upconverted" as="xs:string?"/>
  <xsl:param name="cmathml" as="xs:string?"/>
  <xsl:param name="maxima-input" as="xs:string?"/>

  <!-- Override page ID -->
  <xsl:variable name="pageId" select="'upConversionDemo'" as="xs:string"/>

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
    <h3>MathML Rendered by Browser</h3>
    <div class="result">
      <xsl:copy-of select="node()"/>
    </div>

    <h3>Raw Presentation MathML</h3>
    <pre class="result">
      <xsl:value-of select="$pmathml-initial"/>
    </pre>

    <h3>Enhanced Presentation MathML</h3>
    <pre class="result">
      <xsl:value-of select="$pmathml-upconverted"/>
    </pre>

    <h3>Content MathML</h3>
    <xsl:variable name="content-failures" as="element(s:fail)*" select="m:math/m:semantics/m:annotation-xml[@encoding='MathML-Content-upconversion-failures']/*"/>
    <xsl:choose>
      <xsl:when test="exists($content-failures)">
        <p>
          The conversion from Presentation MathML to Content MathML was not successful
          for this input.
        </p>
        <xsl:call-template name="format-upconversion-failures">
          <xsl:with-param name="failures" select="$content-failures"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <pre class="result">
          <xsl:value-of select="$cmathml"/>
        </pre>
      </xsl:otherwise>
    </xsl:choose>

    <h3>Maxima Input Form</h3>
    <xsl:variable name="maxima-failures" as="element(s:fail)*" select="m:math/m:semantics/m:annotation-xml[@encoding='Maxima-upconversion-failures']/*"/>
    <xsl:choose>
      <xsl:when test="exists($content-failures)">
        <p>
          Conversion to Maxima Input is reliant on the conversion to Content MathML
          being successful, which was not the case here.
        </p>
      </xsl:when>
      <xsl:when test="exists($maxima-failures)">
        <p>
          The conversion from Content MathML to Maxima Input was not successful for
          this input.
        </p>
        <xsl:call-template name="format-upconversion-failures">
          <xsl:with-param name="failures" select="$maxima-failures"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <pre class="result">
          <xsl:value-of select="$maxima-input"/>
        </pre>
      </xsl:otherwise>
    </xsl:choose>

    <h3>Fully annotated MathML Element</h3>
    <pre class="result">
      <xsl:value-of select="$parallel-mathml"/>
    </pre>
  </xsl:template>

  <xsl:template name="format-upconversion-failures">
    <xsl:param name="failures" as="element(s:fail)+"/>
    <table class="failures">
      <thead>
        <tr>
          <th>Failure Code</th>
          <th>Message</th>
          <th>Context</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="$failures">
          <tr>
            <td>
              <xsl:call-template name="make-error-code-link">
                <xsl:with-param name="error-code" select="@code"/>
              </xsl:call-template>
            </td>
            <td><xsl:value-of select="@message"/></td>
            <td>
              <pre>
                <!--
                We'll strip off the enclosing <s:fail/>, which also conveniently
                removes namespace declarations.
                -->
                <xsl:value-of select="replace(
                  replace(
                    replace(mu:serializeElement(s:context), '^&lt;s:context.+?&gt; ?[\n\r]+', '', 's'),
                    '[\n\r]+ ?&lt;/s:context&gt;$', '', 's'),
                    '^   ', '', 'm') (: de-indent 1 level :)
                  "/>
              </pre>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
  </xsl:template>

  <!--
  Show SnuggleTeX failure details.
  -->
  <xsl:template match="body" mode="handle-failed-input">
    <h3>Result: LaTeX Errors in Input</h3>
    <p>
      The input you entered contained LaTeX errors as follows:
    </p>
    <table class="failures">
      <thead>
        <tr>
          <th>Error Code</th>
          <th>Message</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="$parsing-errors">
          <tr>
            <td>
              <xsl:call-template name="make-error-code-link">
                <xsl:with-param name="error-code" select="@code"/>
              </xsl:call-template>
            </td>
            <td>
              <pre>
                <xsl:value-of select="."/>
              </pre>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
  </xsl:template>

  <!--
  Show bad input details
  -->
  <xsl:template match="body" mode="handle-bad-input">
    <h3>Result: Bad Input</h3>
    <p>
      Sorry, your input was not successfully parsed as Math Mode input.
    </p>
  </xsl:template>

  <xsl:template name="make-error-code-link">
    <xsl:param name="error-code" as="xs:string"/>
    <a href="{$context-path}/documentation/error-codes.html#{$error-code}">
      <xsl:value-of select="$error-code"/>
    </a>
  </xsl:template>


</xsl:stylesheet>
