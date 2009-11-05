<!--

$Id$

This module contains templates for managing user-defined
assumptions.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:function name="s:is-assumed-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:variable name="assumed-function-targets" select="$assumptions/s:assumption[@type='function']/s:target" as="element(s:target)*"/>
    <xsl:sequence select="exists($assumed-function-targets[deep-equal(*, $element)])"/>
  </xsl:function>

</xsl:stylesheet>
