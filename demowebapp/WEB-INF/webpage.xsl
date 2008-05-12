<?xml version="1.0"?>
<!--

$Id: header.jspf 2712 2008-03-10 17:01:01Z davemckain $

Stylesheet to apply UoE headers and footers to SnuggleTeX web page output

Copyright (c) 2008 University of Edinburgh.
All Rights Reserved

-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="html">

  <!-- Need to pass webapp context path so as to locate images and stuff -->
  <xsl:param name="context-path"/>

  <xsl:template match="html:*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="html:body">
    <body>
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
            <h1>SCHOOL OF PHYSICS</h1>
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
                    alt="" />School of Physics Homepage</a>
                  <br />
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <h1 id="location">
        <a href="{$context-path}">
          SnuggleTeX (v@uk.ac.ed.ph.snuggletex.version@)
        </a>
      </h1>
      <div id="content">
        <xsl:apply-templates select="." mode="make-content"/>
      </div>
      <div id="copyright">
        SnuggleTeX Release @uk.ac.ed.ph.snuggletex.version@
        <br />
        Copyright &#xa9; 2008
        <a href="http://www.ph.ed.ac.uk">The School of Physics</a>,
        <a href="http://www.ed.ac.uk">The University of Edinburgh</a>.
        <br />
        For more information, contact
        <a href="http://www.ph.ed.ac.uk/elearning/contacts/#dmckain">David McKain</a>.
      </div>
    </body>
  </xsl:template>

  <xsl:template match="html:body" mode="make-content">
    <xsl:copy-of select="node()"/>
  </xsl:template>

  <xsl:template match="html:h1">
    <h2>
      <xsl:apply-templates/>
    </h2>
  </xsl:template>

  <xsl:template match="html:h2">
    <h3>
      <xsl:apply-templates/>
    </h3>
  </xsl:template>

</xsl:stylesheet>
