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
    <xsl:param name="elements" as="item()*"/>
    <xsl:apply-templates select="$elements" mode="cmathml-to-maxima"/>
  </xsl:template>

  <!-- ************************************************************ -->

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
  <!-- "Functional" helpers  -->
  <!-- (Recall that these may return either xs:string or a failure element) -->

  <xsl:function name="sc:to-maxima" as="node()*">
    <xsl:param name="element" as="element()"/>
    <xsl:apply-templates select="$element" mode="cmathml-to-maxima"/>
  </xsl:function>

  <xsl:function name="sc:to-maxima-map" as="node()*">
    <xsl:param name="elements" as="element()*"/>
    <xsl:param name="joiner" as="xs:string"/>
    <xsl:for-each select="$elements">
      <xsl:value-of select="sc:to-maxima(.)"/>
      <xsl:if test="position() != last()">
        <xsl:value-of select="$joiner"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>

  <!-- ************************************************************ -->

  <!-- Equals -->
  <xsl:template match="apply[*[1][self::eq]]" mode="cmathml-to-maxima">
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima-map($operands, ' = ')"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Sum -->
  <xsl:template match="apply[*[1][self::plus]]" mode="cmathml-to-maxima">
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima-map($operands, ' + ')"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Difference, which is either unary or binary -->
  <xsl:template match="apply[*[1][self::minus] and (count(*)=2 or count(*)=3)]" mode="cmathml-to-maxima">
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:choose>
      <xsl:when test="count($operands)=1">
        <!-- Unary version -->
        <xsl:text>(-</xsl:text>
        <xsl:copy-of select="sc:to-maxima($operands[1])"/>
        <xsl:text>)</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <!-- Binary version -->
        <xsl:text>(</xsl:text>
        <xsl:copy-of select="sc:to-maxima-map($operands, ' - ')"/>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Product -->
  <xsl:template match="apply[*[1][self::times]]" mode="cmathml-to-maxima">
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima-map($operands, ' * ')"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Quotient, which is always binary -->
  <xsl:template match="apply[*[1][self::divide] and count(*)=3]" mode="cmathml-to-maxima">
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima-map($operands, ' / ')"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Power, which is always binary -->
  <xsl:template match="apply[*[1][self::power] and count(*)=3]" mode="cmathml-to-maxima">
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($operands[1])"/>
    <xsl:text>^</xsl:text>
    <xsl:copy-of select="sc:to-maxima($operands[2])"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- Square Root -->
  <xsl:template match="apply[*[1][self::root] and not(degree) and count(*)=2]" mode="cmathml-to-maxima">
    <xsl:variable name="operand" as="element()" select="*[2]"/>
    <xsl:text>sqrt(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($operand)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!-- nth Root -->
  <xsl:template match="apply[*[1][self::root] and degree and count(*)=3]" mode="cmathml-to-maxima">
    <xsl:variable name="operand" as="element()" select="*[not(root) and not(degree)]"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($operand)"/>
    <xsl:text>)^(1/</xsl:text>
    <xsl:copy-of select="sc:to-maxima(degree/*)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!--
  Elementary Function, such as:

  <apply>
    <sin/>
    <ci>x</ci>
  </apply>

  This must have exactly 1 "argument"
  -->
  <xsl:template match="apply[count(*)=2 and *[1][sc:is-elementary-function(.)]]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(*[1])"/>
    <xsl:variable name="argument" as="element()" select="*[2]"/>
    <xsl:value-of select="$function"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($argument)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="apply[count(*)!=2 and *[1][sc:is-elementary-function(.)]]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(*[1])"/>
    <s:fail message="Elementary function {$function} expected to take one argument">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <!--
  Power of an Elementary Function. For example:

  <apply>
    <apply>
      <power/>
      <sin/>
      <cn>2</cn>
    </apply>
    <ci>x</ci>
  </apply>
  -->
  <xsl:template match="apply[count(*)=2 and *[1][self::apply and *[1][self::power] and sc:is-elementary-function(*[2]) and *[3][self::cn]]]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(*[1]/*[2])"/>
    <xsl:variable name="power" as="element()" select="*[1]/*[3]"/>
    <xsl:variable name="argument" as="element()" select="*[2]"/>
    <xsl:value-of select="$function"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($argument)"/>
    <xsl:text>)^</xsl:text>
    <xsl:copy-of select="sc:to-maxima($power)"/>
  </xsl:template>

  <xsl:template match="apply[count(*)!=2 and *[1][self::apply and *[1][self::power] and sc:is-elementary-function(*[2]) and *[3][self::cn]]]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(*[1]/*[2])"/>
    <s:fail message="Power of elementary function {$function} expected to take one argument">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="apply[count(*)!=0]" mode="cmathml-to-maxima" priority="-50">
    <s:fail message="Could not handle &lt;apply&gt; with first child '${*[1]/local-name()}'">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <xsl:template match="apply[count(*)=0]" mode="cmathml-to-maxima" priority="-50">
    <s:fail message="Could empty &lt;apply&gt;">
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
    <xsl:copy-of select="sc:to-maxima-map(*, ', ')"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="list" mode="cmathml-to-maxima">
    <xsl:text>[</xsl:text>
    <xsl:copy-of select="sc:to-maxima-map(*, ', ')"/>
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

  <!-- Map simple identifiers over as-is -->
  <xsl:template match="ci[count(node())=1 and text()]" mode="cmathml-to-maxima">
    <xsl:value-of select="string(.)"/>
  </xsl:template>

  <!-- Map subscripts in a reasonable way -->
  <xsl:template match="ci[count(node())=1 and msub]" mode="cmathml-to-maxima">
    <xsl:value-of select="concat(msub/*[1], '_', msub/*[2])"/>
  </xsl:template>

  <!-- Don't know what to do in other cases -->
  <xsl:template match="ci" mode="cmathml-to-maxima">
    <s:fail message="No support for this type of identifier">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <xsl:template match="cn" mode="cmathml-to-maxima">
    <xsl:value-of select="if (starts-with(., '-'))
        then concat('(', string(.), ')')
        else string(.)"/>
  </xsl:template>

</xsl:stylesheet>
