<?xml version="1.0"?>
<!--

$Id$

Stylesheet to soup up the raw output from the SnuggleTeX
process to make a nice web page.

This adds in the rest of the <head/> stuff, standard headers & footers
and the navigation menu.

Relative links are fixed up so they are relative to the supplied
context-path.

Copyright (c) 2009 University of Edinburgh.
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

  <xsl:output method="xhtml"/>

  <xsl:param name="snuggletex-version" as="xs:string" required="yes"/>
  <xsl:param name="maven-site-url" as="xs:string" required="yes"/>
  <xsl:param name="context-path" as="xs:string" required="yes"/>

  <!-- Optional "page type" text to display within page -->
  <xsl:param name="page-type" as="xs:string?" required="no"/>

  <!-- Extract page ID as first <s:pageId/> element. (Overridden by importers where necessary) -->
  <xsl:variable name="pageId" select="string(/html/body/s:pageId[1])" as="xs:string"/>

  <!-- Extract page title as first <h2/> heading -->
  <xsl:variable name="title" select="string(/html/body/h2[1])" as="xs:string"/>

  <!-- Navigation scheme -->
  <xsl:variable name="navigation" select="document('navigation.xml')/s:navigation/s:section" as="element(s:section)+"/>

  <xsl:template match="head">
    <head>
      <!-- Copy anything already added by SnuggleTeX -->
      <xsl:copy-of select="node()"/>
      <meta name="description" content="SnuggleTeX Documentation" />
      <meta name="author" content="David McKain" />
      <meta name="publisher" content="The University of Edinburgh" />
      <link rel="stylesheet" href="{$context-path}/includes/core.css" />
      <link rel="stylesheet" href="{$context-path}/includes/website.css" />
      <link rel="stylesheet" href="{$context-path}/includes/snuggletex.css" />
      <title>SnuggleTeX - <xsl:value-of select="$title"/></title>
      <xsl:apply-templates select="." mode="extra-head"/>
    </head>
  </xsl:template>

  <!-- Empty template that importes can implement to add stuff to <head/> -->
  <xsl:template match="head" mode="extra-head"/>

  <xsl:template match="body">
    <body id="{$pageId}">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" id="header">
        <tr>
          <td width="84" align="left" valign="top">
            <a href="http://www.ed.ac.uk" class="headertext"><img
              src="{$context-path}/includes/uoe_logo.jpg"
              alt="The University of Edinburgh" id="logo" name="logo"
              width="84" height="84" border="0" /></a>
          </td>
          <td align="left">
            <h3>THE UNIVERSITY of EDINBURGH</h3>
            <h1>SCHOOL OF PHYSICS AND ASTRONOMY</h1>
          </td>
        </tr>
      </table>
      <h1 id="location">
        <a href="{$context-path}">SnuggleTeX (<xsl:value-of select="$snuggletex-version"/>)</a>
      </h1>
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
          <div id="maininner">
            <!-- Generate page content -->
            <xsl:apply-templates select="." mode="make-content"/>
          </div>
        </div>
      </div>
      <div id="copyright">
        <p>
          SnuggleTeX Release <xsl:value-of select="$snuggletex-version"/> &#x2014;
          <a href="{$context-path}/documentation/release-notes.html">Release Notes</a>
          <br />
          Copyright &#xa9; 2009
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
      <a href="{s:fix-href(@href)}">
        <xsl:if test="@id=$pageId">
          <xsl:attribute name="class">selected</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="@name"/>
        <xsl:if test="descendant-or-self::s:node[@id=$pageId]">
          <!-- Current page is deeper, so show it as well -->
          <ul>
            <xsl:apply-templates select="s:node" mode="make-navigation"/>
          </ul>
        </xsl:if>
      </a>
    </li>
  </xsl:template>

  <xsl:template match="body" mode="make-content">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Maybe soup up the first heading with information about the page type -->
  <xsl:template match="body/h2[1]">
    <h2>
      <xsl:apply-templates/>
      <xsl:if test="$page-type">
        (<xsl:value-of select="$page-type"/>)
      </xsl:if>
    </h2>
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
    <a href="{s:fix-href(@href)}">
      <xsl:copy-of select="@*[not(local-name()='href')]"/>
      <xsl:apply-templates/>
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

  <!-- Leave out SnuggleTeX metadata -->
  <xsl:template match="s:*"/>

  <xsl:function name="s:fix-href" as="xs:string">
    <xsl:param name="href" as="xs:string"/>
    <xsl:sequence select="if (starts-with($href, 'maven://')) then concat($maven-site-url, substring-after($href, 'maven://'))
      else if (starts-with($href, '/')) then concat($context-path, $href) else $href"/>
  </xsl:function>

</xsl:stylesheet>
