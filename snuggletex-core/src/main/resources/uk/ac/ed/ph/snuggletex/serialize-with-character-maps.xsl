<!--
FIXME: Document this!

$Id$

-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="character-maps.xsl"/>
  <xsl:output use-character-maps="output"/>

  <xsl:template match="/">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
