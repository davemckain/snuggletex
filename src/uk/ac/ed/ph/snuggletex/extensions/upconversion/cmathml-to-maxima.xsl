<!--

$Id$

This stylesheet is intended to be pipelined after the P->C stylesheet
and converts the subset of Content MathML produced by that stylesheet
into Maxima input.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:sc="http://www.ph.ed.ac.uk/snuggletex/cmathml"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s sc"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <!-- Entry Point -->
  <xsl:template name="s:cmathml-to-maxima">
    <xsl:param name="elements" as="element()*"/>
    <xsl:apply-templates select="$elements" mode="cmathml-to-maxima"/>
  </xsl:template>

  <xsl:variable name="sc:elementary-functions">
    <!-- The resulting Maxima function name is encoded within an input Content MathML element -->
    <sin>sin</sin>
    <cos>cos</cos>
    <tan>tan</tan>
    <sec>sec</sec>
    <csc>csc</csc>
    <cot>cot</cot>
    <arcsin>asin</arcsin>
    <arccos>acos</arccos>
    <arctan>atan</arctan>
    <arcsec>asec</arcsec>
    <arccsc>acsc</arccsc>
    <arccot>acot</arccot>
    <sinh>sinh</sinh>
    <cosh>cosh</cosh>
    <tanh>tanh</tanh>
    <sech>sech</sech>
    <csch>csch</csch>
    <coth>coth</coth>
    <arcsinh>asinh</arcsinh>
    <arccosh>acosh</arccosh>
    <arctanh>atanh</arctanh>
    <arcsech>asech</arcsech>
    <arccsch>acsch</arccsch>
    <arccoth>acoth</arccoth>
    <exp>exp</exp>
    <ln>log</ln><!-- NB! -->
    <log></log><!-- No Maxima built-in for this -->
  </xsl:variable>

  <xsl:function name="sc:is-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($sc:elementary-functions/*[local-name()=$element/local-name()])"/>
  </xsl:function>

  <xsl:function name="sc:get-maxima-function" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="string($sc:elementary-functions/*[local-name()=$element/local-name()])"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <xsl:template match="apply[*[1][self::eq]]" mode="cmathml-to-maxima">
    <!-- Equals -->
    <xsl:for-each select="*[position() != 1]">
      <xsl:apply-templates select="." mode="cmathml-to-maxima"/>
      <xsl:if test="position() != last()">
        <xsl:text> = </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="apply[*[1][self::plus]]" mode="cmathml-to-maxima">
    <!-- Sum -->
    <xsl:text>(</xsl:text>
    <xsl:for-each select="*[position() != 1]">
      <xsl:apply-templates select="." mode="cmathml-to-maxima"/>
      <xsl:if test="position() != last()">
        <xsl:text> + </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::minus]]" mode="cmathml-to-maxima">
    <!-- Difference, which is either unary or binary -->
    <xsl:text>(</xsl:text>
    <xsl:choose>
      <xsl:when test="count(*)=2">
        <!-- Unary version -->
        <xsl:text>-</xsl:text>
        <xsl:apply-templates select="*[2]" mode="cmathml-to-maxima"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Binary version -->
        <xsl:apply-templates select="*[2]" mode="cmathml-to-maxima"/>
        <xsl:text> - </xsl:text>
        <xsl:apply-templates select="*[3]" mode="cmathml-to-maxima"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::times]]" mode="cmathml-to-maxima">
    <!-- Product -->
    <xsl:text>(</xsl:text>
    <xsl:for-each select="*[position()!=1]">
      <xsl:apply-templates select="." mode="cmathml-to-maxima"/>
      <xsl:if test="position()!=last()">
        <xsl:text> * </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::divide]]" mode="cmathml-to-maxima">
    <!-- Quotient, which is always binary -->
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[2]" mode="cmathml-to-maxima"/>
    <xsl:text> / </xsl:text>
    <xsl:apply-templates select="*[3]" mode="cmathml-to-maxima"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::power]]" mode="cmathml-to-maxima">
    <!-- Power, which is always binary -->
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[2]" mode="cmathml-to-maxima"/>
    <xsl:text> ^ </xsl:text>
    <xsl:apply-templates select="*[3]" mode="cmathml-to-maxima"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::root] and not(degree)]" mode="cmathml-to-maxima">
    <!-- Square Root -->
    <xsl:text>sqrt(</xsl:text>
    <xsl:apply-templates select="*[2]" mode="cmathml-to-maxima"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[*[1][self::root] and degree]" mode="cmathml-to-maxima">
    <!-- nth Root -->
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[not(degree) and not(root)]" mode="cmathml-to-maxima"/>
    <xsl:text>)^(1/</xsl:text>
    <xsl:apply-templates select="degree/*" mode="cmathml-to-maxima"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Elementary Function -->
  <xsl:template match="apply[*[1][sc:is-elementary-function(current()/*[1])]]" mode="cmathml-to-maxima">
    <xsl:value-of select="sc:get-maxima-function(*[1])"/>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[position() != 1]" mode="cmathml-to-maxima"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Power of an Elementary Function. For example:

  <apply>
    <apply>
      <power/>
      <sin/>
      <cn>2</cn>
    </apply>
    <ci>x</ci>
  </apply>

  -->
  <xsl:template match="apply[*[1][self::apply and *[1][self::power] and sc:is-elementary-function(*[2]) and *[3][self::cn]]]" mode="cmathml-to-maxima">
    <xsl:value-of select="sc:get-maxima-function(*[1]/*[2])"/>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="*[position() != 1]" mode="cmathml-to-maxima"/>
    <xsl:text>)^</xsl:text>
    <xsl:apply-templates select="*[1]/*[3]" mode="cmathml-to-maxima"/>
  </xsl:template>

  <xsl:template match="apply" mode="cmathml-to-maxima">
    <s:fail message="Could not handle &lt;apply&gt; with first child '${*[1]/local-name()}'">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <xsl:template match="*[../*[1][self::apply]]" priority="-1" mode="cmathml-to-maxima">
    <!-- This will be pulled in as appropriate -->
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="interval" mode="cmathml-to-maxima">
    <s:fail message="No support for intervals">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <xsl:template match="set" mode="cmathml-to-maxima">
    <xsl:text>{</xsl:text>
    <xsl:for-each select="*">
      <xsl:apply-templates select="." mode="cmathml-to-maxima"/>
      <xsl:if test="position()!=last()">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="list" mode="cmathml-to-maxima">
    <xsl:text>[</xsl:text>
    <xsl:for-each select="*">
      <xsl:apply-templates select="." mode="cmathml-to-maxima"/>
      <xsl:if test="position()!=last()">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="exponentiale" mode="cmathml-to-maxima">
    <xsl:text>%e</xsl:text>
  </xsl:template>

  <xsl:template match="imaginaryi" mode="cmathml-to-maxima">
    <xsl:text>%i</xsl:text>
  </xsl:template>

  <xsl:template match="pi" mode="cmathml-to-maxima">
    <xsl:text>%pi</xsl:text>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="ci" mode="cmathml-to-maxima">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="cn" mode="cmathml-to-maxima">
    <xsl:choose>
      <xsl:when test="starts-with(., '-')">
        <xsl:text>(</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>)</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
