<!--

Copyright (c) 2008-2011, The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:local="http://www.ph.ed.ac.uk/snuggletex/snuggletex-upconverter"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs s m local"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <!-- ************************************************************ -->

  <xsl:import href="snuggletex-utilities.xsl"/>
  <xsl:import href="pmathml-utilities.xsl"/>
  <xsl:import href="pmathml-enhancer.xsl"/>
  <xsl:import href="pmathml-to-cmathml.xsl"/>
  <xsl:import href="cmathml-to-maxima.xsl"/>
  <xsl:import href="upconversion-options.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <!--
  This parameter is an XML <s:upconversion-options/> element mirroring the Java
  UpConversionOptions class in a fairly simple way, which is used to control the
  up-conversion process.

  If you are using this stylesheet in standalone fashion, you can pass your own
  parameter or use the default listed here.

  DEVELOPER NOTE: Make sure the default here is kept in sync with the defaults
  as they are set in Java.
  -->
  <xsl:param name="s:global-upconversion-options" as="element(s:upconversion-options)">
    <s:upconversion-options>
      <s:option name="doContentMathML" value="true"/>
      <s:option name="doMaxima" value="true"/>
      <s:option name="addOptionsAnnotation" value="false"/>
      <s:option name="roundBracketHandling" value="grouping"/>
      <s:option name="roundFenceHandling" value="vector"/>
      <s:option name="squareFenceHandling" value="list"/>
      <s:option name="curlyFenceHandling" value="set"/>
      <s:option name="emptyFenceHandling" value="list"/>
      <s:option name="maximaInverseFunction" value="inverse"/>
      <s:option name="maximaOperatorFunction" value="operator"/>
      <s:option name="maximaUnitsFunction" value="units"/>
      <!--
      The lines commented out below are NOT defaults, but are provided as a quick
      example of how to configure additional assumptions. Please look at the Java
      UpConversionOptionDefinitions class for more information on what is available
      here.
      -->
      <!--
      <s:symbol assume="function"><mi>f</mi></s:symbol>
      <s:symbol assume="exponentialNumber"><mi>e</mi></s:symbol>
      -->
    </s:upconversion-options>
  </xsl:param>

  <!-- ************************************************************ -->

  <xsl:variable name="s:snuggletex-annotation" as="xs:string" select="'SnuggleTeX'"/>
  <xsl:variable name="s:snuggletex-upconversion-options-annotation" as="xs:string" select="'SnuggleTeX-upconversion-options'"/>
  <xsl:variable name="s:latex-annotation" as="xs:string" select="'LaTeX'"/>
  <xsl:variable name="s:content-mathml-annotation" as="xs:string" select="'MathML-Content'"/>
  <xsl:variable name="s:content-failures-annotation" as="xs:string" select="'MathML-Content-upconversion-failures'"/>
  <xsl:variable name="s:maxima-annotation" as="xs:string" select="'Maxima'"/>
  <xsl:variable name="s:maxima-failures-annotation" as="xs:string" select="'Maxima-upconversion-failures'"/>

  <!-- ************************************************************ -->

  <!--
  We will actually traverse the document by sibling recursion so that we
  can always have the most "recently" set upconversion-options to hand at each point.
  -->
  <xsl:template match="/">
    <xsl:apply-templates select="node()[1]" mode="sibling-traversal">
      <xsl:with-param name="current-upconversion-options" select="()"/>
    </xsl:apply-templates>
  </xsl:template>

  <!--
  When we find an <s:upconversion-options/>, leave it out of the result tree but
  make it the current assumption for the next node.
  -->
  <xsl:template match="s:upconversion-options" mode="sibling-traversal">
    <xsl:param name="current-upconversion-options" as="element(s:upconversion-options)?"/>
    <xsl:apply-templates select="following-sibling::node()[1]" mode="sibling-traversal">
      <xsl:with-param name="current-upconversion-options" select="."/>
    </xsl:apply-templates>
  </xsl:template>

  <!--
  When we find an <math/>, let the main processing template handle it
  and then traverse on to next siblings.
  -->
  <xsl:template match="math" mode="sibling-traversal">
    <xsl:param name="current-upconversion-options" as="element(s:upconversion-options)?"/>
    <xsl:apply-templates select="." mode="process-math">
      <xsl:with-param name="current-upconversion-options" select="$current-upconversion-options"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="following-sibling::node()[1]" mode="sibling-traversal">
      <xsl:with-param name="current-upconversion-options" select="$current-upconversion-options"/>
    </xsl:apply-templates>
  </xsl:template>

  <!--
  All other elements will be given a shallow copy, then traverse into children.
  Finally, traverse following siblings.
  -->
  <xsl:template match="*" mode="sibling-traversal">
    <xsl:param name="current-upconversion-options" as="element(s:upconversion-options)?"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()[1]" mode="sibling-traversal">
        <xsl:with-param name="current-upconversion-options" select="$current-upconversion-options"/>
      </xsl:apply-templates>
    </xsl:copy>
    <xsl:apply-templates select="following-sibling::node()[1]" mode="sibling-traversal">
      <xsl:with-param name="current-upconversion-options" select="$current-upconversion-options"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Keep text, comments and PIs intact -->
  <xsl:template match="text()|comment()|processing-instruction()" mode="sibling-traversal">
    <xsl:param name="current-upconversion-options" as="element(s:upconversion-options)?"/>
    <xsl:copy-of select="."/>
    <xsl:apply-templates select="following-sibling::node()[1]" mode="sibling-traversal">
      <xsl:with-param name="current-upconversion-options" select="$current-upconversion-options"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="math" mode="process-math" as="element(math)">
    <!-- Current in-scope options -->
    <xsl:param name="current-upconversion-options" as="element(s:upconversion-options)?"/>

    <!-- Merge these options with global ones passed from Java -->
    <xsl:variable name="effective-options" select="local:compute-effective-upconversion-options($current-upconversion-options)" as="element(s:upconversion-options)"/>
    <xsl:variable name="do-content-mathml" select="s:get-boolean-option($effective-options, 'doContentMathML')" as="xs:boolean"/>
    <xsl:variable name="do-maxima" select="s:get-boolean-option($effective-options, 'doMaxima')" as="xs:boolean"/>
    <xsl:variable name="add-options-annotation" select="s:get-boolean-option($effective-options, 'addOptionsAnnotation')" as="xs:boolean"/>

    <!-- Extract the actual PMathML content and any existing annotations.
         (The criterion for whether there are any top level annotations will
         be that we have a <semantics/> element with at least 2 children.) -->
    <xsl:variable name="presentation-mathml" select="if (semantics[*[2]]) then (if (semantics/mrow) then semantics/mrow/* else semantics/*[1]) else *" as="element()*"/>
    <xsl:variable name="annotations" select="if (semantics[*[2]]) then semantics/*[position() != 1] else ()" as="element()*"/>

    <!-- We always perform enhancement of the Presentation MathML, creating a new Document Node -->
    <xsl:variable name="enhanced-pmathml">
      <xsl:call-template name="s:enhance-pmathml">
        <xsl:with-param name="elements" select="$presentation-mathml"/>
        <xsl:with-param name="upconversion-options" select="$effective-options"/>
      </xsl:call-template>
    </xsl:variable>

    <!-- Maybe convert Presentation MathML to Content MathML, creating another new Document Node -->
    <xsl:variable name="cmathml">
      <xsl:if test="$do-content-mathml or $do-maxima">
        <xsl:call-template name="s:pmathml-to-cmathml">
          <xsl:with-param name="elements" select="$enhanced-pmathml/*"/>
          <xsl:with-param name="upconversion-options" select="$effective-options"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>

    <!-- Extract any failures in this process -->
    <xsl:variable name="cmathml-failures" as="element(s:fail)*">
      <xsl:copy-of select="$cmathml//s:fail"/>
    </xsl:variable>

    <!-- Maybe convert Content MathML to Maxima input. (This is normally a sequence of
         xs:string but might contain failure elements as well so we need to be careful) -->
    <xsl:variable name="maxima-raw" as="item()*">
      <xsl:choose>
        <xsl:when test="not($do-maxima) or exists($cmathml-failures)">
          <!-- Don't bother converting if asked not to or if we failed earlier on -->
          <xsl:sequence select="()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="s:cmathml-to-maxima">
            <xsl:with-param name="elements" select="$cmathml/*"/>
            <xsl:with-param name="upconversion-options" select="$effective-options"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- Extract any failures arising here -->
    <xsl:variable name="maxima-failures" as="element(s:fail)*">
      <xsl:copy-of select="$maxima-raw[self::s:fail]"/>
    </xsl:variable>

    <!-- Formulate the final Maxima string, stripping off the outer pair of brackets
         if present. (This is sane as if they occur then they bracket the entire expression. -->
    <xsl:variable name="maxima-with-brackets" as="xs:string?"
      select="if (exists($maxima-failures)) then () else string-join($maxima-raw, '')"/>
    <xsl:variable name="maxima" as="xs:string?"
      select="if (exists($maxima-failures)) then () else
        if (starts-with($maxima-with-brackets, '(') and ends-with($maxima-with-brackets, ')'))
        then substring($maxima-with-brackets, 2, string-length($maxima-with-brackets) - 2)
        else $maxima-with-brackets"/>

    <!-- Build up the resulting MathML math element -->
    <xsl:variable name="result" as="element(math)">
      <math>
        <xsl:copy-of select="@*"/>
        <xsl:choose>
          <xsl:when test="$add-options-annotation or $do-content-mathml or $do-maxima or exists($annotations)">
            <!-- We're definitely going to be doing annotations here! -->
            <semantics>
              <!-- Put in the enhanced Presentation MathML first -->
              <xsl:call-template name="s:maybe-wrap-in-mrow">
                <xsl:with-param name="elements" select="$enhanced-pmathml/*"/>
              </xsl:call-template>
              <!-- Maybe add Content MathML or failure annotation -->
              <xsl:choose>
                <xsl:when test="exists($cmathml-failures)">
                  <annotation-xml encoding="{$s:content-failures-annotation}">
                    <xsl:copy-of select="$cmathml-failures"/>
                  </annotation-xml>
                </xsl:when>
                <xsl:when test="$do-content-mathml">
                  <annotation-xml encoding="{$s:content-mathml-annotation}">
                    <xsl:copy-of select="$cmathml/*"/>
                  </annotation-xml>
                </xsl:when>
              </xsl:choose>
              <!-- Copy existing annotations -->
              <xsl:copy-of select="$annotations"/>
              <!-- Copy any existing "SnuggleTeX" annotation as a "LaTeX" annotation -->
              <xsl:if test="$annotations[self::annotation and @encoding=$s:snuggletex-annotation]">
                <annotation encoding="{$s:latex-annotation}">
                  <xsl:value-of select="$annotations[self::annotation and @encoding=$s:snuggletex-annotation][1]"/>
                </annotation>
              </xsl:if>
              <!-- Maybe add Maxima or failure annotation -->
              <xsl:choose>
                <xsl:when test="exists($maxima-failures)">
                  <annotation-xml encoding="{$s:maxima-failures-annotation}">
                    <xsl:copy-of select="$maxima-failures"/>
                  </annotation-xml>
                </xsl:when>
                <xsl:when test="$do-maxima and not(exists($cmathml-failures))">
                  <annotation encoding="{$s:maxima-annotation}">
                    <xsl:value-of select="$maxima"/>
                  </annotation>
                </xsl:when>
              </xsl:choose>
              <!-- Maybe add assumptions annotation -->
              <xsl:if test="$add-options-annotation">
                <annotation-xml encoding="{$s:snuggletex-upconversion-options-annotation}">
                  <xsl:apply-templates select="$effective-options" mode="apply-snuggletex-prefix"/>
                </annotation-xml>
              </xsl:if>
            </semantics>
          </xsl:when>
          <xsl:otherwise>
            <!-- All we did was enhance the PMathML and there are no annotations. For niceness,
            we'll strip off a top-level <mrow/> if it is deemed to be redundant -->
            <xsl:choose>
              <xsl:when test="$enhanced-pmathml[count(*)=1 and *[1][self::mrow]]">
                <xsl:copy-of select="$enhanced-pmathml/mrow/*"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:copy-of select="$enhanced-pmathml/*"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </math>
    </xsl:variable>
    <!-- Finally we fix up the resulting MathML to make sure all MathML elements have the
         same namespace prefix as the element originally matched.  -->
    <xsl:choose>
      <xsl:when test="name()!=local-name()">
        <!-- A prefix is being used, so apply prefixes to all elements -->
        <xsl:variable name="mathml-prefix" select="substring-before(name(), ':')" as="xs:string"/>
        <xsl:apply-templates select="$result" mode="apply-mathml-prefix">
          <xsl:with-param name="prefix" select="$mathml-prefix"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <!-- No prefixing, so just return result as-is -->
        <xsl:copy-of select="$result"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- Helper function to compute the effective up-conversion options from the given
  instance specified in the original LaTeX, applying global values if nothing has been
  overridden.
  -->
  <xsl:function name="local:compute-effective-upconversion-options" as="element(s:upconversion-options)">
    <xsl:param name="current-upconversion-options" as="element(s:upconversion-options)?"/>
    <xsl:choose>
      <xsl:when test="exists($current-upconversion-options)">
        <!-- Merge current options over global options. -->
        <s:upconversion-options>
          <!-- Options have fixed names, so can iterate over one -->
          <xsl:for-each select="$s:global-upconversion-options/s:option">
            <xsl:variable name="global" select="." as="element(s:option)"/>
            <xsl:variable name="override" select="$current-upconversion-options/s:option[@name=$global/@name]" as="element(s:option)?"/>
            <xsl:sequence select="if (exists($override)) then $override else $global"/>
          </xsl:for-each>
          <!-- Symbol assumptions can be defined arbitrarily, so we take local ones
          and then globals that don't have a local override -->
          <xsl:copy-of select="$current-upconversion-options/s:symbol"/>
          <xsl:for-each select="$s:global-upconversion-options/s:symbol">
            <xsl:variable name="global" select="." as="element(s:symbol)"/>
            <xsl:variable name="override" select="$current-upconversion-options/s:symbol[deep-equal($global/*, *)]" as="element(s:symbol)?"/>
            <xsl:sequence select="if (exists($override)) then () else $global"/>
          </xsl:for-each>
        </s:upconversion-options>
      </xsl:when>
      <xsl:otherwise>
        <!-- No current options, so just use globals as-is -->
        <xsl:sequence select="$s:global-upconversion-options"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="m:*" mode="apply-mathml-prefix">
    <xsl:param name="prefix" as="xs:string"/>
    <xsl:element name="{concat($prefix, ':', local-name())}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="apply-mathml-prefix">
        <xsl:with-param name="prefix" select="$prefix"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="node()" mode="apply-mathml-prefix">
    <xsl:param name="prefix" as="xs:string"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="apply-mathml-prefix">
        <xsl:with-param name="prefix" select="$prefix"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="s:*" mode="apply-snuggletex-prefix">
    <xsl:element name="s:{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="apply-snuggletex-prefix"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="node()" mode="apply-snuggletex-prefix">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="apply-snuggletex-prefix"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
