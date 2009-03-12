<!--

$Id$

Some common definitions for the up-conversion process.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s">

  <!-- Helper template for stylesheets that output PMathML. -->
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

  <!-- Creats an error element <s:fail/> with the given arguments -->
  <xsl:function name="s:make-error" as="element()">
    <xsl:param name="code" as="xs:string"/>
    <xsl:param name="context" as="element()+"/>
    <xsl:param name="arguments" as="xs:string*"/>
    <s:fail code="{$code}">
      <xsl:for-each select="$arguments">
        <s:arg><xsl:value-of select="."/></s:arg>
      </xsl:for-each>
      <s:context>
        <xsl:copy-of select="$context"/>
      </s:context>
    </s:fail>
  </xsl:function>

  <!-- Creats an error element <s:fail/> with the given arguments -->
  <xsl:template name="s:make-error" as="element()">
    <xsl:param name="code" as="xs:string" required="yes"/>
    <xsl:param name="context" as="element()" required="yes"/>
    <xsl:param name="arguments" as="xs:string*" required="yes"/>
    <xsl:copy-of select="s:make-error($code, $context, $arguments)"/>
  </xsl:template>

</xsl:stylesheet>
