<?xml version="1.0"?>
<!--

Trivial stylesheet to convert XHTML to HTML - i.e. move all XHTML elements
into no namespace. It is safer to do all in one go here as the original tree
construction is complex enough as it is!

$Id: checkpoint.xsl 2712 2008-03-10 17:01:01Z davemckain $

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="html">

  <!-- Replace XHTML elements with corresponding variants in no namespace -->
  <xsl:template match="html:*">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*[not(starts-with(name(),'xml:'))]"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!-- Usual identity transform for everything else -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Keep any PIs (since they are required by MathPlayer) -->
  <xsl:template match="processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
