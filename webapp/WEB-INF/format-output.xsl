<?xml version="1.0"?>
<!--

$Id: header.jspf 2712 2008-03-10 17:01:01Z davemckain $

Stylesheet to soup up the raw output from the SnuggleTeX
process to make a nice web page.

This adds in the rest of the <head/> stuff, standard headers & footers
and the navigation menu.

Relative links are fixed up so they are relative to the supplied
context-path.

Copyright (c) 2008 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="h m s xs">

  <xsl:output method="xhtml"/>

  <!-- Extract page ID as first <s:pageId/> element -->
  <xsl:variable name="pageId" select="string(/h:html/h:body/s:pageId[1])" as="xs:string"/>

  <!-- Extract page title as first <h2/> heading -->
  <xsl:variable name="title" select="string(/h:html/h:body/h:h2[1])" as="xs:string"/>

  <!-- Need to pass webapp context path so as to locate images and stuff -->
  <xsl:param name="context-path" as="xs:string" required="yes"/>

  <xsl:template match="h:html">
    <html xml:lang="en" lang="en">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </html>
  </xsl:template>

  <xsl:template match="h:head">
    <head>
      <!-- Copy any PIs added earlier in the process -->
      <xsl:copy-of select="processing-instruction()"/>
      <title>SnuggleTeX - <xsl:value-of select="$title"/></title>
      <!-- Copy any existing <meta/> elements -->
      <xsl:apply-templates select="h:meta"/>
      <meta name="description" content="SnuggleTeX Documentation" />
      <meta name="author" content="David McKain" />
      <meta name="publisher" content="The University of Edinburgh" />
      <link rel="stylesheet" href="{$context-path}/includes/core.css" />
      <link rel="stylesheet" href="{$context-path}/includes/website.css" />
      <link rel="stylesheet" href="{$context-path}/includes/snuggletex.css" />
    </head>
  </xsl:template>

  <xsl:template match="h:body">
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
          <td align="right">
            <table border="0" cellpadding="0" cellspacing="0" style="margin-right:20px;">
              <tr>
                <td>
                  <a href="http://www.ed.ac.uk" class="headertext"><img
                    src="{$context-path}/includes/arrow.gif"
                    width="16" height="11" border="0"
                    alt="" />University&#160;Homepage</a>
                  <br />
                  <a href="http://www.ph.ed.ac.uk" class="headertext"><img
                    src="{$context-path}/includes/arrow.gif"
                    width="16" height="11" border="0"
                    alt="" />School of Physics and Astronomy Homepage</a>
                  <br />
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <h1 id="location">
        <a href="{$context-path}">SnuggleTeX (@uk.ac.ed.ph.snuggletex.version@)</a>
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
              <li><a class="gettingstarted" href="{$context-path}/docs/getting-started.html">Getting Started</a></li>
              <li><a class="usageoverview" href="{$context-path}/docs/usage-overview.html">Usage Overview</a></li>
              <li><a class="minexample" href="{$context-path}/docs/minimal-example.html">Minimal Example</a></li>
              <li><a class="inputs" href="{$context-path}/docs/inputs.html">Inputs</a></li>
              <li><a class="outputs" href="{$context-path}/docs/outputs.html">Outputs</a></li>
              <li><a class="errors" href="{$context-path}/docs/error-reporting.html">Error Reporting</a></li>
              <li><a class="configuration" href="{$context-path}/docs/configuration.html">Configuration</a></li>
              <li><a class="projects" href="{$context-path}/javadoc/">API Documentation</a></li>
            </ul>

            <h2>Demos and Samples</h2>
            <ul>
              <li><a class="samples" href="{$context-path}/docs/latex-samples.html">LaTeX Samples</a></li>
              <li><a class="tryout" href="{$context-path}/tryout.xml">Try Out</a></li>
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
          SnuggleTeX Release @uk.ac.ed.ph.snuggletex.version@ &#x2014;
          <a href="{$context-path}/docs/release-notes.html">Release Notes</a>
          <br />
          Copyright &#xa9; 2008
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

  <xsl:template match="h:body" mode="make-content">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Copy all other HTML and MathML as-is -->
  <xsl:template match="h:*">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="m:math">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Keep any PI's (e.g. for MathPlayer) -->
  <xsl:template match="processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- Leave out SnuggleTeX metadata -->
  <xsl:template match="s:*"/>

</xsl:stylesheet>
