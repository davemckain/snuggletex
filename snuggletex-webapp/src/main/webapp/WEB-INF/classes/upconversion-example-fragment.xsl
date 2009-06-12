<?xml version="1.0"?>
<!--

$Id: upconversion-demo.xsl 371 2009-05-25 21:11:40Z davemckain $

Formats the little fragment containing up-conversion example results.

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

  <xsl:import href="demo-utilities.xsl"/>

  <!-- FIXME: These should move to a base stylesheet as they're needed everywhere -->
  <xsl:param name="snuggletex-version" as="xs:string" required="yes"/>
  <xsl:param name="maven-site-url" as="xs:string" required="yes"/>
  <xsl:param name="context-path" as="xs:string" required="yes"/>

  <xsl:param name="latex-input" as="xs:string" required="yes"/>
  <xsl:param name="is-bad-input" as="xs:boolean" required="yes"/>
  <xsl:param name="parsing-errors" as="element(s:error)*"/>
  <xsl:param name="parallel-mathml" as="xs:string?"/>
  <xsl:param name="pmathml-initial" as="xs:string?"/>
  <xsl:param name="pmathml-upconverted" as="xs:string?"/>
  <xsl:param name="cmathml" as="xs:string?"/>
  <xsl:param name="maxima-input" as="xs:string?"/>

  <xsl:template match="/">
    <div class="exampleResult">
      <xsl:choose>
        <xsl:when test="$is-bad-input">
          <!-- Bad input -->
          <xsl:call-template name="handle-successful-input"/>
        </xsl:when>
        <xsl:when test="exists($parsing-errors)">
          <!-- SnuggleTeX Parsing Error(s) -->
          <xsl:call-template name="handle-failed-input"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Successful Parsing -->
          <xsl:call-template name="handle-successful-input"/>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="handle-successful-input">
    <h3>Enhanced Presentation MathML</h3>
    <pre class="result">
      <xsl:value-of select="$pmathml-upconverted"/>
    </pre>

    <h3>Content MathML</h3>
    <xsl:variable name="content-failures" as="element(s:fail)*" select="/m:math/m:semantics/m:annotation-xml[@encoding='MathML-Content-upconversion-failures']/*"/>
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
    <xsl:variable name="maxima-failures" as="element(s:fail)*" select="/m:math/m:semantics/m:annotation-xml[@encoding='Maxima-upconversion-failures']/*"/>
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

  <!-- Show SnuggleTeX failure details. -->
  <xsl:template name="handle-failed-input">
    <h3>Result: LaTeX Errors in Input</h3>
    <p>
      The input LaTeX contained errors as follows:
    </p>
    <xsl:call-template name="format-parsing-errors">
      <xsl:with-param name="parsing-errors" select="$parsing-errors"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Show bad input details -->
  <xsl:template name="handle-bad-input">
    <h3>Result: Bad Input</h3>
    <p>
      The input LaTeX was not successfully parsed as Math Mode input.
    </p>
  </xsl:template>

</xsl:stylesheet>
