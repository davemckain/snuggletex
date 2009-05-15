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
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="m s xs">

  <xsl:output method="xhtml"/>

  <!-- Need to pass webapp context path so as to locate images and stuff -->
  <xsl:param name="context-path" as="xs:string" required="yes"/>

  <!-- Optional "page type" text to display within page -->
  <xsl:param name="page-type" as="xs:string?" required="no"/>

  <!-- Extract page ID as first <s:pageId/> element -->
  <xsl:variable name="pageId" select="string(/html/body/s:pageId[1])" as="xs:string"/>

  <!-- Extract page title as first <h2/> heading -->
  <xsl:variable name="title" select="string(/html/body/h2[1])" as="xs:string"/>

  <xsl:template match="html">
    <html xml:lang="en" lang="en">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </html>
  </xsl:template>

  <xsl:template match="head">
    <head>
      <!-- Copy any PIs added earlier in the process -->
      <xsl:copy-of select="processing-instruction()"/>
      <title>SnuggleTeX - <xsl:value-of select="$title"/></title>
      <!-- Copy any existing <meta/> elements -->
      <xsl:apply-templates select="meta"/>
      <meta name="description" content="SnuggleTeX Documentation" />
      <meta name="author" content="David McKain" />
      <meta name="publisher" content="The University of Edinburgh" />
      <link rel="stylesheet" href="{$context-path}/includes/core.css" />
      <link rel="stylesheet" href="{$context-path}/includes/website.css" />
      <link rel="stylesheet" href="{$context-path}/includes/snuggletex.css" />
    </head>
  </xsl:template>

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
        <a href="{$context-path}">SnuggleTeX (1.1-SNAPSHOT)</a>
      </h1>
      <div id="content">
        <div id="skipnavigation">
          <a href="#maincontent">Skip Navigation</a>
        </div>
        <div id="navigation">
          <div id="navinner">
            <!-- Standard Navigation -->
            <h2>About SnuggleTeX</h2>
            <ul>
              <li><a class="overview" href="{$context-path}/docs/overview.html">Overview</a></li>
              <li><a class="features" href="{$context-path}/docs/features.html">Features</a></li>
              <li><a class="usecases" href="{$context-path}/docs/use-cases.html">Use Cases</a></li>
              <li><a class="releasenotes" href="{$context-path}/docs/release-notes.html">Release Notes</a></li>
              <li><a href="http://sourceforge.net/project/showfiles.php?group_id=221375">Download from SourceForge.net</a></li>
            </ul>

            <h2>User Guide</h2>
            <ul>
              <li><a class="requirements" href="{$context-path}/docs/requirements.html">Software Requirements</a></li>
              <li><a class="browserRequirements" href="{$context-path}/docs/browser-requirements.html">Browser Requirements</a></li>
              <li><a class="gettingstarted" href="{$context-path}/docs/getting-started.html">Getting Started</a></li>
              <li><a class="usageoverview" href="{$context-path}/docs/usage-overview.html">Usage Overview</a></li>
              <li><a class="minexample" href="{$context-path}/docs/minimal-example.html">Minimal Example</a></li>
              <li><a class="inputs" href="{$context-path}/docs/inputs.html">Inputs</a></li>
              <li><a class="outputs" href="{$context-path}/docs/outputs.html">Outputs</a></li>
              <li><a class="errors" href="{$context-path}/docs/error-reporting.html">Error Reporting</a></li>
              <li><a class="errorCodes" href="{$context-path}/docs/error-codes.html">SnuggleTeX Error Codes</a></li>
              <li><a class="configuration" href="{$context-path}/docs/configuration.html">Configuration</a></li>
              <li><a class="projects" href="{$context-path}/javadoc/">API Documentation</a></li>
            </ul>

            <h2>LaTeX Guide</h2>
            <ul>
              <li><a class="textMode" href="{$context-path}/docs/text-mode.html">Basic Text Mode Commands</a></li>
              <li><a class="mathMode" href="{$context-path}/docs/math-mode.html">Basic Math Mode Commands</a></li>
              <li><a class="verbatimMode" href="{$context-path}/docs/verbatim-mode.html">Verbatim Mode</a></li>
              <li><a class="commands" href="{$context-path}/docs/commands.html">Defining Commands &amp; Environments</a></li>
              <li><a class="xhtmlCommands" href="{$context-path}/docs/xhtml-commands.html">XHTML-related Commands</a></li>
              <li><a class="xmlCommands" href="{$context-path}/docs/xml-commands.html">XML-related Commands</a></li>
            </ul>

            <h2>Demos and Samples</h2>
            <ul>
              <li><a class="samples" href="{$context-path}/docs/latex-samples.html">Web Output Samples</a></li>
              <li><a class="tryout" href="{$context-path}/tryout.xml">Try Out (requires Firefox or IE6/7 with MathPlayer)</a></li>
            </ul>

            <h2>External Links</h2>
            <ul>
              <li><a href="http://sourceforge.net/projects/snuggletex/">SnuggleTeX on SourceForge.net</a></li>
              <li><a href="https://www.wiki.ed.ac.uk/display/Physics/SnuggleTeX">SnuggleTeX Wiki</a></li>
            </ul>
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
          SnuggleTeX Release 1.1-SNAPSHOT &#x2014;
          <a href="{$context-path}/docs/release-notes.html">Release Notes</a>
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

  <!-- Leave out SnuggleTeX metadata -->
  <xsl:template match="s:*"/>

  <xsl:template match="m:math">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Keep any PI's (e.g. for MathPlayer) -->
  <xsl:template match="processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
