<!--

Copyright (c) 2008-2011, The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:local="http://www.ph.ed.ac.uk/snuggletex/pmathml-enhancer"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s local"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:import href="pmathml-utilities.xsl"/>
  <xsl:import href="snuggletex-utilities.xsl"/>
  <xsl:import href="upconversion-options.xsl"/>
  <xsl:strip-space elements="m:*"/>

  <!-- ************************************************************ -->

  <!-- Entry point -->
  <xsl:template name="s:enhance-pmathml">
    <xsl:param name="elements" as="element()*"/>
    <xsl:param name="upconversion-options" as="element(s:upconversion-options)"/>
    <xsl:call-template name="local:process-group">
      <xsl:with-param name="elements" select="$elements"/>
      <xsl:with-param name="upconversion-options" select="$upconversion-options" tunnel="yes"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- All pre-defined elementary functions. -->
  <xsl:variable name="local:elementary-functions" as="xs:string+"
    select="('sin', 'cos', 'tan',
             'sec', 'csc' ,'cot',
             'sinh', 'cosh', 'tanh',
             'sech', 'csch', 'coth',
             'arcsin', 'arccos', 'arctan',
             'arcsec', 'arccsc', 'arccot',
             'arcsinh', 'arccosh', 'arctanh',
             'arcsech', 'arccsch', 'arccoth',
             'ln', 'log', 'exp')"/>

  <!-- Other supported pre-defined functions, such as \Re and \Im. -->
  <xsl:variable name="local:other-predefined-functions" as="xs:string+"
    select="('gcd', 'lcm', 'min', 'max', 'det',
             '&#x2111;' (: imaginary part :),
             '&#x211c;' (: real part :)
             )"/>

  <!-- All supported pre-defined functions, based on the above. -->
  <xsl:variable name="local:predefined-functions" as="xs:string+"
    select="($local:elementary-functions, $local:other-predefined-functions)"/>

  <xsl:function name="local:is-predefined-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mi and $local:predefined-functions=string(.)])"/>
  </xsl:function>

  <!--
  Tests for the equivalent of \sin, \sin^{.}, \log_{.}, \log_{.}^{.}, \Re, \Re^{.},
  plus assumed functions and powers of assumed functions.

  The result need not make any actual sense!
  -->
  <xsl:function name="local:is-legal-function-construct" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="upconversion-options" as="element(s:upconversion-options)"/>
    <xsl:sequence select="local:is-predefined-function($element)
      or $element[self::msup and local:is-predefined-function(*[1])]
      or $element[self::msub and *[1][self::mi and .='log']]
      or $element[self::msubsup and *[1][self::mi and .='log']]
      or $element[s:is-assumed-function(., $upconversion-options)]
      or $element[s:is-power(.) and s:is-assumed-function(s:get-power-base(.), $upconversion-options)]
      "/>
  </xsl:function>

  <!--
  All supported relation operators.
  -->
  <xsl:variable name="local:relation-characters" as="xs:string+"
    select="('=', '&lt;', '&gt;', '|',
            '&#x2192;' (: \rightarrow :),
            '&#x21d2;' (: \Rightaroow :),
            '&#x2208;' (: \in :),
            '&#x2209;' (: \not\in :),
            '&#x2224;' (: \not\mid :),
            '&#x2248;' (: \approx :),
            '&#x2249;' (: \not\approx :),
            '&#x2264;' (: \leq :),
            '&#x2265;' (: \geq :),
            '&#x2260;' (: \not= :),
            '&#x2261;' (: \equiv :),
            '&#x2262;' (: \not\equiv :),
            '&#x226e;' (: \not&lt; :),
            '&#x226f;' (: \not&gt; :),
            '&#x2270;' (: \not\leq :),
            '&#x2271;' (: \not\geq :),
            '&#x2282;' (: \subset :),
            '&#x2284;' (: \not\subset :),
            '&#x2286;' (: \subseteq :),
            '&#x2288;' (: \not\subseteq :)
    )"/>

  <!--
  Operators specifying explicit multiplications, such as * and \times
  -->
  <xsl:variable name="local:explicit-multiplication-characters" as="xs:string+"
    select="('*',
            '&#xd7;' (: \times :),
            '&#x22c5;' (: \cdot :)
    )"/>

  <!--
  Operators specifying explicit division. (This doesn't include \frac{}{} which
  is special.)
  -->
  <xsl:variable name="local:explicit-division-characters" as="xs:string+"
    select="('/',
            '&#xf7;' (: \div :)
    )"/>

  <!--
  All supported prefix operators
  -->
  <xsl:variable name="local:prefix-operators" as="xs:string+"
    select="('&#xac;' (: logical not :))"/>

  <!--
  All supported infix operators.

  NOTE: I've listed these in precedence order for readability. The actual ordering
  is not used in the code though.

  NOTE: We're allowing infix operators to act as prefix operators here, even though
  this won't make sense further in the up-conversion process
  -->
  <xsl:variable name="local:infix-operators" as="xs:string+"
    select="(',',
            '&#x2228;' (: \vee :),
            '&#x2227;' (: \wedge :),
            $local:relation-characters,
            '&#x222a;' (: \cup :),
            '&#x2229;' (: \cap :),
            '&#x2216;' (: \setminus :),
            '+', '-',
            $local:explicit-multiplication-characters,
            $local:explicit-division-characters
    )"/>

  <!--
  All supported postfix operators.

  NOTE: Currently, the only postfix operator is factorial, which is handled in a special way.
  But I'll keep this more general logic for the time being as it gives nicer symmetry with prefix
  operators.
  -->
  <xsl:variable name="local:postfix-operators" as="xs:string+"
    select="('!')"/>

  <xsl:function name="local:is-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo])"/>
  </xsl:function>

  <!--
  Tests whether the given element is an <mo/> infix operator as listed above
  -->
  <xsl:function name="local:is-infix-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="local:is-operator($element) and $element=$local:infix-operators"/>
  </xsl:function>

  <!--
  Tests that the given element is an <mo/> infix operator that doesn't immediately
  follow another operator.

  As an example, an input derived from '-1+-1', the '+' matches here, whereas both
  '-' operators do not since they are actually being used in prefix context.
  -->
  <xsl:function name="local:is-strict-infix-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:variable name="previous" as="element()?" select="$element/preceding-sibling::*[1]"/>
    <xsl:sequence select="local:is-infix-operator($element) and not(exists($previous) and local:is-operator($previous))"/>
  </xsl:function>

  <!--
  Tests whether the given element is a particular leading strict infix operator,
  as defined above.
  -->
  <xsl:function name="local:is-matching-strict-infix-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="match" as="xs:string+"/>
    <xsl:sequence select="boolean(local:is-strict-infix-operator($element) and $element=$match)"/>
  </xsl:function>

  <xsl:function name="local:is-factorial-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='!'])"/>
  </xsl:function>

  <xsl:function name="local:is-prefix-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $local:prefix-operators=string(.)])"/>
  </xsl:function>

  <xsl:function name="local:is-prefix-or-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="upconversion-options" as="element(s:upconversion-options)"/>
    <xsl:sequence select="boolean(local:is-legal-function-construct($element, $upconversion-options)
      or local:is-prefix-operator($element))"/>
  </xsl:function>

  <xsl:function name="local:is-postfix-operator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $local:postfix-operators=string(.)])"/>
  </xsl:function>

  <!--
  We'll say that an element starts an "implicit product" group if it is:

  1. The first sibling
  OR 2. The first sibling following an \verb|<mfenced/>|
  OR 3. The first of one or more prefix operator or function siblings
  OR 4. The first non-postfix operator after one or more postfix operator siblings

  Such an element will thus start a subexpression of the form:

  prefix-operator-or-function* implicit-multiplication-operands* postfix-operator*

  Examples (as derived from LaTeX) would identify elements starting the following
  subexpressions:

  xy
  \sin 2x
  x!
  \cos 2(x-y)
  \sin \cos 2x!

  -->
  <xsl:function name="local:is-implicit-product-starter" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:param name="upconversion-options" as="element(s:upconversion-options)"/>
    <xsl:variable name="previous" as="element()?" select="$element/preceding-sibling::*[1]"/>
    <xsl:sequence select="boolean(
      not(exists($previous)) (: case 1 :)
      or ($previous[self::mfenced]) (: case 2 :)
      or (local:is-prefix-or-function($element, $upconversion-options) and not(local:is-prefix-or-function($previous, $upconversion-options))) (: case 3 :)
      or (not(local:is-postfix-operator($element)) and local:is-postfix-operator($previous) (: case 4 :)))"/>
  </xsl:function>

  <!-- ************************************************************ -->

  <!--
  This is the most important template here. It takes a sequence of elements
  (normally siblings) and applies a number of precedence-based tests to decide
  what to do to them.

  Note for those unfamiliar with XSLT that the elements passed here may be a
  strict subset of the children of a particular element, so an expression like
  $elements[1]/preceding-sibling::* may actually be non-empty.
  -->
  <xsl:template name="local:process-group" as="element()*">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., (','))]">
        <!-- Comma operator, which we'll convert into an fence with empty opener and closer -->
        <xsl:call-template name="local:group-infix-comma">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('&#x2228;'))]">
        <!-- Logical Or -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2228;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('&#x2227;'))]">
        <!-- Logical And -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2227;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., $local:relation-characters)]">
        <!-- Relations are all kept at the same precedence level -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="$local:relation-characters"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('&#x222a;'))]">
        <!-- Set Union -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x222a;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('&#x2229;'))]">
        <!-- Set Intersection -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2229;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('&#x2216;'))]">
        <!-- Set Difference -->
        <xsl:call-template name="local:group-left-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('&#x2216;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('+'))]">
        <!-- Addition -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('+')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., ('-'))]">
        <!-- Subtraction -->
        <xsl:call-template name="local:group-left-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="('-')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., $local:explicit-multiplication-characters)]">
        <!-- Explicit Multiplication, detected in various ways -->
        <xsl:call-template name="local:group-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="$local:explicit-multiplication-characters"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[local:is-matching-strict-infix-operator(., $local:explicit-division-characters)]">
        <!-- Explicit Division -->
        <xsl:call-template name="local:group-left-associative-infix-mo">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="match" select="$local:explicit-division-characters"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[self::mspace]">
        <!-- Any <mspace/> is kept but interpreted as an implicit multiplication as well -->
        <xsl:call-template name="local:handle-mspace-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[1][local:is-infix-operator(.)]">
        <!-- An infix operator being used as in prefix context -->
        <xsl:call-template name="local:apply-unary-infix-operator">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($elements) &gt; 1">
        <!-- Need to infer function applications and multiplications, leave other operators as-is -->
        <xsl:call-template name="local:infer-implicit-product-subgroups">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($elements)=1">
        <!-- "Atom" -->
        <xsl:apply-templates select="$elements[1]" mode="enhance-pmathml"/>
      </xsl:when>
      <xsl:when test="empty($elements)">
        <!-- Empty -> empty -->
      </xsl:when>
      <xsl:otherwise>
        <!-- Based on the logic above, this can't actually happen! -->
        <xsl:message terminate="yes">
          Unexpected logic branch in local:process-group template
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Groups the infix comma operator -->
  <xsl:template name="local:group-infix-comma" as="element()*">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <mfenced open="" close="">
      <xsl:for-each-group select="$elements" group-adjacent="local:is-matching-strict-infix-operator(., ',')">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <!-- This is the comma operator, which we will ignore -->
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="s:maybe-wrap-in-mrow">
              <xsl:with-param name="elements" as="element()*">
                <xsl:call-template name="local:process-group">
                  <xsl:with-param name="elements" select="current-group()"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </mfenced>
  </xsl:template>

  <!-- Groups an associative infix <mo/> operator -->
  <xsl:template name="local:group-associative-infix-mo" as="element()*">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="match" as="xs:string+" required="yes"/>
    <xsl:for-each-group select="$elements" group-adjacent="local:is-matching-strict-infix-operator(., $match)">
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <!-- Copy the matching operator -->
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="s:maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="current-group()"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <!-- Groups a left- but not right-associative infix <mo/> operator -->
  <xsl:template name="local:group-left-associative-infix-mo" as="element()*">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="match" as="xs:string+" required="yes"/>
    <xsl:variable name="operators" select="$elements[local:is-matching-strict-infix-operator(., $match)]" as="element()+"/>
    <xsl:variable name="operator-count" select="count($operators)" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$operator-count != 1">
        <!-- Something like 'a o b o c'. We handle this recursively as '(a o b) o c' -->
        <xsl:variable name="last-operator" select="$operators[position()=last()]" as="element()"/>
        <xsl:variable name="before-last-operator" select="$elements[. &lt;&lt; $last-operator]" as="element()+"/>
        <xsl:variable name="after-last-operator" select="$elements[. &gt;&gt; $last-operator]" as="element()*"/>
        <mrow>
          <xsl:call-template name="local:group-left-associative-infix-mo">
            <xsl:with-param name="elements" select="$before-last-operator"/>
            <xsl:with-param name="match" select="$match"/>
          </xsl:call-template>
        </mrow>
        <xsl:copy-of select="$last-operator"/>
        <xsl:call-template name="s:maybe-wrap-in-mrow">
          <xsl:with-param name="elements" as="element()">
            <xsl:call-template name="local:process-group">
              <xsl:with-param name="elements" select="$after-last-operator"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- Only one operator, so it'll be 'a o b' (or more pathologically 'a o').
             We will allow the pathological cases here. -->
        <xsl:variable name="operator" select="$operators[1]" as="element()"/>
        <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $operator]" as="element()*"/>
        <xsl:variable name="right-operand" select="$elements[. &gt;&gt; $operator]" as="element()*"/>
        <xsl:if test="exists($left-operand)">
          <xsl:call-template name="s:maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$left-operand"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
        <xsl:copy-of select="$operator"/>
        <xsl:if test="exists($right-operand)">
          <xsl:call-template name="s:maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$right-operand"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Groups up a prefix operator, provided it is being applied to something -->
  <xsl:template name="local:apply-unary-infix-operator">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:choose>
      <xsl:when test="$elements[2]">
        <mrow>
          <xsl:copy-of select="$elements[1]"/>
          <xsl:call-template name="s:maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="$elements[position()!=1]"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </mrow>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$elements[1]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- <mspace/> as explicit multiplication -->
  <xsl:template name="local:handle-mspace-group" as="element()+">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:for-each-group select="$elements" group-adjacent="boolean(self::mspace)">
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <xsl:copy-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="position()!=1">
            <!-- Add in InvisibleTimes -->
            <mo>&#x2062;</mo>
          </xsl:if>
          <!-- Then process as normal -->
          <xsl:call-template name="s:maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="local:process-group">
                <xsl:with-param name="elements" select="current-group()"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="local:infer-implicit-product-subgroups" as="element()+">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="upconversion-options" as="element(s:upconversion-options)" tunnel="yes"/>
    <xsl:for-each-group select="$elements" group-starting-with="*[local:is-implicit-product-starter(., $upconversion-options)]">
      <!-- Add an invisible times if we're the second multiplicative group -->
      <xsl:if test="position()!=1">
        <mo>&#x2062;</mo>
      </xsl:if>
      <!-- Apply prefix operators and functions from start of group -->
      <xsl:call-template name="s:maybe-wrap-in-mrow">
        <xsl:with-param name="elements" as="element()*">
          <xsl:call-template name="local:apply-prefix-functions-and-operators">
            <xsl:with-param name="elements" select="current-group()"/>
            <xsl:with-param name="upconversion-options" select="$upconversion-options" tunnel="yes"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="local:apply-prefix-functions-and-operators" as="element()+">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:param name="upconversion-options" as="element(s:upconversion-options)" tunnel="yes"/>
    <xsl:variable name="first-element" as="element()" select="$elements[1]"/>
    <xsl:variable name="after-first-element" as="element()*" select="$elements[position()!=1]"/>
    <xsl:choose>
      <xsl:when test="local:is-legal-function-construct($first-element, $upconversion-options) and exists($after-first-element)">
        <!-- This is a (prefix) function application. Copy the operator as-is -->
        <xsl:copy-of select="$first-element"/>
        <!-- Add an "Apply Function" operator -->
        <mo>&#x2061;</mo>
        <!-- Process the rest recursively -->
        <xsl:call-template name="s:maybe-wrap-in-mrow">
          <xsl:with-param name="elements" as="element()+">
            <xsl:call-template name="local:apply-prefix-functions-and-operators">
              <xsl:with-param name="elements" select="$after-first-element"/>
              <xsl:with-param name="upconversion-options" select="$upconversion-options" tunnel="yes"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="local:is-prefix-operator($first-element)">
        <!-- This is a prefix operator. Apply to everything that follows. -->
        <xsl:copy-of select="$first-element"/>
        <xsl:if test="exists($after-first-element)">
          <xsl:call-template name="s:maybe-wrap-in-mrow">
            <xsl:with-param name="elements" as="element()*">
              <xsl:call-template name="local:apply-prefix-functions-and-operators">
                <xsl:with-param name="elements" select="$after-first-element"/>
                <xsl:with-param name="upconversion-options" select="$upconversion-options" tunnel="yes"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- This is everything after any prefixes but before any postfixes -->
        <xsl:call-template name="s:maybe-wrap-in-mrow">
          <xsl:with-param name="elements" as="element()*">
            <xsl:call-template name="local:apply-postfix-operators">
              <xsl:with-param name="elements" select="$elements"/>
              <xsl:with-param name="upconversion-options" select="$upconversion-options" tunnel="yes"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="local:apply-postfix-operators" as="element()*">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:variable name="last-element" as="element()?" select="$elements[position()=last()]"/>
    <xsl:variable name="before-last-element" as="element()*" select="$elements[position()!=last()]"/>
    <xsl:choose>
      <xsl:when test="not(exists($last-element))">
        <!-- Nothing left to do -->
      </xsl:when>
      <xsl:when test="$last-element[local:is-factorial-operator(.)]">
        <!-- The factorial operator only binds to the last resulting subexpression -->
        <xsl:call-template name="local:apply-factorial">
          <xsl:with-param name="elements" as="element()*">
            <xsl:call-template name="local:apply-postfix-operators">
              <xsl:with-param name="elements" select="$before-last-element"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$last-element[local:is-postfix-operator(.)]">
        <!-- General Postfix operator. Bind to everything preceding.
             NOTE: This is not tested yet as we don't have any such operators! -->
        <xsl:call-template name="local:apply-postfix-operators">
          <xsl:with-param name="elements" select="$before-last-element"/>
        </xsl:call-template>
        <xsl:copy-of select="$last-element"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- We're in the "middle" of the expression, which we assume is implicit multiplication -->
        <xsl:call-template name="local:handle-implicit-multiplicative-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Applying a factorial is actually pretty complicated as it only binds with
  the preceding item. We can also have multiple factorials mixed with
  postfix operators at the same level.

  Example 1:

  <mi>x</mi>
  <mo>!</mo>

  becomes:

  <mrow>
    <mi>x</mi>
    <mo>!</mo>
  </mrow>

  Example 2:

  <mn>2</mn>
  <mi>x</mi>
  <mo>!</mo>

  becomes:

  <mn>2</mn>
  <mo>&InvisibleTimes;</mo>
  <mrow>
    <mi>x</mi>
    <mo>!</mo>
  </mrow>

  Example 3:

  <mi>x</mi>
  <mo>!</mo>
  <mo>!</mo>

  becomes:

  <mrow>
    <mrow>
      <mi>x</mi>
      <mo>!</mo>
    </mrow>
    <mo>!</mo>
  </mrow>

  Example 4:

  <mn>2</mn>
  <mi>x</mi>
  <mo>!</mo>
  <mo>!</mo>

  becomes:

  <mn>2</mn>
  <mo>&InvisibleTimes;</mo>
  <mrow>
    <mrow>
      <mi>x</mi>
      <mo>!</mo>
    </mrow>
    <mo>!</mo>
  </mrow>

  Example 5:

  <mn>2</mn>
  <mi>x</mi>
  <mo>!</mo>
  <mo>#</mo> (some postfix operator #)
  <mo>!</mo>

  becomes:

  <mrow>
    <mrow>
      <mrow>
        <mn>2</mn>
        <mo>&InvisibleTimes;</mo>
        <mrow>
          <mi>x</mi>
          <mo>!</mo>
        </mrow>
      </mrow>
      <mo>#</mo>
    </mrow>
    <mo>!</mo>
  </mrow>
  -->
  <xsl:template name="local:apply-factorial" as="element()*">
    <!-- NB: This doesn't include the actual factorial operator itself! -->
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
      <xsl:when test="not(exists($elements))">
        <!-- Unapplied Factorial -->
        <mo>!</mo>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="last-element" as="element()" select="$elements[position()=last()]"/>
        <xsl:variable name="before-last-element" as="element()*" select="$elements[position()!=last()]"/>
        <xsl:choose>
          <xsl:when test="$last-element[self::mrow and not(local:is-postfix-operator($last-element/*[position()=last()]))]
              and not(exists($before-last-element))">
              <!-- This is where we're processing a single <mrow/> whose last element
              is not a postfix operator. In this case, we just re-process by
              descending into it. -->
            <xsl:call-template name="local:apply-factorial">
              <xsl:with-param name="elements" select="$last-element/*"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <!-- Otherwise, bind the factorial only to the last element -->
            <xsl:copy-of select="$before-last-element"/>
            <mrow>
              <xsl:copy-of select="$last-element"/>
              <mo>!</mo>
            </mrow>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="local:handle-implicit-multiplicative-group" as="element()*">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:call-template name="s:maybe-wrap-in-mrow">
      <xsl:with-param name="elements" as="element()*">
        <xsl:for-each select="$elements">
          <xsl:if test="position()!=1">
            <!-- Add an "Invisible Times" -->
            <mo>&#x2062;</mo>
          </xsl:if>
          <!-- Descend into the element itself -->
          <xsl:apply-templates select="." mode="enhance-pmathml"/>
        </xsl:for-each>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- ************************************************************ -->
  <!-- Templates for explicit MathML elements -->

  <!--
  Special template to handle the case where the DOM building process
  has created an "apply function" of the following form:

  <mrow>
    <mi>f</mi>
    <mo>&ApplyFunction;</mo>
    <mfenced>
        ... args ...
    </mfenced>
  </mrow>

  In this case, we keep the same general structure intact but
  descend into enhancing the arguments.

  (I have added this primarily for the MathAssess project, but
  it might have utility with custom DOM handlers as well.)
  -->
  <xsl:template match="mrow[count(*)=3 and *[1][self::mi]
      and *[2][self::mo and .='&#x2061;']
      and *[3][self::mfenced]]" mode="enhance-pmathml" as="element(mrow)">
    <xsl:copy>
      <xsl:copy-of select="*[1]"/>
      <xsl:copy-of select="*[2]"/>
      <mfenced open="{*[3]/@open}" close="{*[3]/@close}">
        <xsl:apply-templates select="*[3]/*" mode="enhance-pmathml"/>
      </mfenced>
    </xsl:copy>
  </xsl:template>

  <!-- We'll strip <mrow/> if it ends up containing a single child element -->
  <xsl:template match="mrow" mode="enhance-pmathml" as="element()">
    <!-- Process contents as normal -->
    <xsl:variable name="processed-contents" as="element()*">
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($processed-contents)=1">
        <xsl:copy-of select="$processed-contents"/>
      </xsl:when>
      <xsl:otherwise>
        <mrow>
          <xsl:copy-of select="$processed-contents"/>
        </mrow>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Container elements with unrestricted content. If these end up containing
  a single <mrow/> then we'll just ignore it and pull in its children
  -->
  <xsl:template match="msqrt" mode="enhance-pmathml" as="element()">
    <!-- Process contents as normal -->
    <xsl:variable name="processed-contents" as="element()*">
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
        <xsl:when test="count($processed-contents)=1 and $processed-contents[1][self::mrow]">
          <xsl:copy-of select="$processed-contents/*"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$processed-contents"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- Default template for other MathML elements -->
  <xsl:template match="*" mode="enhance-pmathml" as="element()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="enhance-pmathml"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" mode="enhance-pmathml" as="text()">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>

