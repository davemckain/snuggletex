#!/usr/bin/perl -w

use strict;

my $file = shift;
local $/ = undef;

open F, "<$file" or die $!;
my $data = <F>;
close F or die $!;

my %mapping = (
  "overview.html", "overview",
  "use-cases.html", "usecases",
  "license.html", "license",
  "release-notes.html", "releasenotes",
  "/MathInputDemo", "mathInputDemo",
  "/FullLaTeXInputDemo", "fullLaTeXInputDemo",
  "/UpConversionDemo", "upConversionDemo",
  "/ASCIIMathMLUpConversionDemo", "asciiMathMLUpConversionDemo",
  "latex-samples.html", "samples",
  "requirements.html", "requirements",
  "getting-started.html", "gettingstarted",
  "first-example.html", "firstExample",
  "basic-usage.html", "basicUsage",
  "inputs.html", "inputs",
  "dom-output.html", "domOutput",
  "web-output.html", "webOutput",
  "web-page-types.html", "webPageTypes",
  "browser-requirements.html", "browserRequirements",
  "legacy-web-pages.html", "legacyWebPages",
  "stylesheets.html", "stylesheets",
  "error-reporting.html", "errors",
  "error-codes.html", "errorCodes",
  "supported-latex.html", "supportedLaTeX",
  "text-mode.html", "textMode",
  "math-mode.html", "mathMode",
  "verbatim-mode.html", "verbatimMode",
  "commands.html", "commands",
  "xhtml-commands.html", "xhtmlCommands",
  "xml-commands.html", "xmlCommands",
  "advanced-usage.html", "advanced",
  "semantic-upconversion.html", "upconversion",
  "pmathml-enhancement.html", "pmathmlEnhancement",
  "content-mathml.html", "cmathml",
  "maxima-input.html", "maxima",
  "upconversion-usage.html", "upconversionUsage",
  "upconversion-failures.html", "upconversionFailures",
);

for ($data) {
  s!(\\href\[.+?\]\{)(.+?)(\})!
    my $url = $2;
    my $id = $mapping{$url};
    if ($id) {
      $url = "docs://$id";
    }
    qq($1$url$3)
   !gse;
}

open F, ">$file" or die "Can't open $file for writing: $!";
print F $data;
close F or die "Error writing $file: $!";
