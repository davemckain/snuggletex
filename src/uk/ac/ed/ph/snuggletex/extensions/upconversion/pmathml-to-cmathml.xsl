<!--

$Id$

This stylesheet attempts to convert a Presentation MathML <math/>
element to Content MathML, under the core assumption that the mathematics
represented is simple (i.e. elementary functions and operators, plus a
few other things).

Some semantic inference is also performed basic on common conventions,
which can be turned off if required.

TODO: /
TODO: Factorial operator
TODO: Need to trim whitespace from MathML elements when performing comparisons.
TODO: Should we convert subscripted variables to special identifiers?

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

  <!-- ************************************************************ -->

  <xsl:param name="s:assume-exponential-e" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-imaginary-i" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-constant-pi" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-braces-set" select="true()" as="xs:boolean"/>
  <xsl:param name="s:assume-square-list" select="true()" as="xs:boolean"/>

  <!-- ************************************************************ -->

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

  <xsl:function name="sp:is-equals" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='='])"/>
  </xsl:function>

  <xsl:function name="sp:is-addition" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='+'])"/>
  </xsl:function>

  <xsl:function name="sp:is-minus" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='-'])"/>
  </xsl:function>

  <xsl:function name="sp:is-divide" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='/'])"/>
  </xsl:function>

  <xsl:function name="sp:is-implicit-multiplication" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='&#x2062;'])"/>
  </xsl:function>

  <xsl:function name="sp:is-explicit-multiplication" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and (.='*' or .='&#xd7;' or .='&#x22c5;')])"/>
  </xsl:function>

  <xsl:function name="sp:is-multiplication" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="sp:is-implicit-multiplication($element) or sp:is-explicit-multiplication($element)"/>
  </xsl:function>

  <xsl:function name="sp:is-function-application" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .='&#x2061;'])"/>
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
    <s:fail message="No support for this type of fence">
      <xsl:copy-of select="."/>
    </s:fail>
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
        <xsl:with-param name="elements" select="*[1]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

  <!-- nth roots -->
  <xsl:template match="mroot" mode="pmathml-to-cmathml">
    <apply>
      <root/>
      <degree>
        <xsl:call-template name="local:process-group">
          <xsl:with-param name="elements" select="*[1]"/>
        </xsl:call-template>
      </degree>
      <xsl:call-template name="local:process-group">
        <xsl:with-param name="elements" select="*[2]"/>
      </xsl:call-template>
    </apply>
  </xsl:template>

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
    <s:fail message="No support for this Presentation MathML element">
      <xsl:copy-of select="."/>
    </s:fail>
  </xsl:template>

  <!-- ************************************************************ -->

  <!--
  This is the main template for handling a sequence of sibling Nodes.

  We group this to reflect (reverse) implicit precedence as following:

  1. =
  2. +
  3. -
  4. *
  5. function applications

  -->
  <xsl:template name="local:process-group">
    <xsl:param name="elements" as="element()*" required="yes"/>
    <xsl:choose>
      <xsl:when test="$elements[sp:is-equals(.)]">
        <!-- Equals -->
        <xsl:call-template name="local:handle-equals-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[sp:is-addition(.)]">
        <!-- Addition -->
        <xsl:call-template name="local:handle-add-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[sp:is-minus(.)]">
        <!-- Subtraction -->
        <xsl:call-template name="local:handle-minus-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[sp:is-multiplication(.)]">
        <!-- Explicit multiplication -->
        <xsl:call-template name="local:handle-multiplication-group">
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$elements[sp:is-function-application(.)]">
        <!-- Function Application -->
        <xsl:call-template name="local:handle-function-application-group">
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
        <s:fail message="No support for this MathML grouping">
          <xsl:copy-of select="$elements"/>
        </s:fail>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
  Equals Group. This is nice and associative and easy, though we will disallow
  things like 'a=' and '=a'
  -->
  <xsl:template name="local:handle-equals-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <apply>
      <eq/>
      <xsl:for-each-group select="$elements" group-adjacent="sp:is-equals(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:if test="not(following-sibling::*[1])">
              <s:fail message="Nothing followed equals operator">
                <xsl:copy-of select="$elements"/>
              </s:fail>
            </xsl:if>
            <xsl:if test="not(preceding-sibling::*[1])">
              <s:fail message="Nothing preceded equals operator">
                <xsl:copy-of select="$elements"/>
              </s:fail>
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
  </xsl:template>

  <!--
  Addition Expression. This is nice and easy since it is associative.
  We do however need to check for the pathological case of 'a+'
  -->
  <xsl:template name="local:handle-add-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <apply>
      <plus/>
      <xsl:for-each-group select="$elements" group-adjacent="sp:is-addition(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:if test="not(following-sibling::*[1])">
              <s:fail message="Nothing followed addition operator">
                <xsl:copy-of select="$elements"/>
              </s:fail>
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
  </xsl:template>

  <!-- Subtraction Expression. Need to be very careful with this as it is not associative! -->
  <xsl:template name="local:handle-minus-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="minus-count" select="count($elements[sp:is-minus(.)])" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$minus-count != 1">
        <!-- Something like 'a-b-c'. We handle this recursively as '(a-b)-c' -->
        <xsl:variable name="last-minus" select="$elements[sp:is-minus(.)][position()=last()]" as="element()"/>
        <xsl:variable name="before-last-minus" select="$elements[. &lt;&lt; $last-minus]" as="element()+"/>
        <xsl:variable name="after-last-minus" select="$elements[. &gt;&gt; $last-minus]" as="element()*"/>
        <apply>
          <minus/>
          <xsl:call-template name="local:handle-minus-group">
            <xsl:with-param name="elements" select="$before-last-minus"/>
          </xsl:call-template>
          <xsl:call-template name="local:process-group">
            <xsl:with-param name="elements" select="$after-last-minus"/>
          </xsl:call-template>
        </apply>
      </xsl:when>
      <xsl:otherwise>
        <!-- Only one minus, so either '-a' or 'a-b' (or more pathologically '-' or 'a-') -->
        <xsl:variable name="minus" select="$elements[sp:is-minus(.)]" as="element()"/>
        <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $minus]" as="element()*"/>
        <xsl:variable name="right-operand" select="$elements[. &gt;&gt; $minus]" as="element()*"/>
        <xsl:choose>
          <xsl:when test="empty($right-operand)">
            <s:fail message="Nothing followed subtraction operator">
              <xsl:copy-of select="$elements"/>
            </s:fail>
          </xsl:when>
          <xsl:otherwise>
            <apply>
              <minus/>
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

  <!-- Explicit Multiplicative Expression. This is again easy since it is associative. -->
  <xsl:template name="local:handle-multiplication-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <apply>
      <times/>
      <xsl:for-each-group select="$elements" group-adjacent="sp:is-multiplication(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:if test="not(following-sibling::*[1])">
              <s:fail message="Nothing followed multiplication operator">
                <xsl:copy-of select="$elements"/>
              </s:fail>
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
  </xsl:template>

  <!--
  Function Application. Denoting 'o' as 'apply function' here, then 'fogoh'
  is treated as 'fo(goh)' so it's quite easy to implement this.
  -->
  <xsl:template name="local:handle-function-application-group">
    <xsl:param name="elements" as="element()+" required="yes"/>
    <xsl:variable name="first-apply" select="$elements[sp:is-function-application(.)][1]" as="element()"/>
    <xsl:variable name="left-operand" select="$elements[. &lt;&lt; $first-apply]" as="element()+"/>
    <xsl:variable name="after-first-apply" select="$elements[. &gt;&gt; $first-apply]" as="element()*"/>
    <xsl:choose>
      <xsl:when test="count($left-operand)!=1">
        <s:fail message="Expected single element preceding function application">
          <xsl:copy-of select="$elements"/>
        </s:fail>
      </xsl:when>
      <xsl:when test="empty($after-first-apply)">
        <s:fail message="Expected element after function application">
          <xsl:copy-of select="$elements"/>
        </s:fail>
      </xsl:when>
      <xsl:otherwise>
        <apply>
          <xsl:call-template name="local:create-elementary-function-operator">
            <xsl:with-param name="operand-element" select="$left-operand"/>
          </xsl:call-template>
          <xsl:call-template name="local:process-group">
            <xsl:with-param name="elements" select="$after-first-apply"/>
          </xsl:call-template>
        </apply>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="local:create-elementary-function-operator">
    <xsl:param name="operand-element" as="element()" required="yes"/>
    <xsl:choose>
      <xsl:when test="$operand-element[self::msup and *[1][self::mi] and *[2][self::mn and .='-1']]">
        <!-- It looks like an inverse function. Make sure we know about it -->
        <xsl:variable name="function" select="string($operand-element/*[1])" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$sp:invertible-elementary-functions=$function">
            <xsl:element name="arc{$function}"/>
          </xsl:when>
          <xsl:otherwise>
            <s:fail message="Unknown inverse function '{$function}"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$operand-element[self::msup and *[1][self::mi] and *[2][self::mn and number(.) &gt;= 1]]">
        <!-- This looks like sin^2, which we will interpret as such -->
        <xsl:variable name="function" select="string($operand-element/*[1])" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$sp:elementary-functions=$function">
            <apply>
              <power/>
              <xsl:element name="{$function}"/>
              <xsl:apply-templates select="$operand-element/*[2]" mode="pmathml-to-cmathml"/>
            </apply>
          </xsl:when>
          <xsl:otherwise>
            <s:fail message="Unknown function '{$function}'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$operand-element[self::msub and *[1][self::mi and .='log'] and *[2][self::mi or self::mn]]">
        <!-- Log to a different base -->
        <log/>
        <logbase>
          <xsl:apply-templates select="$operand-element/*[2]" mode="pmathml-to-cmathml"/>
        </logbase>
      </xsl:when>
      <xsl:when test="$operand-element[self::mi]">
        <xsl:variable name="function" select="string($operand-element)" as="xs:string"/>
        <xsl:choose>
          <xsl:when test="$sp:elementary-functions=$function">
            <!-- Create Content MathML element with same name as content of <mi/> element -->
            <xsl:element name="{$function}"/>
          </xsl:when>
          <xsl:otherwise>
            <s:fail message="Unknown function '{$function}'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <s:fail message="Unsupported operand element '{$operand-element/local-name()}'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
