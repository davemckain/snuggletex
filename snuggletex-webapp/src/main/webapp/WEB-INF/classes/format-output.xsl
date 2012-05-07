<?xml version="1.0"?>
<!--

$Id$

Stylesheet to soup up the raw output from the SnuggleTeX
process to make a nice web page.

This adds in the rest of the <head/> stuff, standard headers & footers
and the navigation menu.

Relative links are fixed up so they are relative to the supplied
context-path.

Copyright (c) 2008-2011, The University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="s m xs">

  <xsl:import href="base.xsl"/>
  <xsl:output method="xhtml"/>

  <!-- Optional "page type" text to display within page -->
  <xsl:param name="page-type" as="xs:string?" required="no"/>

  <!-- Extract page ID as first <s:pageId/> element. (Overridden by importers where necessary) -->
  <xsl:variable name="pageId" select="string(/html/body/s:pageId[1])" as="xs:string"/>

  <!-- Navigation scheme -->
  <xsl:variable name="navigation" select="document('navigation.xml')/s:navigation/s:section" as="element(s:section)+"/>

  <!-- This page as a "node" in the navigation tree -->
  <xsl:variable name="node" select="$navigation//s:node[@id=$pageId]" as="element(s:node)"/>

  <!-- Extract page title from navigation -->
  <xsl:variable name="title" select="$node/@name" as="xs:string"/>

  <xsl:template match="head">
    <head>
      <!-- Copy anything already added by SnuggleTeX -->
      <xsl:copy-of select="node()"/>
      <meta name="description" content="SnuggleTeX Documentation" />
      <meta name="author" content="David McKain" />
      <meta name="publisher" content="The University of Edinburgh" />
      <title>SnuggleTeX - <xsl:value-of select="$title"/></title>
      <link rel="stylesheet" type="text/css" href="{$context-path}/includes/webapp-base.css" />
      <link rel="stylesheet" type="text/css" href="{$context-path}/includes/content-styles.css" />
      <link rel="stylesheet" type="text/css" href="{$context-path}/includes/snuggletex-webapp.css" />
      <link rel="stylesheet" type="text/css" href="{$context-path}/includes/snuggletex.css" />
      <link rel="stylesheet" type="text/css" href="{$context-path}/includes/jquery/redmond/jquery-ui-1.8.7.custom.css" />
      <script type="text/javascript" src="{$context-path}/includes/jquery/jquery-1.4.4.min.js"></script>
      <script type="text/javascript" src="{$context-path}/includes/jquery/jquery-ui-1.8.7.custom.min.js"></script>
      <script type="text/javascript" src="{$context-path}/includes/snuggletex-webapp.js"></script>
      <xsl:apply-templates select="." mode="extra-head"/>
    </head>
  </xsl:template>

  <!-- Empty template that importes can implement to add stuff to <head/> -->
  <xsl:template match="head" mode="extra-head"/>

  <xsl:template match="body">
    <body id="{$pageId}">
      <div id="container">
        <div id="header">
          <div id="identities">
            <div id="edlogo"><a href="http://www.ed.ac.uk"><img src="{$context-path}/includes/images/ed_logo.gif" alt="University of Edinburgh Logo"/></a></div>
            <div id="edname"><a href="http://www.ed.ac.uk"><img src="{$context-path}/includes/images/uofe2.gif" alt="University of Edinburgh" /></a></div>
            <div id="schoolname"><a href="/"><img src="{$context-path}/includes/images/panda.gif" alt="School of Physics &amp; Astronomy" /></a></div>
          </div>
          <div id="GlobalNav">
            <ul>
              <li class="active"><a href="http://www2.ph.ed.ac.uk/elearning/" class="active"><span>e-Learning</span></a></li>
            </ul>
          </div>
          <div id="utility">
            <a class="utilContact" href="http://www.ph.ed.ac.uk/elearning/contacts/#dmckain">Contact us</a>
          </div>
        </div>
        <div id="sectionHeader">
          <div id="sectionHeaderTitle">
            <a href="{$context-path}">SnuggleTeX (<xsl:value-of select="$snuggletex-version"/>)</a>
          </div>
        </div>
        <div id="contentArea">
          <div id="content">
            <div id="skipnavigation">
              <a href="#maincontent">Skip Navigation</a>
            </div>
            <div id="navigation">
              <div id="navinner">
                <!-- Build navigation -->
                <xsl:apply-templates select="$navigation" mode="make-navigation"/>
              </div>
            </div>
            <div id="maincontent">
              <!-- Create empty container for popups -->
              <div id="popup"></div>

              <!-- Do main content -->
              <div id="maininner">

                <!-- Add page title, and maybe a box about page types -->
                <h2>
                  <xsl:value-of select="$title"/>
                </h2>
                <xsl:if test="exists($page-type)">
                  <div class="note">
                    <strong>NOTE:</strong>
                    This particular rendition of this page has been generated using the
                    <xsl:value-of select="$page-type"/>
                    Web Page Type.
                  </div>
                </xsl:if>

                <!-- Generate page content -->
                <xsl:apply-templates select="." mode="make-content"/>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- Standard Footer -->
      <div id="footer">
        <div id="copyright">
          <div id="footer-text">
            <p>
              SnuggleTeX Release <xsl:value-of select="$snuggletex-version"/> &#x2014;
              <a href="{$context-path}/documentation/release-notes.html">Release Notes</a>
              <br />
              Copyright &#xa9; 2008&#x2012;2011
              <a href="http://www.ph.ed.ac.uk">The School of Physics and Astronomy</a>,
              <a href="http://www.ed.ac.uk">The University of Edinburgh</a>.
              <br />
              For more information, contact
              <a href="http://www.ph.ed.ac.uk/elearning/contacts/#dmckain">David McKain</a>.
            </p>
            <p>
              The University of Edinburgh is a charitable body, registered in Scotland,
              with registration number SC005336.
            </p>
          </div>
          <div class="clearFloat"></div>
        </div>
      </div>
    </body>
  </xsl:template>

  <!-- Builds section in navigation panel -->
  <xsl:template match="s:section" mode="make-navigation">
    <h2><xsl:value-of select="@name"/></h2>
    <ul>
      <xsl:apply-templates select="s:node" mode="make-navigation"/>
    </ul>
  </xsl:template>

  <!-- Builds navigation item in panel -->
  <xsl:template match="s:node" mode="make-navigation">
    <li>
      <xsl:variable name="href" select="s:fix-href(@href)" as="xs:string"/>
      <a href="{$href}">
        <xsl:if test="@id=$pageId">
          <xsl:attribute name="class">selected</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="@name"/>
        <xsl:if test="not(starts-with($href,'/'))">
          <span class="extlink">&#xa0;</span>
        </xsl:if>
      </a>
      <xsl:if test="descendant-or-self::s:node[@id=$pageId] and exists(s:node)">
        <!-- Current page is deeper, so show it as well -->
        <ul>
          <xsl:apply-templates select="s:node" mode="make-navigation"/>
        </ul>
      </xsl:if>
    </li>
  </xsl:template>

  <xsl:template match="body" mode="make-content">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Copy all other HTML as-is -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Do vaguely exciting things to hyperlinks -->
  <xsl:template match="a[@href]">
    <xsl:variable name="href" select="s:fix-href(@href)" as="xs:string"/>
    <a href="{$href}">
      <xsl:copy-of select="@*[not(local-name()='href')]"/>
      <xsl:apply-templates/>
      <xsl:if test="not(starts-with($href,'/'))">
        <span class="extlink">&#xa0;</span>
      </xsl:if>
    </a>
  </xsl:template>

  <!-- Deep Copy MathML -->
  <xsl:template match="m:math">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Keep Processing Instructions -->
  <xsl:template match="processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Leave out SnuggleTeX pageId -->
  <xsl:template match="s:pageId"/>

  <!-- Up-conversion examples -->
  <xsl:template match="s:upConversionExample">
    <!-- NB: Author uses \verb|...| to wrap input, which will have replaced
    spaces with nbsp, so need to undo when producing the form to pass back as
    SnuggleTeX input -->
    <xsl:variable name="input" select="encode-for-uri(replace(., '&#xa0;', ' '))" as="xs:string"/>
    <a class="upconversionExample dialog" title="{.}"
        href="{$context-path}/UpConversionExampleFragment?input={$input}">
      <code><xsl:value-of select="."/></code>
    </a>
  </xsl:template>

  <!-- Fail if any other s:* element gets through, as I'm not expecting that to happen -->
  <xsl:template match="s:*">
    <xsl:message terminate="yes">
      Unexpected element <xsl:copy-of select="."/>
    </xsl:message>
  </xsl:template>

  <xsl:function name="s:fix-href" as="xs:string">
    <xsl:param name="href" as="xs:string"/>
    <xsl:sequence select="if (starts-with($href, 'docs://'))
        then concat($context-path, $navigation//s:node[@id=substring-after($href, 'docs://')]/@href)
      else if (starts-with($href, 'maven://'))
        then concat($maven-site-url, substring-after($href, 'maven://'))
      else if (starts-with($href, '/')) then concat($context-path, $href)
      else $href"/>
  </xsl:function>

</xsl:stylesheet>
