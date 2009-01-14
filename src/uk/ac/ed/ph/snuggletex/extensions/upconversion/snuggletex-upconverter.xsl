<!--

$Id$

This stylesheet controls the up-conversion of Presentation MathML
within the results of SnuggleTeX documents into other forms,
supporting various types of MathML results.

This may be applied to an entire XHTML + MathML document.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <!-- ************************************************************ -->

  <xsl:import href="pmathml-enhancer.xsl"/>
  <xsl:import href="pmathml-to-cmathml.xsl"/>
  <xsl:import href="cmathml-to-maxima.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <!-- ************************************************************ -->

  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="math">
    <!-- Extract the actual PMathML content and any existing annotations -->
    <xsl:variable name="presentation-mathml" select="if (semantics) then (if (semantics/mrow) then semantics/mrow/* else semantics/*[1]) else *" as="element()*"/>
    <xsl:variable name="annotations" select="if (semantics) then semantics/*[position() != 1] else ()" as="element()*"/>
    <!-- Perform enhancement of the Presentation MathML, creating a new Document Node -->
    <xsl:variable name="enhanced-pmathml">
      <xsl:call-template name="s:enhance-pmathml">
        <xsl:with-param name="elements" select="$presentation-mathml"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- Convert Presentation MathML to Content MathML, creating another new Document Node -->
    <xsl:variable name="cmathml">
      <xsl:call-template name="s:pmathml-to-cmathml">
        <xsl:with-param name="elements" select="$enhanced-pmathml/*"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- Extract any failures in this process -->
    <xsl:variable name="cmathml-failures" as="element(s:fail)*">
      <xsl:copy-of select="$cmathml//s:fail"/>
    </xsl:variable>
    <!-- Convert Content MathML to Maxima input. (This is normally a sequence of xs:string
         but might contain failure elements as well so we need to be careful) -->
    <xsl:variable name="maxima-raw" as="item()*">
      <xsl:choose>
        <xsl:when test="exists($cmathml-failures)">
          <!-- Don't bother converting if we failed earlier on -->
          <xsl:sequence select="()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="s:cmathml-to-maxima">
            <xsl:with-param name="elements" select="$cmathml/*"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Extract any failures arising here -->
    <xsl:variable name="maxima-failures" as="element(s:fail)*">
      <xsl:copy-of select="$maxima-raw/descendant-or-self::s:fail"/>
    </xsl:variable>
    <!-- Formulate the final Maxima string -->
    <xsl:variable name="maxima" as="xs:string?"
      select="if (exists($maxima-failures)) then () else string-join($maxima-raw, '')"/>

    <!-- Finally build up the resulting MathML -->
    <math>
      <xsl:copy-of select="@*"/>
      <semantics>
        <!-- Put in the enhanced Presentation MathML first -->
        <xsl:call-template name="s:maybe-wrap-in-mrow">
          <xsl:with-param name="elements" select="$enhanced-pmathml/*"/>
        </xsl:call-template>
        <!-- Add Content MathML or failure annotation -->
        <xsl:choose>
          <xsl:when test="exists($cmathml-failures)">
            <annotation-xml encoding="MathML-Content-upconversion-failures">
              <xsl:copy-of select="$cmathml-failures"/>
            </annotation-xml>
          </xsl:when>
          <xsl:otherwise>
            <annotation-xml encoding="MathML-Content">
              <xsl:copy-of select="$cmathml/*"/>
            </annotation-xml>
          </xsl:otherwise>
        </xsl:choose>
        <!-- Copy existing annotations -->
        <xsl:copy-of select="$annotations"/>
        <!-- Copy any existing "SnuggleTeX" annotation as a "LaTeX" annotation -->
        <xsl:if test="$annotations[self::annotation and @encoding='SnuggleTeX']">
          <annotation encoding="LaTeX">
            <xsl:value-of select="$annotations[self::annotation and @encoding='SnuggleTeX'][1]"/>
          </annotation>
        </xsl:if>
        <!-- Add Maxima or failure annotation -->
        <xsl:choose>
          <xsl:when test="exists($maxima-failures)">
            <annotation-xml encoding="Maxima-upconversion-failures">
              <xsl:copy-of select="$maxima-failures"/>
            </annotation-xml>
          </xsl:when>
          <xsl:when test="not(exists($cmathml-failures))">
            <annotation encoding="Maxima">
              <xsl:value-of select="$maxima"/>
            </annotation>
          </xsl:when>
        </xsl:choose>
      </semantics>
    </math>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template name="s:maybe-wrap-in-mrow">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1">
        <xsl:copy-of select="$elements"/>
      </xsl:when>
      <xsl:otherwise>
        <mrow>
          <xsl:copy-of select="$elements"/>
        </mrow>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>


