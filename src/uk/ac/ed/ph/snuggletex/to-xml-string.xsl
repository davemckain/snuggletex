<?xml version="1.0"?>
<!--

Trivial stylesheet by SnuggleSession.buildXMLString() to remove the temporary fake
<root/> from the incoming XML, resulting in a well-formed external parsed entity.

$Id: checkpoint.xsl 2712 2008-03-10 17:01:01Z davemckain $

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output omit-xml-declaration="yes"/>

  <!-- Skip over fake root -->
  <xsl:template match="root">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Keep everything else -->
  <xsl:template match="node()">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
