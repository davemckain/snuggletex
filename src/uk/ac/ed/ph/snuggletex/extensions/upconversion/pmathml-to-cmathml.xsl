<!--

$Id$

This stylesheet attempts to convert a Presentation MathML <math/>
element to Content MathML, under the core assumption that the mathematics
represented is simple (i.e. elementary functions and operators, plus a
few other things).

Some semantic inference is also performed basic on common conventions,
which can be turned off if required.

TODO: Factorial operator
TODO: Need to trim whitespace from MathML elements when performing comparisons.

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
      <xsl:when test="$elements[local:is-matching-operator(., ('='))]">
        <!-- Equals -->
        <xsl:call-template name="local:handle-nary-operator">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('=')"/>
          <xsl:with-param name="cmathml-name" select="'eq'"/>
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
      <xsl:when test="count($elements)=1">
        <!-- "Atom" -->
        <xsl:apply-templates select="$elements[1]" mode="pmathml-to-cmathml"/>
      </xsl:when>
      <xsl:when test="empty($elements)">
        <!-- Empty -> empty -->
      </xsl:when>
      <xsl:when test="$elements[self::mspace]">
        <!-- Strip off <mspace/> and reapply this template to whatever is left -->
        <xsl:call-template name="local:process-group">
          <xsl:with-param name="elements" select="$elements[not(self::mspace)]"/>
        </xsl:call-template>
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
  n-ary operator, such as '=' or '+'.

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
      <xsl:otherwise>
        <apply>
          <xsl:element name="{$cmathml-name}"/>
          <xsl:for-each-group select="$elements" group-adjacent="local:is-matching-operator(., $match)">
            <xsl:choose>
              <xsl:when test="current-grouping-key()">
                <xsl:if test="not($allow-as-prefix) and not(preceding-sibling::*[1])">
                  <!-- Fail: operator is not a prefix operator -->
                  <xsl:copy-of select="s:make-error('UCEOP0', ., ($cmathml-name))"/>
                </xsl:if>
                <xsl:if test="not(following-sibling::*[1])">
                  <!-- Fail: operator is not a postfix operator -->
                  <xsl:copy-of select="s:make-error('UCEOP1', ., ($cmathml-name))"/>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="local:process-group">
                  <xsl:with-param name="elements" select="current-group()"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each-group>
        </apply>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Binary operator, such as '-' or '/'.

  These may be left unapplied.
  We forbid these from being applied as postfix operators.
  These may be allowed to behave as prefix operators, if specified.
  -->
  <xsl:template name="local:handle-binary-operator">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="match" as="xs:string+" required="yes"/>
    <xsl:param name="cmathml-name" as="xs:string" required="yes"/>
    <xsl:param name="allow-as-prefix" as="xs:boolean" select="false()"/>
    <xsl:variable name="operators" select="$elements[local:is-matching-operator(., $match)]" as="element()?"/>
    <xsl:variable name="operator-count" select="count($operators)" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1 and $operator-count=1">
        <!-- Unapplied operator -->
        <xsl:element name="{$cmathml-name}"/>
      </xsl:when>
      <xsl:when test="$operator-count!=1">
        <!-- Fail: Ungrouped Binary operator (this should not happen) -->
        <xsl:copy-of select="s:make-error('UCEOP2', $elements, ($cmathml-name))"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Only one operator, so either 'op a', 'a op' or 'a op b' -->
        <xsl:variable name="operator" select="$elements[local:is-operator(.)]" as="element()"/>
        <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $operator]" as="element()*"/>
        <xsl:variable name="right-operand" select="$elements[. &gt;&gt; $operator]" as="element()*"/>
        <xsl:choose>
          <xsl:when test="empty($right-operand)">
            <!-- Fail: operator is not a postfix operator -->
            <xsl:copy-of select="s:make-error('UCEOP1', ., ($cmathml-name))"/>
          </xsl:when>
          <xsl:when test="empty($left-operand) and not($allow-as-prefix)">
            <!-- Fail: operator is not a prefix operator -->
            <xsl:copy-of select="s:make-error('UCEOP0', ., ($cmathml-name))"/>
          </xsl:when>
          <xsl:otherwise>
            <apply>
              <xsl:element name="{$cmathml-name}"/>
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$left-operand"/>
              </xsl:call-template>
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$right-operand"/>
              </xsl:call-template>
            </apply>
          </xsl:otherwise>
        </xsl:choose>
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

  <!-- ************************************************************ -->

  <!-- Optional Special constants. -->

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
