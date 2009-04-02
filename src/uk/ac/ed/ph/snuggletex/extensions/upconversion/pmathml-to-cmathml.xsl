<!--

$Id$

This stylesheet attempts to convert a Presentation MathML <math/>
element to Content MathML, under the core assumption that the mathematics
represented is simple (i.e. elementary functions and operators, plus a
few other things).

Some semantic inference is also performed basic on common conventions,
which can be turned off if required.

Copyright (c) 2009 The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:sp="http://www.ph.ed.ac.uk/snuggletex/pmathml"
  xmlns:local="http://www.ph.ed.ac.uk/snuggletex/pmathml-to-cmathml"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s sp local"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:import href="common.xsl"/>

  <!-- ************************************************************ -->

  <xsl:param name="s:assume-exponential-e" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-imaginary-i" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-constant-pi" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-braces-set" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-square-list" select="true()" as="xs:boolean"/>

  <!-- ************************************************************ -->
  <!-- TODO: A lot of what follows is shared with pmathml-enhancer.xsl -->

  <xsl:strip-space elements="m:*"/>

  <xsl:variable name="sp:invertible-elementary-functions" as="xs:string+"
    select="('sin', 'cos', 'tan',
             'sec', 'csc' ,'cot',
             'sinh', 'cosh', 'tanh',
             'sech', 'csch', 'coth')"/>

  <xsl:variable name="sp:elementary-functions" as="xs:string+"
    select="($sp:invertible-elementary-functions,
            'arcsin', 'arccos', 'arctan',
            'arcsec', 'arccsc', 'arccot',
            'arcsinh', 'arccosh', 'arctanh',
            'arcsech', 'arccsch', 'arccoth',
            'ln', 'log', 'exp')"/>

  <xsl:variable name="local:explicit-multiplication-characters" as="xs:string+"
    select="('*', '&#xd7;', '&#x22c5;')"/>

  <xsl:variable name="local:implicit-multiplication-characters" as="xs:string+"
    select="('&#x2062;')"/>

  <xsl:variable name="local:multiplication-characters" as="xs:string+"
    select="($local:explicit-multiplication-characters,
             $local:implicit-multiplication-characters)"/>

  <xsl:variable name="local:prefix-operators" as="element()+">
    <local:operator input="&#xac;" output="not"/>
  </xsl:variable>

  <!--
  Mappings of <mi/> element contents to Content MathML relation operators,
  including support for negating operators by combining with a <not/> element
  where appropriate.
  -->
  <xsl:variable name="local:relation-operators" as="element()+">
    <local:operator input="=" output="eq"/>
    <local:operator input="&#x2260;" output="neq"/>
    <local:operator input="&lt;" input-negated="&#x226e;" output="lt"/>
    <local:operator input="&gt;" input-negated="&#x226f;" output="gt"/>
    <local:operator input="&#x2264;" input-negated="&#x2270;" output="leq"/>
    <local:operator input="&#x2265;" input-negated="&#x2271;" output="geq"/>
    <local:operator input="&#x2261;" input-negated="&#x2262;" output="equivalent"/>
    <local:operator input="&#x2248;" input-negated="&#x2249;" output="approx"/>
    <local:operator input="|" input-negated="&#x2224;" output="factorof"/>
    <local:operator input="&#x2208;" output="in"/>
    <local:operator input="&#x2209;" output="notin"/>
    <!--
    TODO: The following outputs are often represented by different input characters.
    Might be good to parametrise these.
    -->
    <local:operator input="&#x2282;" output="prsubset"/>
    <local:operator input="&#x2284;" output="notprsubset"/>
    <local:operator input="&#x2286;" output="subset"/>
    <local:operator input="&#x2288;" output="notsubset"/>
    <local:operator input="&#x2192;" output="tendsto"/>
    <local:operator input="&#x21d2;" output="implies"/>
  </xsl:variable>

  <!--
  Tests for the equivalent of \sin, \sin^{.}, \log_{.}, \log_{.}^{.}
  Result need not make any actual sense so will need further inspection
  -->
  <xsl:function name="local:is-supported-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="local:is-elementary-function($element)
      or $element[self::msup and local:is-elementary-function(*[1])]
      or $element[self::msub and *[1][self::mi and .='log']]
      or $element[self::msubsup and *[1][self::mi and .='log']]"/>
  </xsl:function>

  <xsl:function name="local:is-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mi and $sp:elementary-functions=string(.)])"/>
  </xsl:function>

  <xsl:function name="local:is-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo])"/>
  </xsl:function>

  <xsl:function name="local:is-relation-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="local:is-operator($element)
      and ($local:relation-operators[@input=string($element)]
        or $local:relation-operators[@input-negated=string($element)])"/>
  </xsl:function>

  <xsl:function name="local:get-prefix-operator" as="xs:string?">
    <xsl:param name="string" as="xs:string"/>
    <xsl:sequence select="$local:prefix-operators[@input=$string]/@output"/>
  </xsl:function>

  <xsl:function name="local:is-prefix-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="local:is-operator($element)
      and exists(local:get-prefix-operator(string($element)))"/>
  </xsl:function>

  <xsl:function name="local:is-factorial-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='!'])"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <!-- Entry point -->
  <xsl:template name="s:pmathml-to-cmathml">
    <xsl:param name="elements" as="element()*"/>
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="$elements"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Application of groups by the precedence order built by pmathml-enhancer.xsl -->
  <xsl:template name="local:process-group">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
      <xsl:when test="$elements[self::mspace]">
        <!-- Strip off <mspace/> and reapply this template to whatever is left -->
        <xsl:call-template name="local:process-group">
          <xsl:with-param name="elements" select="$elements[not(self::mspace)]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('&#x2228;'))]">
        <!-- Logical Or -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2228;')"/>
          <xsl:with-param name="cmathml-name" select="'or'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('&#x2227;'))]">
        <!-- Logical And -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2227;')"/>
          <xsl:with-param name="cmathml-name" select="'and'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-relation-operator(.)]">
        <!-- (Possibly mixed) Relation Operators -->
        <xsl:call-template name="local:handle-relation-operators">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('&#x222a;'))]">
        <!-- Set Union -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x222a;')"/>
          <xsl:with-param name="cmathml-name" select="'union'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('&#x2229;'))]">
        <!-- Set Intersection -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2229;')"/>
          <xsl:with-param name="cmathml-name" select="'intersect'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('\'))]">
        <!-- Set Difference -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('\')"/>
          <xsl:with-param name="cmathml-name" select="'setdiff'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('+'))]">
        <!-- Addition -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('+')"/>
          <xsl:with-param name="cmathml-name" select="'plus'"/>
          <xsl:with-param name="allow-as-prefix" select="true()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('-'))]">
        <!-- Subtraction -->
        <xsl:call-template name="local:handle-binary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('-')"/>
          <xsl:with-param name="cmathml-name" select="'minus'"/>
          <xsl:with-param name="allow-as-prefix" select="true()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., $local:multiplication-characters)]">
        <!-- Explicit or Implicit multiplication -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="$local:multiplication-characters"/>
          <xsl:with-param name="cmathml-name" select="'times'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-operator(., ('/'))]">
        <!-- Division -->
        <xsl:call-template name="local:handle-binary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('/')"/>
          <xsl:with-param name="cmathml-name" select="'divide'"/>
          <xsl:with-param name="allow-as-prefix" select="false()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[1][local:is-supported-function(.)]">
        <!-- Supported function (not necessarily applied) -->
        <xsl:call-template name="local:handle-supported-function-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[1][local:is-prefix-operator(.)]">
        <!-- Supported prefix operator (not necessarily applied) -->
        <xsl:call-template name="local:handle-prefix-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[position()=last()][local:is-factorial-operator(.)]">
        <!-- Factorial -->
        <xsl:call-template name="local:handle-factorial-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($elements)=1">
        <!-- "Atom" -->
        <xsl:apply-templates select="$elements[1]" mode="pmathml-to-cmathml"/>
      </xsl:when>
      <xsl:when test="empty($elements)">
        <!-- Empty -> empty -->
      </xsl:when>
      <xsl:otherwise>
        <!-- Fail: unhandled group -->
        <xsl:copy-of select="s:make-error('UCEG01', $elements, ())"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Tests whether the given element is a <mo/> applied infix -->
  <xsl:function name="local:is-matching-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="match" as="xs:string+"/>
    <xsl:sequence select="boolean(local:is-operator($element) and $element=$match)"/>
  </xsl:function>

  <!--
  n-ary infix operator, such as '+', optionally allowed to be used in a prefix context.

  This will have been grouped appropriately by the pmathml-enhancer.xsl stylesheet
  so supported legal expressions will always be of one of the following forms:

  1. n1 op1 n2 op2 ... nk (usual infix form)
  2. op (unapplied operator)
  3. op n (if the operator may also be used in prefix context)

  We need to be careful to handle unapplied operators and illegal
  prefix/postfix applications.
  -->
  <xsl:template name="local:handle-nary-operator">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="match" as="xs:string+" required="yes"/>
    <xsl:param name="cmathml-name" as="xs:string" required="yes"/>
    <xsl:param name="allow-as-prefix" as="xs:boolean" select="false()"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1 and local:is-matching-operator($elements[1], $match)">
        <!-- Unapplied operator -->
        <xsl:element name="{$cmathml-name}"/>
      </xsl:when>
      <xsl:when test="count($elements)=2 and local:is-matching-operator($elements[1], $match)
          and not(local:is-operator($elements[2]))">
        <!-- Prefix context -->
        <xsl:choose>
          <xsl:when test="not($allow-as-prefix)">
            <!-- Fail: operator is not a prefix operator -->
            <xsl:copy-of select="s:make-error('UCEOP0', ., ($cmathml-name))"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Legal prefix application -->
            <apply>
              <xsl:element name="{$cmathml-name}"/>
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$elements[2]"/>
              </xsl:call-template>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- Expecting legal infix content. We will check and process contents first -->
        <xsl:variable name="content" as="element()*">
          <xsl:if test="count($elements) mod 2 = 0">
            <!-- Fail: Unsupported n-ary infix grouping -->
            <xsl:copy-of select="s:make-error('UCEOP1', $elements, ($cmathml-name))"/>
          </xsl:if>
          <xsl:for-each select="$elements">
            <xsl:variable name="i" as="xs:integer" select="position()"/>
            <xsl:choose>
              <xsl:when test="$i mod 2 = 1">
                <!-- Odd position, so expecting operand -->
                <xsl:choose>
                  <xsl:when test="local:is-matching-operator(., $match)">
                    <!-- Fail: (as above) -->
                    <xsl:copy-of select="s:make-error('UCEOP1', $elements, ($cmathml-name))"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- Supported -->
                    <xsl:call-template name="local:process-group">
                      <xsl:with-param name="elements" select="."/>
                    </xsl:call-template>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <!-- Even position, so expecting operator -->
                <xsl:if test="not(local:is-matching-operator(., $match))">
                  <!-- Fail: (as above) -->
                  <xsl:copy-of select="s:make-error('UCEOP4', $elements, ())"/>
                </xsl:if>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:variable>
        <!-- Report first error child, if found -->
        <xsl:choose>
          <xsl:when test="$content[self::s:fail]">
            <xsl:copy-of select="$content[self::s:fail][1] | $content[not(self::s:fail)]"/>
          </xsl:when>
          <xsl:otherwise>
            <apply>
              <xsl:element name="{$cmathml-name}"/>
              <xsl:copy-of select="$content"/>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Mix of (positive) relation operators, such as '=', '<', '>'.

  If there are more than 2 of these then we pair them into logical "and" groups.

  For example, an expression like '1 < 2 = 3' would end up being represented as:

  <apply>
    <and/>
    <apply>
      <lt/>
      <ci>1</ci>
      <ci>2</ci>
    </apply>
    <apply>
      <eq/>
      <ci>2</ci>
      <ci>3</ci>
    </apply>
  </apply>

  This is not strictly necessary if there is only one unique type of operator
  in the expression as they are n-ary, but makes any later up-conversion to Maxima
  much easier.

  As with infix operators, we support the following forms:

  1. n1 rel1 n2 rel2 ... nk
  2. rel (unapplied relation)

  FIXME: Need to cope with negative operators, which need to generatate
  corresponding notted operators in the output.
  -->
  <xsl:template name="local:handle-relation-operators">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="element-count" as="xs:integer" select="count($elements)"/>
    <xsl:choose>
      <xsl:when test="$element-count=1 and local:is-relation-operator($elements[1])">
        <!-- Unapplied relation -->
        <xsl:copy-of select="local:create-relation-element($elements[1], ())"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Standard infix form -->
        <xsl:variable name="paired" as="element()+">
          <xsl:if test="count($elements) mod 2 = 0">
            <!-- Fail: Relation operators must be strictly infix -->
            <xsl:copy-of select="s:make-error('UCEOP4', $elements, ())"/>
          </xsl:if>
          <xsl:for-each select="$elements">
            <xsl:variable name="i" as="xs:integer" select="position()"/>
            <xsl:choose>
              <xsl:when test="$i mod 2 = 1">
                <!-- Odd position, so expecting operand -->
                <xsl:if test="local:is-relation-operator(.)">
                  <!-- Fail: Relation operators must be strictly infix -->
                  <xsl:copy-of select="s:make-error('UCEOP4', $elements, ())"/>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <!-- Even position, so expecting relation -->
                <xsl:choose>
                  <xsl:when test="not(local:is-relation-operator(.))">
                    <!-- Fail: Relation operators must be strictly infix -->
                    <xsl:copy-of select="s:make-error('UCEOP4', $elements, ())"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- Group what came before and what comes after -->
                    <xsl:variable name="arguments" as="element()*">
                      <xsl:call-template name="local:process-group">
                        <xsl:with-param name="elements" select="$elements[position()=$i - 1]"/>
                      </xsl:call-template>
                      <xsl:call-template name="local:process-group">
                        <xsl:with-param name="elements" select="$elements[position()=$i + 1]"/>
                      </xsl:call-template>
                    </xsl:variable>
                    <xsl:copy-of select="local:create-relation-element(., $arguments)"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$paired[self::s:fail]">
            <!-- Grouping error occurred. Keep the first one only as otherwise it gets tedious -->
            <xsl:copy-of select="$paired[self::s:fail][1] | $paired[not(self::s:fail)]"/>
          </xsl:when>
          <xsl:when test="count($paired)=1">
            <!-- Single relation operator used in binary context, so easy -->
            <xsl:copy-of select="$paired[1]"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- More than one operator, so group as a logical 'and' -->
            <apply>
              <and/>
              <xsl:copy-of select="$paired"/>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Helper for the above template that creates the appropriate CMathML relation operator
  corresponding to the given <mi/>, wrapping in <not/> if required
  -->
  <xsl:function name="local:create-relation-element" as="element()?">
    <xsl:param name="mi" as="element()"/>
    <xsl:param name="arguments" as="element()*"/>
    <xsl:variable name="positive-native" as="xs:string?" select="$local:relation-operators[@input=string($mi)]/@output"/>
    <xsl:variable name="negated-native" as="xs:string?" select="$local:relation-operators[@input-negated=string($mi)]/@output-negated"/>
    <xsl:variable name="negated-synthetic" as="xs:string?" select="$local:relation-operators[@input-negated=string($mi)]/@output"/>
    <xsl:choose>
      <xsl:when test="exists($positive-native) or exists($negated-native)">
        <xsl:variable name="output-native" as="xs:string" select="($positive-native, $negated-native)[1]"/>
        <xsl:choose>
          <xsl:when test="exists($arguments)">
            <apply>
              <xsl:element name="{$output-native}"/>
              <xsl:copy-of select="$arguments"/>
            </apply>
          </xsl:when>
          <xsl:otherwise>
            <!-- Unapplied relation -->
            <xsl:element name="{$output-native}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="exists($negated-synthetic)">
        <xsl:choose>
          <xsl:when test="exists($arguments)">
            <apply>
              <not/>
              <apply>
                <xsl:element name="{$negated-synthetic}"/>
                <xsl:copy-of select="$arguments"/>
              </apply>
            </apply>
          </xsl:when>
          <xsl:otherwise>
            <!-- Unapplied case, though not we still apply "not" to it -->
            <apply>
              <not/>
              <xsl:element name="{$negated-synthetic}"/>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Unexpected logic branch
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <!--
  Binary (and possibly unary) operator, such as '-' or '/'.

  Supported legal expressions will always be of one of the following forms:

  1. n1 op n2 (infix form)
  2. op (unapplied operator)
  3. op n (if the operator may also be used in unary (prefix) context)

  -->
  <xsl:template name="local:handle-binary-operator">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="match" as="xs:string+" required="yes"/>
    <xsl:param name="cmathml-name" as="xs:string" required="yes"/>
    <xsl:param name="allow-as-prefix" as="xs:boolean" select="false()"/>
    <xsl:variable name="operators" select="$elements[local:is-matching-operator(., $match)]" as="element()+"/>
    <xsl:variable name="operator-count" select="count($operators)" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1 and local:is-matching-operator($elements[1], $match)">
        <!-- Unapplied operator -->
        <xsl:element name="{$cmathml-name}"/>
      </xsl:when>
      <xsl:when test="count($elements)=2 and local:is-matching-operator($elements[1], $match)
          and not(local:is-operator($elements[2]))">
        <!-- Unary/prefix context -->
        <xsl:choose>
          <xsl:when test="not($allow-as-prefix)">
            <!-- Fail: operator is not a prefix operator -->
            <xsl:copy-of select="s:make-error('UCEOP0', ., ($cmathml-name))"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Legal prefix/unary application -->
            <apply>
              <xsl:element name="{$cmathml-name}"/>
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$elements[2]"/>
              </xsl:call-template>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="count($elements) &gt; 3">
        <!-- Fail: n-ary with n>2 not allowed -->
        <xsl:copy-of select="s:make-error('UCEOP3', $elements, ($cmathml-name))"/>
      </xsl:when>
      <xsl:when test="count($elements) &lt; 3 or $elements[position()!=2][local:is-operator(.)] or not($elements[2][local:is-matching-operator(., $match)])">
        <!-- Fail: bad grouping for binary operator -->
        <xsl:copy-of select="s:make-error('UCEOP2', $elements, ($cmathml-name))"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- This is type (1) as outlined above -->
        <apply>
          <xsl:element name="{$cmathml-name}"/>
          <xsl:call-template name="local:process-group">
            <xsl:with-param name="elements" select="$elements[1]"/>
          </xsl:call-template>
          <xsl:call-template name="local:process-group">
            <xsl:with-param name="elements" select="$elements[3]"/>
          </xsl:call-template>
        </apply>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Group containing a supported function, say 'f'.

  Denoting function application as 'o', we have the following possibilities:

  (1) 'f' is unapplied
  (2) 'fox' which is the most common case.
  (3) 'fogox' which is treated as 'fo(gox)'

  -->
  <xsl:template name="local:handle-supported-function-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1">
        <!-- This is case (1) above -->
        <xsl:call-template name="local:create-elementary-function-operator">
          <xsl:with-param name="operator-element" select="$elements[1]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- This is (2) or (3). In both cases, the second element must be "apply function" -->
        <xsl:variable name="first-function" select="$elements[1]" as="element()"/>
        <xsl:choose>
          <xsl:when test="not($elements[2][self::mo and .='&#x2061;'])">
            <!-- Fail (unlikely): Expected "apply function" operator as second element -->
            <xsl:copy-of select="s:make-error('UCEFX0', $elements, ())"/>
          </xsl:when>
          <xsl:when test="not(exists($elements[3]))">
            <!-- Fail (unlikely): Nothing following "apply function" operator -->
            <xsl:copy-of select="s:make-error('UCEFX1', $elements, ())"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- This is really (2) or (3)! -->
            <xsl:variable name="first-apply" select="$elements[2]" as="element()"/>
            <xsl:variable name="after-first-apply" select="$elements[position() &gt; 2]" as="element()+"/>
            <apply>
              <xsl:call-template name="local:create-elementary-function-operator">
                <xsl:with-param name="operator-element" select="$first-function"/>
              </xsl:call-template>
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$after-first-apply"/>
              </xsl:call-template>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="local:create-elementary-function-operator">
    <xsl:param name="operator-element" as="element()" required="yes"/>
    <xsl:choose>
      <xsl:when test="$operator-element[self::msup and *[1][self::mi] and *[2][self::mn and .='-1']]">
        <!-- It looks like an inverse function. Make sure we know about it -->
        <xsl:variable name="function" select="string($operator-element/*[1])" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$sp:invertible-elementary-functions=$function">
            <xsl:element name="arc{$function}"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Fail: Unknown inverse function -->
            <xsl:copy-of select="s:make-error('UCEFN1', $operator-element, ($function))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$operator-element[self::msup
          and *[1][self::mi]
          and *[2][self::mn and number(.) &gt;= 1]]">
        <!-- This looks like sin^2, which we will interpret as such -->
        <xsl:variable name="function" select="string($operator-element/*[1])" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$sp:elementary-functions=$function">
            <apply>
              <power/>
              <xsl:element name="{$function}"/>
              <xsl:apply-templates select="$operator-element/*[2]" mode="pmathml-to-cmathml"/>
            </apply>
          </xsl:when>
          <xsl:otherwise>
            <!-- Fail: Unknown function -->
            <xsl:copy-of select="s:make-error('UCEFN0', $operator-element, ($function))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$operator-element[self::msub
          and *[1][self::mi and .='log']
          and *[2][self::mi or self::mn]]">
        <!-- Log to a different base -->
        <log/>
        <logbase>
          <xsl:apply-templates select="$operator-element/*[2]" mode="pmathml-to-cmathml"/>
        </logbase>
      </xsl:when>
      <xsl:when test="$operator-element[self::msubsup
          and *[1][self::mi and .='log']
          and *[2][self::mi or self::mn]
          and *[3][self::mn and number(.) &gt;=1]]">
        <!-- Log to a different base with a power -->
        <apply>
          <power/>
          <apply>
            <log/>
            <logbase>
              <xsl:apply-templates select="$operator-element/*[2]" mode="pmathml-to-cmathml"/>
            </logbase>
          </apply>
          <xsl:apply-templates select="$operator-element/*[3]" mode="pmathml-to-cmathml"/>
        </apply>
      </xsl:when>
      <xsl:when test="$operator-element[self::mi]">
        <xsl:variable name="function" select="string($operator-element)" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$sp:elementary-functions=$function">
            <!-- Create Content MathML element with same name as content of <mi/> element -->
            <xsl:element name="{$function}"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Fail: Unknown function -->
            <xsl:copy-of select="s:make-error('UCEFN0', $operator-element, ($function))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Unknown supported function <xsl:copy-of select="$operator-element"/>.
          This logic branch should not have been reached!
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--
  Group starting with a prefix operator, say 'p'. Main possibilities:

  (1) 'p' is unapplied
  (2) 'p' is applied to what follows

  -->
  <xsl:template name="local:handle-prefix-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <!-- Get Content MathML element corresponding to this operator -->
    <xsl:variable name="prefix-operator" as="xs:string" select="local:get-prefix-operator($elements[1])"/>
    <xsl:variable name="cmathml-operator" as="element()">
      <xsl:element name="{$prefix-operator}"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($elements)=1">
        <!-- This is case (1) above -->
        <xsl:copy-of select="$cmathml-operator"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- This is (2) -->
        <xsl:variable name="operands" as="element()+" select="$elements[position()!=1]"/>
        <xsl:choose>
          <xsl:when test="$operands[local:is-operator(.)]">
            <!-- Fail: bad combination of operators -->
            <xsl:copy-of select="s:make-error('UCEOP5', $elements, ())"/>
          </xsl:when>
          <xsl:otherwise>
            <apply>
              <xsl:copy-of select="$cmathml-operator"/>
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$operands"/>
              </xsl:call-template>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="local:handle-factorial-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="factorials" as="element()+" select="$elements[local:is-factorial-operator(.)]"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1">
        <!-- Unapplied factorial -->
        <factorial/>
      </xsl:when>
      <xsl:when test="count($factorials)&gt;1">
        <!-- Too many factorials...
             Fail: Bad combination of operators -->
        <xsl:copy-of select="s:make-error('UCEOP5', $elements, ())"/>
      </xsl:when>
      <xsl:when test="count($elements)=2">
        <!-- Applied factorial -->
        <apply>
          <factorial/>
          <xsl:call-template name="local:process-group">
            <xsl:with-param name="elements" select="$elements[1]"/>
          </xsl:call-template>
        </apply>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Expected factorial operator to be preceded by 1 element.
          Got: <xsl:copy-of select="$elements[position()!=last()]"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="mrow" mode="pmathml-to-cmathml">
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="*"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="mfenced[@open='(' and @close=')' and count(*)=1]" mode="pmathml-to-cmathml">
    <!-- Treat this as (...), which basically means we treat the content as a single group -->
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="*[1]"/>
    </xsl:call-template>
  </xsl:template>

  <!-- (Optional) Treat [a,b,c,...] as a list -->
  <xsl:template match="mfenced[$s:assume-square-list and @open='[' and @close=']']" mode="pmathml-to-cmathml">
    <list>
      <xsl:apply-templates mode="pmathml-to-cmathml"/>
    </list>
  </xsl:template>

  <!-- (Optional) Treat {a,b,c,...} as a set -->
  <xsl:template match="mfenced[$s:assume-braces-set and @open='{' and @close='}']" mode="pmathml-to-cmathml">
    <!-- We treat this as a set of elements -->
    <set>
      <xsl:apply-templates mode="pmathml-to-cmathml"/>
    </set>
  </xsl:template>

  <!-- Failure fallback for other types of fences -->
  <xsl:template match="mfenced" mode="pmathml-to-cmathml">
    <!-- Failure: can't handle this type of fence -->
    <xsl:copy-of select="s:make-error('UCEG02', ., (@open, @close))"/>
  </xsl:template>

  <!-- Numbers. TODO: Different notations? -->
  <xsl:template match="mn" mode="pmathml-to-cmathml">
    <cn><xsl:value-of select="."/></cn>
  </xsl:template>

  <!-- Identifiers -->
  <xsl:template match="mi" mode="pmathml-to-cmathml">
    <ci><xsl:value-of select="."/></ci>
  </xsl:template>

  <!-- Fractions -->
  <xsl:template match="mfrac" mode="pmathml-to-cmathml">
    <!-- Fractions are relatively easy to cope with here! -->
    <apply>
      <divide/>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- (Optional) Treat $e^x$ as exponential -->
  <xsl:template match="msup[*[1][self::mi and .='e' and $s:assume-exponential-e]]" mode="pmathml-to-cmathml">
    <apply>
      <exp/>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- We interpret <msup/> as a power -->
  <xsl:template match="msup" mode="pmathml-to-cmathml">
    <apply>
      <power/>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- Square roots -->
  <xsl:template match="msqrt" mode="pmathml-to-cmathml">
    <apply>
      <root/>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- nth roots -->
  <xsl:template match="mroot" mode="pmathml-to-cmathml">
    <apply>
      <root/>
      <degree>
        <xsl:call-template name="local:process-group">
          <xsl:with-param name="elements" select="*[2]"/>
        </xsl:call-template>
      </degree>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- Subscripts made of identifiers and numbers only are treated as special identifiers -->
  <xsl:template match="msub[not(*[not(self::mi or self::mn or self::msub)])]" mode="pmathml-to-cmathml">
    <ci>
      <xsl:copy-of select="."/>
    </ci>
  </xsl:template>

  <!-- Special units created using the \units{...} macro -->
  <xsl:template match="mi[@class='MathML-Unit']" mode="pmathml-to-cmathml">
    <semantics definitionURL="http://www.ph.ed.ac.uk/snuggletex/units">
      <csymbol>
        <xsl:value-of select="."/>
      </csymbol>
    </semantics>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Special identifiers -->

  <xsl:template match="mi[.='&#x2205;']" mode="pmathml-to-cmathml">
    <emptyset/>
  </xsl:template>

  <xsl:template match="mi[.='&#x221e;']" mode="pmathml-to-cmathml">
    <infinity/>
  </xsl:template>

  <!-- Optional Special identifiers -->

  <xsl:template match="mi[.='e' and $s:assume-exponential-e]" mode="pmathml-to-cmathml">
    <exponentiale/>
  </xsl:template>

  <xsl:template match="mi[.='i' and $s:assume-imaginary-i]" mode="pmathml-to-cmathml">
    <imaginaryi/>
  </xsl:template>

  <xsl:template match="mi[.='&#x3c0;' and $s:assume-constant-pi]" mode="pmathml-to-cmathml">
    <pi/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Fallback for unsupported MathML elements -->
  <xsl:template match="*" mode="pmathml-to-cmathml">
    <!-- Failure: no support for this element -->
    <xsl:copy-of select="s:make-error('UCEG00', ., (local-name()))"/>
  </xsl:template>

</xsl:stylesheet>
