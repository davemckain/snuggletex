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
  xmlns:local="http://www.ph.ed.ac.uk/snuggletex/pmathml-to-cmathml"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s local"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:import href="common.xsl"/>
  <xsl:strip-space elements="m:*"/>

  <!-- ************************************************************ -->

  <xsl:param name="s:assume-exponential-e" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-imaginary-i" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-constant-pi" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-brackets-vector" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-braces-set" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-square-list" select="true()" as="xs:boolean"/>

  <!-- ************************************************************ -->

  <!-- Entry point -->
  <xsl:template name="s:pmathml-to-cmathml" as="element()*">
    <xsl:param name="elements" as="element()*"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="$elements"/>
      <xsl:with-param name="assumptions" select="$assumptions" tunnel="yes"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ************************************************************ -->

  <!--
  Handy utility function for checking whether an element is an <mo/>
  -->
  <xsl:function name="local:is-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo])"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <!--
  Sequence of all "standard" (i.e. non-relation) operators that we support.

  These will match incoming MathML of the form <mo>c</mo> where c is a character in @inputs.
  Doing it like this allows us to easily cope with having multiple input forms for the same
  operator.

  The corresponding @output attribute specifies the name of the resulting Content MathML
  element that will be created.

  The pmathml-enhancer.xsl stylesheet will have grouped these in a suitable precedence order
  already.
  -->
  <xsl:variable name="local:supported-standard-operators" as="element(local:operator)+">
    <local:operator inputs="+" output="plus" nary="true" allow-unary="true"/>
    <local:operator inputs="-" output="minus" nary="false" allow-unary="true"/>
    <local:operator inputs="*&#xd7;&#x22c5;&#x2062;" output="times" nary="true" allow-unary="false"/>
    <local:operator inputs="/&#xf7;" output="divide" nary="false" allow-unary="false"/>
    <local:operator inputs="&#x2228;" output="or" nary="true" allow-unary="false"/>
    <local:operator inputs="&#x2227;" output="and" nary="true" allow-unary="false"/>
    <local:operator inputs="&#x222a;" output="union" nary="true" allow-unary="false"/>
    <local:operator inputs="&#x2229;" output="intersect" nary="true" allow-unary="false"/>
    <local:operator inputs="&#x2216;" output="setdiff" nary="false" allow-unary="false"/>
  </xsl:variable>

  <xsl:variable name="local:standard-operator-characters" as="xs:string"
    select="string-join($local:supported-standard-operators/@inputs, '')"/>

  <xsl:function name="local:is-standard-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean(local:is-operator($element) and contains($local:standard-operator-characters, string($element)))"/>
  </xsl:function>

  <xsl:function name="local:is-matching-standard-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="operator" as="element(local:operator)"/>
    <xsl:sequence select="boolean(local:is-operator($element) and contains($operator/@inputs, string($element)))"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <!--
  Sequence of all supported relation operators.

  This includes support for negating operators using <not/>, if required.
  -->
  <xsl:variable name="local:supported-relation-operators" as="element(local:operator)+">
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

  <xsl:function name="local:is-relation-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="local:is-operator($element)
      and ($local:supported-relation-operators[@input=string($element)]
        or $local:supported-relation-operators[@input-negated=string($element)])"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <xsl:variable name="local:prefix-operators" as="element(local:operator)+">
    <local:operator input="&#xac;" output="not"/>
  </xsl:variable>

  <xsl:function name="local:get-prefix-operator" as="element(local:operator)?">
    <xsl:param name="string" as="xs:string"/>
    <xsl:sequence select="$local:prefix-operators[@input=$string]"/>
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

  <!--
  All supported functions, input as <mi>@input</mi> etc.
  and output using Content MathML elements named according to the
  @output attribute.
  -->
  <xsl:variable name="local:supported-functions" as="element(local:function)+">
    <local:function input="sin" output="sin" inverse-output="arcsin"/>
    <local:function input="cos" output="cos" inverse-output="arccos"/>
    <local:function input="tan" output="tan" inverse-output="arctan"/>
    <local:function input="sec" output="csc" inverse-output="arcsec"/>
    <local:function input="cot" output="cot" inverse-output="arccot"/>
    <local:function input="sinh" output="sinh" inverse-output="arcsinh"/>
    <local:function inout="cosh" output="cosh" inverse-output="arccosh"/>
    <local:function inout="tanh" output="tanh" inverse-output="arctanh"/>
    <local:function input="sech" output="sech" inverse-output="arcsech"/>
    <local:function inout="csch" output="csch" inverse-output="arccsch"/>
    <local:function inout="coth" output="coth" inverse-output="arccoth"/>
    <local:function input="arcsin" output="arcsin"/>
    <local:function input="arccos" output="arccos"/>
    <local:function input="arctan" output="arctan"/>
    <local:function input="arcsec" output="arcsec"/>
    <local:function input="arccsc" output="arccsc"/>
    <local:function input="arccot" output="arccot"/>
    <local:function input="arcsinh" output="arcsinh"/>
    <local:function input="arccosh" output="arccosh"/>
    <local:function input="arctanh" output="arctanh"/>
    <local:function input="arcsech" output="arcsech"/>
    <local:function input="arccsch" output="arccsch"/>
    <local:function input="arccoth" output="arccoth"/>
    <local:function input="ln" output="ln"/>
    <local:function input="log" output="log"/>
    <local:function input="exp" output="exp"/>
    <local:function input="det" output="determinant"/>
    <local:function input="gcd" output="gcd" nary="true"/>
    <local:function input="lcm" output="lcm" nary="true"/>
    <local:function input="max" output="max" nary="true"/>
    <local:function input="min" output="min" nary="true"/>
    <local:function input="&#x2111;" output="imaginary"/>
    <local:function input="&#x211c;" output="real"/>
  </xsl:variable>

  <xsl:function name="local:is-supported-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mi] and $local:supported-functions[@input=string($element)])"/>
  </xsl:function>

  <xsl:function name="local:get-supported-function" as="element(local:function)?">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="if ($element[self::mi])
      then $local:supported-functions[@input=string($element)]
      else ()"/>
  </xsl:function>

  <!--
  Helper to extract all of the "simple" assumed functions specified by the user.
  These are things like 'f' but not 'f_n'.

  TODO: Need to handle f_n and f_n^p as well.
  TODO: Need to signal error if bad arguments are passed.
  -->
  <xsl:function name="local:get-simple-assumed-functions" as="xs:string*">
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:variable name="function-targets" select="$assumptions/s:assume[@property='function']/s:target" as="element(s:target)*"/>
    <xsl:variable name="simple-function-targets" select="$function-targets[mi and count(node())=1]" as="element(s:target)*"/>
    <xsl:sequence select="for $t in $simple-function-targets return string($t/mi)"/>
  </xsl:function>

  <xsl:function name="local:is-simple-assumed-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:sequence select="boolean($element[self::mi and local:get-simple-assumed-functions($assumptions)=string(.)])"/>
  </xsl:function>

  <xsl:function name="local:is-supported-function-construct" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?"/>
    <xsl:sequence select="local:is-supported-function($element)
      or $element[self::msup and local:is-supported-function(*[1])]
      or $element[self::msub and *[1][self::mi and .='log']]
      or $element[self::msubsup and *[1][self::mi and .='log']]
      or local:is-simple-assumed-function($element, $assumptions)
      "/>
  </xsl:function>

  <!-- ************************************************************ -->

  <!-- Application of groups by the precedence order built by pmathml-enhancer.xsl -->
  <xsl:template name="local:process-group" as="element()*">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?" tunnel="yes"/>
    <xsl:choose>
      <xsl:when test="$elements[self::mspace]">
        <!-- Strip off <mspace/> and reapply this template to whatever is left -->
        <xsl:call-template name="local:process-group">
          <xsl:with-param name="elements" select="$elements[not(self::mspace)]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-standard-operator(.)]">
        <!--
        Group contains a standard operator. We'll pull off the first one
        (remember that precedence has already been established) and create an
        appropriate Content MathML construct from that.
        -->
        <xsl:variable name="first-standard-operator" as="element(local:operator)"
          select="$local:supported-standard-operators[some $e in $elements satisfies local:is-matching-standard-operator($e, .)][1]"/>
        <xsl:choose>
          <xsl:when test="$first-standard-operator/@nary='true'">
            <xsl:call-template name="local:handle-standard-nary-operator">
              <xsl:with-param name="elements" select="$elements"/>
              <xsl:with-param name="operator" select="$first-standard-operator"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="local:handle-standard-binary-operator">
              <xsl:with-param name="elements" select="$elements"/>
              <xsl:with-param name="operator" select="$first-standard-operator"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$elements[local:is-relation-operator(.)]">
        <!-- (Possibly mixed) Relation Operators -->
        <xsl:call-template name="local:handle-supported-relation-operators">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[1][local:is-supported-function-construct(., $assumptions)]">
        <!-- Supported function (not necessarily applied) -->
        <xsl:call-template name="local:handle-supported-function-group">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="assumptions" select="$assumptions" tunnel="yes"/>
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
        <!-- Non-function and non-operator "Atom" -->
        <xsl:apply-templates select="$elements[1]" mode="pmathml-to-cmathml"/>
      </xsl:when>
      <xsl:when test="empty($elements)">
        <!-- Empty -> empty -->
      </xsl:when>
      <xsl:otherwise>
        <!-- Fail: unhandled group -->
        <xsl:copy-of select="s:make-error('UCFG01', $elements, ())"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  n-ary standard operator, such as '+', optionally allowed to be used in a unary context.

  This will have been grouped appropriately by the pmathml-enhancer.xsl stylesheet
  so supported legal expressions will always be of one of the following forms:

  1. n1 op1 n2 op2 ... nk (usual infix form)
  2. op (unapplied operator)
  3. op n (if the operator may also be used in unary context)

  We need to be careful to handle unapplied operators and illegal
  unary applications.
  -->
  <xsl:template name="local:handle-standard-nary-operator" as="element()+">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="operator" as="element(local:operator)" required="yes"/>
    <xsl:variable name="cmathml-name" as="xs:string" select="$operator/@output"/>
    <xsl:variable name="allow-unary" as="xs:boolean" select="$operator/@allow-unary='true'"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1 and local:is-matching-standard-operator($elements[1], $operator)">
        <!-- Unapplied operator -->
        <xsl:element name="{$cmathml-name}"/>
      </xsl:when>
      <xsl:when test="count($elements)=2 and local:is-matching-standard-operator($elements[1], $operator)
          and not(local:is-operator($elements[2]))">
        <!-- Prefix context -->
        <xsl:choose>
          <xsl:when test="not($allow-unary)">
            <!-- Fail: operator is not a prefix operator -->
            <xsl:copy-of select="s:make-error('UCFOP0', ., ($cmathml-name))"/>
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
            <xsl:copy-of select="s:make-error('UCFOP1', $elements, ($cmathml-name))"/>
          </xsl:if>
          <xsl:for-each select="$elements">
            <xsl:variable name="i" as="xs:integer" select="position()"/>
            <xsl:choose>
              <xsl:when test="$i mod 2 = 1">
                <!-- Odd position, so expecting operand -->
                <xsl:choose>
                  <xsl:when test="local:is-matching-standard-operator(., $operator)">
                    <!-- Fail: (as above) -->
                    <xsl:copy-of select="s:make-error('UCFOP1', $elements, ($cmathml-name))"/>
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
                <xsl:if test="not(local:is-matching-standard-operator(., $operator))">
                  <!-- Fail: (as above) -->
                  <xsl:copy-of select="s:make-error('UCFOP4', $elements, ())"/>
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
  Binary (and possibly unary) operator, such as '-' or '/'.

  Supported legal expressions will always be of one of the following forms:

  1. n1 op n2 (infix form)
  2. op (unapplied operator)
  3. op n (if the operator may also be used in unary (prefix) context)

  -->
  <xsl:template name="local:handle-standard-binary-operator" as="element()">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="operator" as="element(local:operator)" required="yes"/>
    <xsl:variable name="cmathml-name" as="xs:string" select="$operator/@output"/>
    <xsl:variable name="allow-unary" as="xs:boolean" select="$operator/@allow-unary='true'"/>
    <xsl:variable name="operators" as="element(mo)+" select="$elements[local:is-matching-standard-operator(., $operator)]"/>
    <xsl:variable name="operator-count" as="xs:integer" select="count($operators)"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1 and local:is-matching-standard-operator($elements[1], $operator)">
        <!-- Unapplied operator -->
        <xsl:element name="{$cmathml-name}"/>
      </xsl:when>
      <xsl:when test="count($elements)=2 and local:is-matching-standard-operator($elements[1], $operator)
          and not(local:is-operator($elements[2]))">
        <!-- Unary/prefix context -->
        <xsl:choose>
          <xsl:when test="not($allow-unary)">
            <!-- Fail: operator is not a prefix operator -->
            <xsl:copy-of select="s:make-error('UCFOP0', ., ($cmathml-name))"/>
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
        <xsl:copy-of select="s:make-error('UCFOP3', $elements, ($cmathml-name))"/>
      </xsl:when>
      <xsl:when test="count($elements) &lt; 3 or $elements[position()!=2][local:is-operator(.)] or not($elements[2][local:is-matching-standard-operator(., $operator)])">
        <!-- Fail: bad grouping for binary operator -->
        <xsl:copy-of select="s:make-error('UCFOP2', $elements, ($cmathml-name))"/>
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
  <xsl:template name="local:handle-supported-relation-operators" as="element()+">
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
            <xsl:copy-of select="s:make-error('UCFOP4', $elements, ())"/>
          </xsl:if>
          <xsl:for-each select="$elements">
            <xsl:variable name="i" as="xs:integer" select="position()"/>
            <xsl:choose>
              <xsl:when test="$i mod 2 = 1">
                <!-- Odd position, so expecting operand -->
                <xsl:if test="local:is-relation-operator(.)">
                  <!-- Fail: Relation operators must be strictly infix -->
                  <xsl:copy-of select="s:make-error('UCFOP4', $elements, ())"/>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <!-- Even position, so expecting relation -->
                <xsl:choose>
                  <xsl:when test="not(local:is-relation-operator(.))">
                    <!-- Fail: Relation operators must be strictly infix -->
                    <xsl:copy-of select="s:make-error('UCFOP4', $elements, ())"/>
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
  corresponding to the given <mo/>, wrapping in <not/> if required
  -->
  <xsl:function name="local:create-relation-element" as="element()">
    <xsl:param name="mo" as="element(mo)"/>
    <xsl:param name="arguments" as="element()*"/>
    <xsl:variable name="positive-native" as="xs:string?" select="$local:supported-relation-operators[@input=string($mo)]/@output"/>
    <xsl:variable name="negated-native" as="xs:string?" select="$local:supported-relation-operators[@input-negated=string($mo)]/@output-negated"/>
    <xsl:variable name="negated-synthetic" as="xs:string?" select="$local:supported-relation-operators[@input-negated=string($mo)]/@output"/>
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
  Group containing a supported function, say 'f'.

  Denoting function application as 'o', we have the following possibilities:

  (1) 'f' is unapplied
  (2) 'fox' which is the most common case.
  (3) 'fogox' which is treated as 'fo(gox)'

  -->
  <xsl:template name="local:handle-supported-function-group" as="element()+">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="assumptions" as="element(s:assumptions)?" tunnel="yes"/>
    <xsl:choose>
      <xsl:when test="count($elements)=1">
        <!-- This is case (1) above -->
        <xsl:variable name="function" select="$elements[1]" as="element()"/>
        <xsl:choose>
          <xsl:when test="local:is-simple-assumed-function($function, $assumptions)">
            <!-- Simple function -->
            <ci type="function"><xsl:value-of select="string($function)"/></ci>
          </xsl:when>
          <xsl:otherwise>
            <!-- Pre-defined supported function -->
            <xsl:variable name="function-output" as="element(local:function-mapping)">
              <xsl:call-template name="local:map-supported-function">
                <xsl:with-param name="operator-element" select="$function"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- Just return resulting CMathML as there are no operands here -->
            <xsl:copy-of select="$function-output/local:cmathml/*" copy-namespaces="no"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- This is hopefully (2) or (3). In both cases, the second element must be "apply function" -->
        <xsl:variable name="first-function" select="$elements[1]" as="element()"/>
        <xsl:choose>
          <xsl:when test="not($elements[2][self::mo and .='&#x2061;'])">
            <!-- Fail (unlikely): Expected "apply function" operator as second element -->
            <xsl:copy-of select="s:make-error('UCFFX0', $elements, ())"/>
          </xsl:when>
          <xsl:when test="not(exists($elements[3]))">
            <!-- Fail (unlikely): Nothing following "apply function" operator -->
            <xsl:copy-of select="s:make-error('UCFFX1', $elements, ())"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- This is really (2) or (3)! -->
            <xsl:variable name="first-apply" select="$elements[2]" as="element()"/>
            <xsl:variable name="after-first-apply" select="$elements[position() &gt; 2]" as="element()+"/>
            <xsl:variable name="operands" as="element()*">
              <xsl:call-template name="local:handle-function-operands">
                <xsl:with-param name="after-apply-function" select="$after-first-apply"/>
              </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="local:is-simple-assumed-function($first-function, $assumptions)">
                <!-- Simple function application -->
                <apply>
                  <fn><ci type="function"><xsl:value-of select="string($first-function)"/></ci></fn>
                  <xsl:copy-of select="$operands"/>
                </apply>
              </xsl:when>
              <xsl:otherwise>
                <!-- Pre-defined supported function application -->
                <xsl:variable name="function-output" as="element(local:function-mapping)">
                  <xsl:call-template name="local:map-supported-function">
                    <xsl:with-param name="operator-element" select="$first-function"/>
                  </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="output-form" as="element()+" select="$function-output/local:cmathml/*"/>
                <xsl:variable name="function" as="element(local:function)" select="$function-output/local:function"/>
                <!-- Work out operands -->
                <!-- If 'n' operands, then make sure function is actually nary -->
                <xsl:choose>
                  <xsl:when test="count($operands) &gt; 1 and not($function/@nary='true')">
                    <!-- Fail: Function is not n-ary -->
                    <xsl:copy-of select="s:make-error('UCFFX2', $elements, ($function/local-name(), string(count($operands))))"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- Do application -->
                    <apply>
                      <xsl:copy-of select="$output-form" copy-namespaces="no"/>
                      <xsl:copy-of select="$operands"/>
                    </apply>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Helper to get at the (processed) operands for a function application, treating a single
  <mfenced/> as a container for an nary argument.
  -->
  <xsl:template name="local:handle-function-operands" as="element()*">
    <xsl:param name="after-apply-function" as="element()+"/>
    <xsl:choose>
      <xsl:when test="count($after-apply-function)=1 and $after-apply-function[self::mfenced]">
        <xsl:for-each select="$after-apply-function[1]/*">
          <xsl:call-template name="local:process-group">
            <xsl:with-param name="elements" select="."/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="local:process-group">
          <xsl:with-param name="elements" select="$after-apply-function"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Helper to work out the output form of a supported function, handling cases
  of inverses and powers correctly.

  Returns an element having the following form, to be unwrapped as appropriate
  by the caller:

  <local:function-mapping>
    <local:cmathml>
      1 or more elements giving computed function form (e.g. <sin/>, <apply><power><sin/><cn>2</cn></power></apply> ...)
      OR s:fail
    </local:cmathml>
    <local:function ... /> (as pulled from $local:supported-functions)
  </local:function-mapping>

  This slightly complex output helps the caller handle unary and nary applications.
  -->
  <xsl:template name="local:map-supported-function" as="element(local:function-mapping)">
    <xsl:param name="operator-element" as="element()" required="yes"/>
    <xsl:choose>
      <xsl:when test="$operator-element[self::msup and *[1][self::mi] and *[2][self::mn and .='-1']]">
        <!-- It looks like an inverse function. Make sure we know about it -->
        <xsl:variable name="function" select="local:get-supported-function($operator-element/*[1])" as="element(local:function)"/>
        <local:function-mapping>
          <local:cmathml>
            <xsl:choose>
              <xsl:when test="$function/@inverse-output">
                <xsl:element name="{$function/@inverse-output}"/>
              </xsl:when>
              <xsl:otherwise>
                <!-- Fail: Function cannot be inverted -->
                <xsl:copy-of select="s:make-error('UCFFN1', $operator-element, ($function))"/>
              </xsl:otherwise>
            </xsl:choose>
          </local:cmathml>
          <xsl:copy-of select="$function"/>
        </local:function-mapping>
      </xsl:when>
      <xsl:when test="$operator-element[self::msup
          and *[1][self::mi]
          and *[2][self::mn and number(.) &gt;= 1]]">
        <!-- This looks like sin^2, which we will interpret as such -->
        <xsl:variable name="function" select="local:get-supported-function($operator-element/*[1])" as="element(local:function)"/>
        <local:function-mapping>
          <local:cmathml>
            <apply>
              <power/>
              <xsl:element name="{$function/@output}"/>
              <xsl:apply-templates select="$operator-element/*[2]" mode="pmathml-to-cmathml"/>
            </apply>
          </local:cmathml>
          <xsl:copy-of select="$function"/>
        </local:function-mapping>
      </xsl:when>
      <xsl:when test="$operator-element[self::msub
          and *[1][self::mi and .='log']
          and *[2][self::mi or self::mn]]">
        <!-- Log to a different base -->
        <local:function-mapping>
          <local:cmathml>
            <log/>
            <logbase>
              <xsl:apply-templates select="$operator-element/*[2]" mode="pmathml-to-cmathml"/>
            </logbase>
          </local:cmathml>
          <xsl:copy-of select="$local:supported-functions[.[@input='log']]"/>
        </local:function-mapping>
      </xsl:when>
      <xsl:when test="$operator-element[self::msubsup
          and *[1][self::mi and .='log']
          and *[2][self::mi or self::mn]
          and *[3][self::mn and number(.) &gt;=1]]">
        <!-- Log to a different base with a power -->
        <local:function-mapping>
          <local:cmathml>
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
          </local:cmathml>
          <xsl:copy-of select="$local:supported-functions[.[@input='log']]"/>
        </local:function-mapping>
      </xsl:when>
      <xsl:when test="$operator-element[self::mi]">
        <!-- Unapplied case: create Content MathML element with same name as content of <mi/> element -->
        <xsl:variable name="function" select="local:get-supported-function($operator-element)" as="element(local:function)"/>
        <local:function-mapping>
          <local:cmathml>
            <xsl:element name="{$function/@output}"/>
          </local:cmathml>
          <xsl:copy-of select="$function"/>
        </local:function-mapping>
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
  <xsl:template name="local:handle-prefix-group" as="element()">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <!-- Get Content MathML element corresponding to this operator -->
    <xsl:variable name="prefix-operator" as="element(local:operator)" select="local:get-prefix-operator($elements[1])"/>
    <xsl:variable name="cmathml-operator" as="element()">
      <xsl:element name="{$prefix-operator/@output}"/>
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
            <xsl:copy-of select="s:make-error('UCFOP5', $elements, ())"/>
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

  <xsl:template name="local:handle-factorial-group" as="element()">
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
        <xsl:copy-of select="s:make-error('UCFOP5', $elements, ())"/>
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

  <xsl:template match="mrow" mode="pmathml-to-cmathml" as="element()*">
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="*"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Treat a,b,c with no opener and closer as a list -->
  <xsl:template match="mfenced[$s:assume-square-list and @open='' and @close='']" mode="pmathml-to-cmathml" as="element(list)">
    <list>
      <xsl:apply-templates mode="pmathml-to-cmathml"/>
    </list>
  </xsl:template>

  <xsl:template match="mfenced[@open='(' and @close=')' and count(*)=1]" mode="pmathml-to-cmathml" as="element()*">
    <!-- Treat this as (...), which basically means we treat the content as a single group -->
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="*[1]"/>
    </xsl:call-template>
  </xsl:template>

  <!-- (Optional) Treat (a,b,c,...) with more than 1 element as a vector -->
  <xsl:template match="mfenced[$s:assume-brackets-vector and @open='(' and @close=')' and count(*)&gt;1]" mode="pmathml-to-cmathml" as="element(vector)">
    <vector>
      <xsl:apply-templates mode="pmathml-to-cmathml"/>
    </vector>
  </xsl:template>

  <!-- (Optional) Treat [a,b,c,...] as a list -->
  <xsl:template match="mfenced[$s:assume-square-list and @open='[' and @close=']']" mode="pmathml-to-cmathml" as="element(list)">
    <list>
      <xsl:apply-templates mode="pmathml-to-cmathml"/>
    </list>
  </xsl:template>

  <!-- (Optional) Treat {a,b,c,...} as a set -->
  <xsl:template match="mfenced[$s:assume-braces-set and @open='{' and @close='}']" mode="pmathml-to-cmathml" as="element(set)">
    <!-- We treat this as a set of elements -->
    <set>
      <xsl:apply-templates mode="pmathml-to-cmathml"/>
    </set>
  </xsl:template>

  <!-- Failure fallback for other types of fences -->
  <xsl:template match="mfenced" mode="pmathml-to-cmathml" as="element(s:fail)">
    <!-- Failure: can't handle this type of fence -->
    <xsl:copy-of select="s:make-error('UCFG02', ., (@open, @close))"/>
  </xsl:template>

  <!-- Numbers. TODO: Different notations? -->
  <xsl:template match="mn" mode="pmathml-to-cmathml" as="element(cn)">
    <cn><xsl:value-of select="."/></cn>
  </xsl:template>

  <!-- Identifiers -->
  <xsl:template match="mi" mode="pmathml-to-cmathml" as="element(ci)">
    <ci><xsl:value-of select="."/></ci>
  </xsl:template>

  <!-- Fractions -->
  <xsl:template match="mfrac" mode="pmathml-to-cmathml" as="element(apply)">
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

  <!-- (Optional) Treat $e^.$ as exponential -->
  <xsl:template match="msup[*[1][self::mi and .='e' and $s:assume-exponential-e]]" mode="pmathml-to-cmathml" as="element(apply)">
    <apply>
      <exp/>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- We interpret <msup/> as a power -->
  <xsl:template match="msup" mode="pmathml-to-cmathml" as="element(apply)">
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
  <xsl:template match="msqrt" mode="pmathml-to-cmathml" as="element(apply)">
    <apply>
      <root/>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- nth roots -->
  <xsl:template match="mroot" mode="pmathml-to-cmathml" as="element(apply)">
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
  <xsl:template match="msub[not(*[not(self::mi or self::mn or self::msub)])]" mode="pmathml-to-cmathml" as="element(ci)">
    <ci>
      <xsl:copy-of select="."/>
    </ci>
  </xsl:template>

  <!-- We'll allow the same as above if it has a fence as its second argument -->
  <xsl:template match="msub[*[2][self::mfenced] and *[1][self::mi or self::mn or self::msub]]" mode="pmathml-to-cmathml" as="element(ci)">
    <ci>
      <xsl:copy-of select="."/>
    </ci>
  </xsl:template>

  <!-- Special units created using the \units{...} macro -->
  <xsl:template match="mi[@class='MathML-Unit']" mode="pmathml-to-cmathml" as="element(semantics)">
    <semantics definitionURL="http://www.ph.ed.ac.uk/snuggletex/units">
      <csymbol>
        <xsl:value-of select="."/>
      </csymbol>
    </semantics>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Special identifiers -->

  <xsl:template match="mi[.='&#x2205;']" mode="pmathml-to-cmathml" as="element(emptyset)">
    <emptyset/>
  </xsl:template>

  <xsl:template match="mi[.='&#x221e;']" mode="pmathml-to-cmathml" as="element(infinity)">
    <infinity/>
  </xsl:template>

  <!-- Optional Special identifiers -->

  <xsl:template match="mi[.='e' and $s:assume-exponential-e]" mode="pmathml-to-cmathml" as="element(exponentiale)">
    <exponentiale/>
  </xsl:template>

  <xsl:template match="mi[.='i' and $s:assume-imaginary-i]" mode="pmathml-to-cmathml" as="element(imaginaryi)">
    <imaginaryi/>
  </xsl:template>

  <xsl:template match="mi[.='&#x3c0;' and $s:assume-constant-pi]" mode="pmathml-to-cmathml" as="element(pi)">
    <pi/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Fallback for unsupported MathML elements -->
  <xsl:template match="*" mode="pmathml-to-cmathml" as="element(s:fail)">
    <!-- Failure: cannot up-convert this presentation MathML element -->
    <xsl:copy-of select="s:make-error('UCFG00', ., ())"/>
  </xsl:template>

</xsl:stylesheet>
