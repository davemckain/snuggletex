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
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:function name="s:get-symbol-assumption" as="element(s:assumption)?">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:variable name="assumed-symbols" as="element(s:assumption)*"
      select="$assumptions/s:assumption[@type='symbol']" />
    <xsl:sequence select="$assumed-symbols[deep-equal(s:target/*, $element)]"/>
  </xsl:function>

  <xsl:function name="s:is-assumed-symbol" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:param name="property" as="xs:string"/>
    <xsl:sequence select="exists($assumptions/s:assumption[@type='symbol' and @property=$property]/s:target[deep-equal($element, *)])"/>
  </xsl:function>

  <xsl:function name="s:is-assumed-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:sequence select="s:is-assumed-symbol($element, $assumptions, 'function')"/>
  </xsl:function>

</xsl:stylesheet>
