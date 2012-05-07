<!--

$Id$

Some common definitions for the up-conversion process.

NOTE: This uses an extension function to convert raw error codes
to full messages, which is convenient when inspecting the raw
XML output.

Copyright (c) 2008-2011, The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:util="ext://uk.ac.ed.ph.snuggletex.upconversion.UpConversionUtilities"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s m util">

  <!-- Java properties file containing up-conversion error message deails -->
  <xsl:variable name="error-codes" as="xs:string"
    select="unparsed-text('error-messages.properties')"
    use-when="not(function-available('util:getErrorMessage'))"/>

  <xsl:function name="s:get-error-message" as="xs:string" use-when="function-available('util:getErrorMessage')">
    <xsl:param name="code" as="xs:string"/>
    <xsl:param name="arguments" as="xs:string*"/>
    <xsl:value-of select="util:getErrorMessage($code, $arguments)"/>
  </xsl:function>

  <!--
  Pure Java fall-back version of UpConversionUtilities#getErrorMessage()
  that formats an up-conversion error message by parsing the Java properties file directly.

  This makes it easier for people to use these stylesheets without requiring the underlying
  SnuggleTeX Java code. However, there is no support for internationalisation here.

  NOTE: There is no support for escaped characters in the property values (i.e. message
  text) as we don't currently need them. Add this if it's required later.
  -->
  <xsl:function name="s:get-error-message" as="xs:string" use-when="not(function-available('util:getErrorMessage'))">
    <xsl:param name="code" as="xs:string"/>
    <xsl:param name="arguments" as="xs:string*"/>
    <!-- Find the appropriate error message -->
    <xsl:variable name="message" as="xs:string?">
      <xsl:analyze-string select="$error-codes" regex="^{$code}=(.+?)$" flags="m">
        <xsl:matching-substring>
          <xsl:sequence select="regex-group(1)"/>
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="exists($message)">
        <!-- Replace argument placeholders -->
        <xsl:variable name="replaced" as="item()*">
          <xsl:analyze-string select="$message" regex="\{{(\d+)\}}">
            <xsl:matching-substring>
              <xsl:variable name="argument-index" as="xs:integer" select="xs:integer(regex-group(1))"/>
              <xsl:value-of select="$arguments[position()=$argument-index + 1]"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
              <xsl:value-of select="."/>
            </xsl:non-matching-substring>
          </xsl:analyze-string>
        </xsl:variable>
        <xsl:value-of select="string-join($replaced, '')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Error code <xsl:value-of select="$error-codes"/> is not present in default messages file
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Creates an "up-conversion failure" element <s:fail/> with the given arguments -->
  <xsl:function name="s:make-error" as="element(s:fail)">
    <xsl:param name="code" as="xs:string"/>
    <xsl:param name="context" as="element()+"/>
    <xsl:param name="arguments" as="xs:string*"/>
    <s:fail code="{$code}" message="{s:get-error-message($code, $arguments)}">
      <xsl:for-each select="$arguments">
        <s:arg><xsl:value-of select="."/></s:arg>
      </xsl:for-each>
      <s:xpath>
        <xsl:value-of select="s:make-math-xpath($context[1])"/>
      </s:xpath>
      <s:context>
        <xsl:copy-of select="$context"/>
      </s:context>
    </s:fail>
  </xsl:function>

  <!--
  Makes an XPath expression from the given (MathML) element, rooted at
  the enclosing <math/> container.

  This should generate a valid XPath, provided the default namespace is
  set correctly.
  -->
  <xsl:function name="s:make-math-xpath" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="s:build-math-xpath($element, '')"/>
  </xsl:function>

  <xsl:function name="s:build-math-xpath" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="xpath-tail" as="xs:string"/>
    <xsl:variable name="path-element" as="xs:string" select="concat(
      local-name($element),
      '[', 1 + count($element/preceding-sibling::*[local-name()=local-name($element)]), ']',
      (if ($xpath-tail != '') then '/' else ''),
      $xpath-tail)"/>
    <xsl:variable name="math-parent" select="$element/parent::m:*" as="element()?"/>
    <xsl:sequence select="if (exists($math-parent))
        then s:build-math-xpath($math-parent, $path-element)
        else $path-element"/>
  </xsl:function>

</xsl:stylesheet>
