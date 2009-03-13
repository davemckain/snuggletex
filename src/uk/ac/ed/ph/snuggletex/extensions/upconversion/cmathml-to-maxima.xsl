<!--

$Id$

This stylesheet is intended to be pipelined after the P->C stylesheet
and converts the subset of Content MathML produced by that stylesheet
into Maxima input.

IMPORTANT NOTE: This stylesheet is NOT intended to be applied to more general
Content MathML elements as it assumes certain post-conditions of the earlier
conversion to Content MathML, making life very easy here.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

TODO: Handle the lack of support for log to base 10 (or indeed other bases)

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:sc="http://www.ph.ed.ac.uk/snuggletex/cmathml"
  xmlns:local="http://www.ph.ed.ac.uk/snuggletex/cmathml-to-maxima"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s sc local"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:import href="common.xsl"/>

  <!-- ************************************************************ -->

  <xsl:param name="s:maxima-operator-function" select="'operator'" as="xs:string"/>

  <!-- ************************************************************ -->

  <!-- Entry Point -->
  <xsl:template name="s:cmathml-to-maxima">
    <xsl:param name="elements" as="item()*"/>
    <xsl:apply-templates select="$elements" mode="cmathml-to-maxima"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Supported non-alphanumeric identifiers, mapping Unicode character to Maxima input -->
  <xsl:variable name="sc:identifier-dictionary" as="element()+">
    <ci maxima-input="%alpha">&#x3b1;</ci>
    <!-- TODO: Finish this off! -->
  </xsl:variable>

  <!-- Supported elementary functions, named after CMathML elements -->
  <xsl:variable name="sc:elementary-functions" as="element()+">
    <!-- The resulting Maxima function name is encoded within an input Content MathML element -->
    <sin maxima-function="sin"/>
    <cos maxima-function="cos"/>
    <tan maxima-function="tan"/>
    <sec maxima-function="sec"/>
    <csc maxima-function="csc"/>
    <cot maxima-function="cot"/>
    <arcsin maxima-function="asin"/>
    <arccos maxima-function="acos"/>
    <arctan maxima-function="atan"/>
    <arcsec maxima-function="asec"/>
    <arccsc maxima-function="acsc"/>
    <arccot maxima-function="acot"/>
    <sinh maxima-function="sinh"/>
    <cosh maxima-function="cosh"/>
    <tanh maxima-function="tanh"/>
    <sech maxima-function="sech"/>
    <csch maxima-function="csch"/>
    <coth maxima-function="coth"/>
    <arcsinh maxima-function="asinh"/>
    <arccosh maxima-function="acosh"/>
    <arctanh maxima-function="atanh"/>
    <arcsech maxima-function="asech"/>
    <arccsch maxima-function="acsch"/>
    <arccoth maxima-function="acoth"/>
    <exp maxima-function="exp"/>
    <ln maxima-function="log"/>
    <log/>
  </xsl:variable>

  <!-- Supported prefix/infix/postfix operators -->
  <xsl:variable name="sc:operators" as="element()+">
    <eq maxima-unapplied-operator="=" maxima-nary-infix-operator=" = "/>
    <plus maxima-unapplied-operator="+" maxima-nary-infix-operator=" + " maxima-unary-prefix-operator="+"/>
    <minus maxima-unapplied-operator="-" maxima-nary-infix-operator=" - " maxima-unary-prefix-operator="-"/>
    <times maxima-unapplied-operator="*" maxima-nary-infix-operator=" * "/>
    <divide maxima-unapplied-operator="/" maxima-nary-infix-operator=" / "/>
    <power maxima-unapplied-operator="^" maxima-nary-infix-operator="^"/>
    <factorial maxima-unapplied-operator="!" maxima-unary-postfix-operator="!"/>
  </xsl:variable>

  <xsl:function name="sc:is-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($sc:operators[local-name()=$element/local-name()])"/>
  </xsl:function>

  <xsl:function name="sc:get-operator" as="element()?">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="$sc:operators[local-name()=$element/local-name()]"/>
  </xsl:function>

  <xsl:function name="sc:is-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($sc:elementary-functions[local-name()=$element/local-name()])"/>
  </xsl:function>

  <xsl:function name="sc:get-maxima-function" as="xs:string?">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="$sc:elementary-functions[local-name()=$element/local-name()]/@maxima-function"/>
  </xsl:function>

  <!-- ************************************************************ -->
  <!-- "Functional" helpers  -->
  <!-- (Recall that these may return either xs:string or an <s:fail/> element) -->

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

  <xsl:function name="sc:make-unapplied-operator" as="node()*">
    <xsl:param name="operator" as="xs:string"/>
    <xsl:value-of select="concat($s:maxima-operator-function, '(&quot;', $operator, '&quot;)')"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <!--
  Unapplied infix operator

  Example:

  <plus/>
  -->
  <xsl:template match="*[sc:is-operator(.)]" mode="cmathml-to-maxima">
    <xsl:variable name="operator" select="sc:get-operator(.)" as="element()"/>
    <xsl:value-of select="local:unapply-operator($operator)"/>
  </xsl:template>

  <xsl:function name="local:unapply-operator" as="xs:string">
    <xsl:param name="operator" as="element()"/>
    <xsl:choose>
      <xsl:when test="$operator/@maxima-unapplied-operator">
        <xsl:copy-of select="sc:make-unapplied-operator($operator/@maxima-unapplied-operator)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Operator <xsl:value-of select="$operator/local-name()"/> cannot be
          used in an unapplied context.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!--
  Applied infix operator.

  Example:

  <apply>
    <plus/>
    <ci>x</ci>
    <cn>5</cn>
  </apply>
  -->
  <xsl:template match="apply[sc:is-operator(*[1]) and count(*)&gt;1]" mode="cmathml-to-maxima">
    <xsl:variable name="operator" as="element()" select="sc:get-operator(*[1])"/>
    <xsl:variable name="operands" as="element()+" select="*[position()!=1]"/>
    <xsl:choose>
      <xsl:when test="count($operands)=1">
        <!-- Unary case -->
        <xsl:choose>
          <xsl:when test="$operator/@maxima-unary-prefix-operator">
            <!-- Prefix operator -->
            <xsl:text>(</xsl:text>
            <xsl:value-of select="$operator/@maxima-unary-prefix-operator"/>
            <xsl:copy-of select="sc:to-maxima($operands[1])"/>
            <xsl:text>)</xsl:text>
          </xsl:when>
          <xsl:when test="$operator/@maxima-unary-postfix-operator">
            <!-- Postfix operator -->
            <xsl:text>(</xsl:text>
            <xsl:copy-of select="sc:to-maxima($operands[1])"/>
            <xsl:value-of select="$operator/@maxima-unary-postfix-operator"/>
            <xsl:text>)</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">
              Operator <xsl:value-of select="$operator/local-name()"/> cannot
              be used in a unary context.
            </xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- nary case (NOTE: Earlier stylesheet will ensure binary when required) -->
        <xsl:text>(</xsl:text>
        <xsl:copy-of select="sc:to-maxima-map($operands, $operator/@maxima-nary-infix-operator)"/>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Half-arsed Applied infix operator

  Example:

  <apply>
    <plus/>
  </apply>

  This one doesn't really make any sense; we'll pretend it's an unapplied
  operator for the time being

  -->
  <xsl:template match="apply[sc:is-operator(*[1]) and count(*)=1]" mode="cmathml-to-maxima">
    <xsl:variable name="operator" as="element()" select="sc:get-operator(*[1])"/>
    <xsl:value-of select="local:unapply-operator($operator)"/>
  </xsl:template>

  <!--
  Square Root

  Example:

  <apply>
    <root/>
    <ci>x</ci>
  </apply>

  -->
  <xsl:template match="apply[*[1][self::root] and not(degree) and count(*)=2]" mode="cmathml-to-maxima">
    <xsl:variable name="operand" as="element()" select="*[2]"/>
    <xsl:text>sqrt(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($operand)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!--
  nth Root

  Example:

  <apply>
    <root/>
    <degree>
      <ci>n</ci>
    </degree>
    <ci>x</ci>
  </apply>
  -->
  <xsl:template match="apply[*[1][self::root] and degree and count(*)=3]" mode="cmathml-to-maxima">
    <xsl:variable name="operand" as="element()" select="*[3]"/>
    <xsl:text>((</xsl:text>
    <xsl:copy-of select="sc:to-maxima($operand)"/>
    <xsl:text>)^(1/</xsl:text>
    <xsl:copy-of select="sc:to-maxima(degree/*)"/>
    <xsl:text>))</xsl:text>
  </xsl:template>

  <!--
  Unapplied elementary function

  Example:

  <sin/>
  -->
  <xsl:template match="*[sc:is-elementary-function(.)]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(.)"/>
    <xsl:value-of select="$function"/>
  </xsl:template>

  <!--
  Applied Elementary Function

  Example:

  <apply>
    <sin/>
    <ci>x</ci>
  </apply>

  -->
  <xsl:template match="apply[count(*)=2 and *[1][sc:is-elementary-function(.)]]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(*[1])"/>
    <xsl:variable name="argument" as="element()" select="*[2]"/>
    <xsl:value-of select="$function"/>
    <xsl:text>(</xsl:text>
    <xsl:copy-of select="sc:to-maxima($argument)"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <!--
  Half-arsed Applied Elementary Function

  Example:

  <apply>
    <sin/>
  </apply>

  This one doesn't really make any sense; we'll pretend it's an unapplied
  function for the time being

  -->
  <xsl:template match="apply[count(*)=1 and *[1][sc:is-elementary-function(.)]]" mode="cmathml-to-maxima">
    <xsl:variable name="function" as="xs:string" select="sc:get-maxima-function(*[1])"/>
    <xsl:value-of select="$function"/>
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
    <xsl:message terminate="yes">
      Power of elementary function <xsl:value-of select="$function"/> was expected to take one argument
    </xsl:message>
  </xsl:template>

  <!-- ************************************************************ -->

  <!--
  Maxima doesn't actually support intervals!
  -->
  <xsl:template match="interval" mode="cmathml-to-maxima">
    <xsl:copy-of select="s:make-error('UMEG00', ., ())"/>
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

  <!--
  Helper function to map a flattened <ci/> element name into a suitable
  Maxima input, coping with non-alphanumeric characters as required
  -->
  <xsl:function name="local:map-identifier" as="node()">
    <xsl:param name="element" as="element(ci)"/>
    <xsl:param name="flattened" as="xs:string"/>
    <xsl:variable name="name" select="normalize-space($flattened)" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="matches($name, '^[a-zA-Z_][a-zA-Z0-9_]*$')">
        <!-- Safe to map to a Maxima variable of the same name -->
        <xsl:value-of select="$name"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Use the identifier dictionary to map it to some Maxima input -->
        <xsl:variable name="maxima-input" as="xs:string?"
          select="$sc:identifier-dictionary[.=$name]/@maxima-input"/>
        <xsl:choose>
          <xsl:when test="exists($maxima-input)">
            <xsl:value-of select="$maxima-input"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Fail: no suitable Maxima input form for identifier -->
            <xsl:copy-of select="s:make-error('UMEID0', $element, ($name))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- Map simple identifiers over as-is -->
  <xsl:template match="ci[count(node())=1 and text()]" mode="cmathml-to-maxima">
    <xsl:copy-of select="local:map-identifier(., string(.))"/>
  </xsl:template>

  <!-- Map subscripts in a reasonable way -->
  <xsl:template match="ci[count(node())=1 and msub]" mode="cmathml-to-maxima">
    <xsl:copy-of select="local:map-identifier(., concat(msub/*[1], '_', msub/*[2]))"/>
  </xsl:template>

  <!-- Don't know what to do in other cases -->
  <xsl:template match="ci" mode="cmathml-to-maxima">
    <xsl:message terminate="yes">
      Did not expect <ci/> element with content <xsl:copy-of select="node()"/>
    </xsl:message>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="cn" mode="cmathml-to-maxima">
    <xsl:value-of select="if (starts-with(., '-'))
        then concat('(', string(.), ')')
        else string(.)"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Default catch-all -->
  <xsl:template match="*" mode="cmathml-to-maxima">
    <xsl:message terminate="yes">
      No template match for element <xsl:copy-of select="."/>
    </xsl:message>
  </xsl:template>

</xsl:stylesheet>
