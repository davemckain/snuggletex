<?xml version="1.0"?>
<!--

$Id: asciimathml-upconversion-demo.xsl 370 2009-05-25 16:32:05Z davemckain $

Some handy utility templats used by the various demos.

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

  <xsl:template name="format-parsing-errors">
    <xsl:param name="parsing-errors" as="element(s:error)+"/>
    <table class="failures">
      <thead>
        <tr>
          <th>Error Code</th>
          <th>Message</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="$parsing-errors">
          <tr>
            <td>
              <xsl:call-template name="make-error-code-link">
                <xsl:with-param name="error-code" select="@code"/>
              </xsl:call-template>
            </td>
            <td>
              <pre>
                <xsl:value-of select="."/>
              </pre>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
  </xsl:template>

  <xsl:template name="format-upconversion-failures">
    <xsl:param name="failures" as="element(s:fail)+"/>
    <table class="failures">
      <thead>
        <tr>
          <th>Failure Code</th>
          <th>Message</th>
          <th>Context</th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="$failures">
          <tr>
            <td>
              <xsl:call-template name="make-error-code-link">
                <xsl:with-param name="error-code" select="@code"/>
              </xsl:call-template>
            </td>
            <td><xsl:value-of select="@message"/></td>
            <td>
              <pre>
                <!--
                We'll strip off the enclosing <s:fail/>, which also conveniently
                removes namespace declarations.
                -->
                <xsl:value-of select="replace(
                  replace(
                    replace(mu:serializeElement(s:context), '^&lt;s:context.+?&gt; ?[\n\r]+', '', 's'),
                    '[\n\r]+ ?&lt;/s:context&gt;$', '', 's'),
                    '^   ', '', 'm') (: de-indent 1 level :)
                  "/>
              </pre>
            </td>
          </tr>
        </xsl:for-each>
      </tbody>
    </table>
  </xsl:template>


  <xsl:template name="make-error-code-link">
    <xsl:param name="error-code" as="xs:string"/>
    <a href="{$context-path}/documentation/error-codes.html#{$error-code}">
      <xsl:value-of select="$error-code"/>
    </a>
  </xsl:template>


</xsl:stylesheet>


