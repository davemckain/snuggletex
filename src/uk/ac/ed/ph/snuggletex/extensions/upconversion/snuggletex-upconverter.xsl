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
  xmlns:sho="http://www.ph.ed.ac.uk/snuggletex/higher-order"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s sho"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <!-- ************************************************************ -->

  <xsl:import href="pmathml-enhancer.xsl"/>
  <xsl:import href="pmathml-to-cmathml.xsl"/>
  <xsl:import href="cmathml-to-maxima.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="s:do-content-mathml" as="xs:boolean" select="true()"/>
  <xsl:param name="s:do-maxima" as="xs:boolean" select="true()"/>

  <xsl:variable name="s:snuggletex-annotation" as="xs:string" select="'SnuggleTeX'"/>
  <xsl:variable name="s:latex-annotation" as="xs:string" select="'LaTeX'"/>
  <xsl:variable name="s:content-mathml-annotation" as="xs:string" select="'MathML-Content'"/>
  <xsl:variable name="s:content-failures-annotation" as="xs:string" select="'MathML-Content-upconversion-failures'"/>
  <xsl:variable name="s:maxima-annotation" as="xs:string" select="'Maxima'"/>
  <xsl:variable name="s:maxima-failures-annotation" as="xs:string" select="'Maxima-upconversion-failures'"/>

  <!-- ************************************************************ -->

  <!-- Catch-all template for non-MathML (also non-"higher-order elements" used by PMathML enhancer) -->
  <xsl:template match="*[not(self::sho:*)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="math" priority="10">
    <!-- Extract the actual PMathML content and any existing annotations -->
    <xsl:variable name="presentation-mathml" select="if (semantics) then (if (semantics/mrow) then semantics/mrow/* else semantics/*[1]) else *" as="element()*"/>
    <xsl:variable name="annotations" select="if (semantics) then semantics/*[position() != 1] else ()" as="element()*"/>
    <!-- We always perform enhancement of the Presentation MathML, creating a new Document Node -->
    <xsl:variable name="enhanced-pmathml">
      <xsl:call-template name="s:enhance-pmathml">
        <xsl:with-param name="elements" select="$presentation-mathml"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- Maybe convert Presentation MathML to Content MathML, creating another new Document Node -->
    <xsl:variable name="cmathml">
      <xsl:if test="$s:do-content-mathml or $s:do-maxima">
        <xsl:call-template name="s:pmathml-to-cmathml">
          <xsl:with-param name="elements" select="$enhanced-pmathml/*"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <!-- Extract any failures in this process -->
    <xsl:variable name="cmathml-failures" as="element(s:fail)*">
      <xsl:copy-of select="$cmathml//s:fail"/>
    </xsl:variable>
    <!-- Maybe convert Content MathML to Maxima input. (This is normally a sequence of
         xs:string but might contain failure elements as well so we need to be careful) -->
    <xsl:variable name="maxima-raw" as="item()*">
      <xsl:choose>
        <xsl:when test="not($s:do-maxima) or exists($cmathml-failures)">
          <!-- Don't bother converting if asked not to or if we failed earlier on -->
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
      <xsl:copy-of select="$maxima-raw[self::s:fail]"/>
    </xsl:variable>
    <!-- Formulate the final Maxima string, stripping off the outer pair of brackets
         if present. (This is sane as if they occur then they bracket the entire expression. -->
    <xsl:variable name="maxima-with-brackets" as="xs:string?"
      select="if (exists($maxima-failures)) then () else string-join($maxima-raw, '')"/>
    <xsl:variable name="maxima" as="xs:string?"
      select="if (exists($maxima-failures)) then () else
        if (starts-with($maxima-with-brackets, '(') and ends-with($maxima-with-brackets, ')'))
        then substring($maxima-with-brackets, 2, string-length($maxima-with-brackets) - 2)
        else $maxima-with-brackets"/>
    <!-- Finally build up the resulting MathML -->
    <math>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
        <xsl:when test="$s:do-content-mathml or $s:do-maxima or exists($annotations)">
          <!-- We're definitely going to be doing annotations here! -->
          <semantics>
            <!-- Put in the enhanced Presentation MathML first -->
            <xsl:call-template name="s:maybe-wrap-in-mrow">
              <xsl:with-param name="elements" select="$enhanced-pmathml/*"/>
            </xsl:call-template>
            <!-- Maybe add Content MathML or failure annotation -->
            <xsl:choose>
              <xsl:when test="exists($cmathml-failures)">
                <annotation-xml encoding="{$s:content-failures-annotation}">
                  <xsl:copy-of select="$cmathml-failures"/>
                </annotation-xml>
              </xsl:when>
              <xsl:when test="$s:do-content-mathml">
                <annotation-xml encoding="{$s:content-mathml-annotation}">
                  <xsl:copy-of select="$cmathml/*"/>
                </annotation-xml>
              </xsl:when>
            </xsl:choose>
            <!-- Copy existing annotations -->
            <xsl:copy-of select="$annotations"/>
            <!-- Copy any existing "SnuggleTeX" annotation as a "LaTeX" annotation -->
            <xsl:if test="$annotations[self::annotation and @encoding=$s:snuggletex-annotation]">
              <annotation encoding="{$s:latex-annotation}">
                <xsl:value-of select="$annotations[self::annotation and @encoding=$s:snuggletex-annotation][1]"/>
              </annotation>
            </xsl:if>
            <!-- Maybe add Maxima or failure annotation -->
            <xsl:choose>
              <xsl:when test="exists($maxima-failures)">
                <annotation-xml encoding="{$s:maxima-failures-annotation}">
                  <xsl:copy-of select="$maxima-failures"/>
                </annotation-xml>
              </xsl:when>
              <xsl:when test="$s:do-maxima and not(exists($cmathml-failures))">
                <annotation encoding="{$s:maxima-annotation}">
                  <xsl:value-of select="$maxima"/>
                </annotation>
              </xsl:when>
            </xsl:choose>
          </semantics>
        </xsl:when>
        <xsl:otherwise>
          <!-- All we did was enhance the PMathML and there are no annotations. For niceness,
          we'll strip off a top-level <mrow/> if it is deemed to be redundant -->
          <xsl:choose>
            <xsl:when test="$enhanced-pmathml[count(*)=1 and *[1][self::mrow]]">
              <xsl:copy-of select="$enhanced-pmathml/mrow/*"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="$enhanced-pmathml/*"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
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


