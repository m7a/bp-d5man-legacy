---
section: 32
x-masysma-name: d5man/legacy
title: D5Man Legacy Distribution
date: 2019/11/28 09:30:31
lang: en-US
author: ["Linux-Fan, Ma_Sys.ma (Ma_Sys.ma@web.de)"]
keywords: ["d5man", "legacy", "d5manexport", "d5man2xml", "format", "d5man/format"]
x-masysma-version: 1.0.0
x-masysma-copyright: |
  Copyright (c) 2014--2019 Ma_Sys.ma.
  For further info send an e-mail to Ma_Sys.ma@web.de.
---
Overview
========

This is the D5Man Legacy Distribution. It contains a subset of the programs and
scripts developed for the first attempt on realizing a comprehensive information
storage system.

This distribution focuses on the old system's LaTeX/PDF export capabilities and
includes a new script `d5manlegacyconvert.pl` to conveniently invoke the
export functions automatically generating the necessary D5Man configuration for
exporting a given file. Not all functions were retained for the following
reasons:

 * Simplifying compilation and invocation (all database-related parts and
   external library dependencies have been ellided).
 * Removing features which are very likely not useful
 * Removing overly incomplete/very instable features

The primary reason for giving up on the old D5Man's development was exactly the
high complexity and instability of features, thus this legacy distribution
aims at reducing the complexity and providing only features with some extent
of stability such as to conserve a means of processing legacy documents.

The following functions are provided:

 * PDF export of a single page
 * XHTML export of a single page
 * XSLT stylesheet application for a single page
 * automatic attachment conversion using the `rsvg-convert` command.
 * D5Man legacy format to XML conversion using `d5man2xml`

The following functionality is still present but not exposed through any
convenient interface:

 * `d5manioresolve` utility to locate a D5Man file based on its page name
 * `d5manexport` utility including the code for mass-exporting multiple pages

The following functions were available and removed from the distribution:

 * `d5manserver` for live-serving exported pages
 * `d5manmirror` for uploading exported pages via FTP
 * `d5mandb*` commands for managing the D5Man database.
 * `d5manui` and `d5manquery` commands for querying the D5Man database
   (interactively)
 * `d5manview` VIM plugin for syntax highlighting, generation of headings and
   automatic invocation of `d5mandbsync` on page save.
 * `d5mandetach` for operating D5Man in a separate temporary environment.
   This is the default mode of operation for the functionality provided by this
   repository.

The following functions were never completed and are thus not available in
the distribution either:

 * `d5manvalidate` for mass-checking syntax and metadata consistency of all
   pages
 * `d5manimport` for importing large pieces of information (e.g. JDK API
   Documentation) into the D5Man system by converting all source pages to D5Man
   pages.

Compilation and Dependencies
============================

## Dependencies

### Building
 * Ant
 * GCC (C89 compilation)
### Building and Running
 * Java 7 (or higher, tested with Java 8)
### Running
 * POSIX `make`
 * LaTeX
 * Perl

## Compilation

Compile as follows:

	$ ant

Install by building a package (works on Debian with `debuild`):

	$ ant package

Run the installed package by invoking

	$ d5manlegacyconvert

If the package is to be run without being installed, provide the following lines
in `d5manlegacyconvert.pl`:

	my %conf = (
		common_res      => "./d5manlegacycommonres",
		compl_a         => "$newr/root", # XML: io_compl_a / io_compl_b
		compl_b         => "$newr/root",
		d5man2xml       => "./d5manlegacy2xml/d5manlegacy2xml",
		db_search       => "$newr/d5man.conf", # XML: dbloc_real
		db_sync         => ":",
		file_root       => "$newr/root",
		io_resolver     => "./d5manlegacyioresolve/d5manlegacyioresolve",
		media_converter => "/usr/bin/rsvg-convert -f pdf /dev/stdin",
		vim             => "/usr/bin/vim",
		vim_plugin      => "$newr/d5man.conf",
	);

This shows a definition of the binaries relative to the present working
directory which may suffice if `d5manlegacyconvert.pl` is invoked from the
repository's directory. Although never tested, replacing the calls to
`/usr/bin/vim` etc. with suitable Windows paths may make this functionality
available on Windows as well (`vim` is only ever called for XHTML exports).

In case the legacy D5Man binaries are part of the `$PATH` it may suffice to only
configure `common_res`.

## Usage

Although there might be little point in running such a “legacy” software, one
can test the functionality of the program by providing a sample page (e.g.
`test.d5i` and invoking it as follows. See the _Legacy D5Man Format and Design_
section for an example file.

	$ d5manlegacyconvert test.d5i

This should produce file `test.pdf` corresponding to the input file.

Legacy D5Man Format and Design
==============================

This section captures the definition of the legacy D5Man format in its original
text which includes some reasoning about why the specific format was chosen.
The part about the use of D5Man sections has been removed due to being
inconsistent.

## Motivation

Manpages can be considered one of the most efficient, useful and most reliable
way of storing textual information. Their features, however, are severely
limited making Manpages a bad choice for information to be displayed online or
in print form. Also, Manpages are designed to be distributed as part of a
program package which means that there is no workflow to edit them immediately
and efficiently.

Apart from Manpages there are LaTeX and XHTML documents each of which is
designed to be either used for printing or for the web. Although both of those
are useful means of storing information, especially XHTML as webbrowsers are
almost omnipresent, they remain limited to specific target devices and have
their own disadvantages.

The D5Man format has been designed to overcome many of the existing limitations.
For each format, the major disadvantages are listed below

### Manpages
 * limited formatting options
 * not to be edited by the user
 * unreadable markup language
 * sections are designed to be used specifically with UNIX systems

### Websites (XHTML+CSS)
 * no simple means of rendering (Webbrowser with 300 MiB or more RAM required)
 * focuses too hard on layout, too little on content
 * the markup is not nice to read

### LaTeX
 * rendering on textual devices is theorethically possible but there exist few
   tools to handle this situation
 * not useful for web based applications
 * difficult to process automatically (consider multiple pdfLaTeX runs for
   instance)

Concerning the difficult readability of known markup formats, many alternatives
have emerged. Among the most popular is Markdown which is focused on websites.
Among the best of these formats are
[reStructuredText](http://docutils.sourceforge.net/rst.html) and
[Grutatxt](http://triptico.com/docs/grutatxt_markup.html). While both of them
are really good, there are minor disadvantages: reStructuredText can become
difficult to read once links are involved and Grutatxt is a bit too simple for
the task.

Extracting the best experiences from all these formats, the D5Man format has
been developed.

The following ideas have been taken from the other formats

 * a readable markup language (mainly taken from Grutatxt)
 * a means of controlling typrographic specialities like half spaces and forced
   spaces (from LaTeX)
 * a predefined document structure (inspired by Manpages)
 * sectioning after topic (taken from the Manpages)
 * advanced metadata (taken from XHTML compare `<meta .../>`)

Using this combination, the following new features have become possible

 * D5Man Pages can be rendered nicely in
    * Browsers (like XHTML)
    * Terminals (like Manpages)
    * and printed on paper (like LaTeX)
 * D5Man Pages can be edited WYSIWYG as the format is so readable the user can
   be trusted to view it directly.
    * This is further enhanced using syntax highlighting
 * Unlike Manpages or anything else, we do not even need an own program to read
   the files: VIM can do the job very well and if it is not at hand, any text
   editor may do.

## Main Elements

The D5Man format supports the following constructs.

Metadata
:   A D5-Manpage starts with metadata in a key-value syntax with a tab
    separator. Tabs can be repeated as is necessary for a suitable
    representation in your editor. Configure your editor to use 8 spaces per
    tab.
Text
:   Text can be entered without special attention. `\`` can be used to
    encode inline code, commands and filenames (just about any single words
    you would typeset in teletype font). Quotation is entered the same way
    as in LaTeX.
Escaping
:   `\` is used to escape characters if necessary. This allows “`{`” and
    similar characters to be entered directly. Be aware that escaping
    basically tells the parser to just “ignore” any functionality this
    character might have. Thus, if you want to write a text and make
    an immediate note in parenthes you only need to excape the
    first character, because that is the one the parser uses to find links.
    Example: “`HTML\(5) expert`”.
External Markup
:   Although often unreadable, a plaintext-like markup format can not avoid
    interfacing with other markup languages: Using the “LaTeX”-Brackets
    `{` and `}` one can encapsulate LaTeX and XML using `{< ` and ` >}`
    (note that the first has a trailing, the second a leading space). If
    the TeX Math-Mode is entered immediately after the opening bracket,
    it can also be processed for website generation.
Raw text (“Code”)
:   Text which is indented from the left and not part of a list or table is
    considered raw text. The initial level of indentation will be removed
    and the text will otherwise be left untouched.
Links
:   Links can be made in several forms. You can either link to a D5-Manpage
    by entering the name and section in parentheses without a separating
    space. Otherwise you enter the text you want to link and then give the
    URL in parentheses and as a third possibility you can just use
    `url(http://...)` to link to a specific URL.
Lists
:   There are four types of lists which can be nested, except for
    description lists: You can create unordered lists prepending the `*`
    character to your text. Lists are nested via suitable indentation.
    Numbered lists use numbers and a `.` to number the elements just like
    expect them to be used and definition lists are simply a term followed
    by a newline and an indented description. The fourth type of list is a
    so-called “pro-contra” list which you create by using appropriate `+`
    and `-` signs as your list bullet. Nesting description lists with a
    single element and unordered lists is so common that such lists are
    specially treated and called ``titled'' lists. All list types except for
    description lists have to be indented.
Tables
:   Tables use `o` to mark the top and bottom rule and `+` to create a mid
    line. Fields are separated with two spaces.
Sections
:   A section consists of `-` signs from the very beginning of a line then
    a `[`, a space, the section title another space and a terminating
    `]--\n`. You can also create one level of subsections by creating a
    line of text which is underlined with dashes (just like the old
    Ma_Sys.ma Note Format).
Emphasizing
:   Put emphasis on important text by surrounding it with underscores (`_`).
    Emphasis is not recognized _inside_ a word because you need to be able
    to enter things like “Dev_Swap”, “Ma_Zentral” or “Ma_Sys.ma”
Shortcuts
:   Whenever keyboard shortcuts are to be entered, the necessary keys can
    be encoded like `[CTRL]-[X]` to display them as key symbols.
Substitution
:   Special words and parts of text are automatically replaced whith nicer
    symbols for rendering. The table “Symbol replacement” lists all common
    substitutions.
Space control
:   Just like in LaTeX, you can enter forced half spaces using `\,` and
    forced spaces using `~`.

### Symbol replacement

Text         LaTeX               Symbol
-----------  ------------------  ----------
`...`        `\\dots`            ...
`=>`         `$\\Rightarrow$`    ⇒
`->`         `$\\rightarrow$`    →
`<-`         `$\\leftarrow$`     ←
`2^3`        `$2^3$`             2³
` :) `       (smiley)            :)
`3 e {4,5}`  `$3\\in\\{3,5\\}$`  3 ∈ \{3,5\}

## Universal External Markup

The external markup described in this section is recognized by all renderers
except for WYSIWYG.

`{\\img{file}{caption}}`
:   Inserts the image attachment file `file` and associates the textual
    caption `caption`
`{\\code{language}}`
:   Helps the renderer to understand the source code language of following
    codes, i.e. enables syntax highlighting.
`{$...$}`
:   Math-mode-only is also specially recognized.

## Meta fields

After the fields, a meta section may contain any number of LaTeX commands
which are executed before any other LaTeX is processed. If the commands are
all written between `<` and `>` symbols, they are processed as XHTML instead.

`name` (REQ)
:   An internal name for the page. Use `[0-9a-z_/]+` only.
    Imported pages are allowed to also use upper case letters, dots and
    hyphens.
`section` (REQ)
:   A numeric meta field to define the `d5man` section. Use `-1` for TBD.
`description` (REQ)
:   A plaintext description which may not contain control sequences.
`tags` (REQ)
:   Associates tags (form `[0-9a-z_]+`) separated with spaces.
    Tags are used to find the document through the different D5Man search
    functions like the UI, generated websites and `d5manquery`.
`encoding` (RC)
:   Either `utf8` (`UTF-8`, `utf-8`) or `ascii` (`ASCII`). Defaults to
    `utf8`. _WARNING_ The field exists for future usage and reader
    information but is not evaluated by any D5Man applications which are all
    programmed to support UTF-8 and UTF-8 only
`compliance` (REQ)
:   One of (becoming more and more public) `qqvx`, `qqv`, `secret`,
    `restricted`, `confidential`, `personal`, `internal`, `prerelease`,
    `informal`, `public`. Informal and public texts are allowed to be
    published on the internet. Prerelease texts are designed to be published
    sooner or later, internal texts may be shared but not be publicly
    available on the internet, personal texts might be shared but should not
    be included in IAL or Ma_Zentral DVDs, restricted texts are not to be
    shared, secret texts need special care and the two levels below _must_
    be encrypted.
    Repeat: public and informal are online, internal are in Ma_Zentral and
    MDVL, the rest not.
`lang` (REQ)
:   The language the text is written in. This may either be `en` for English
:   or `de` for German. The possibility to use `it` and `fr` is planned.
`creation` (RC)
:   Time of creation (`YYYY[/MM[/DD [HH:mm[:ss]]]]`).
`copyright` (OPT)
:   A copyright statement. The copyright statement may span across multiple
    lines (with the same indentation).
`version` (OPT)
:   Version information in any application-specific format.
`expires` (OPT)
:   Expiration date. Expiration is not defined as the document becoming
    automatically obsolete, but rather intended to be a sort of
    “review required”. Interpretation and usage are up to the user.
    Expired documents can be queried with `d5manquery -e`
`location` (OPT)
:   Redirects D5Man to another page. This may be a `file://` URL which is
    then opened with the default browser.
    (_This field was never implemented in legacy D5Man, but the new
    D5Man processes `x-masysma-redirect` fields with similar semantics_).
`attachments` (OPT)
:   Space separated list of files considered attachments. These may be
    useful for enhancing websites and LaTeX targets w/ resource files.
`web_freq` (OPT)
:   Change Frequency (always, hourly, daily, weekly, monthly, yearly, never)
    for website XML-sitemaps.
`web_priority`
:   Page priority ]0;1[ for website XML-sitemaps.

## Download Fields

In addition to the fields listed above, there are some fields which are
specially designed to be used in conjunction with the Website export to
specify a “download” attached to a page. Unlike an `attachment`, these
downloads are given by URL and not by relative resource. Thus, these fields
are useful for external mirror links as well. Download URLs may not contain
spaces (use URL-encoding if spaces are required).

Finally, pages can support multiple downloads by giving them numbers starting
from 0 or 1 (a digit appended to all download field names, like e.g. `download1`
and `dlink1` for the second download etc.). If this feature is used, all
downloads must be ordered by their number, i.e. all fields for `download1`
occur before any field for `download2` etc. As download numbers are digits,
a maximum of ten downloads (0-9) is supported per page. If the number is
omitted (as is useful for only a single download), the number 0 is assumed.

Downloads can also be “imported/linked” from other pages by referencing their
page name and the download name (as given in the table). These references'
numbers are ignored and they do not count to the limit of ten downloads per
page.

### Additional Fields for Website Downloads

Field       Description
----------  -----------------------------------------------------
`dref`      Reference other download in form `page(section)/name`
`download`  Declares a download (internal name)
`ddescr`    Download description / title (UI name)
`dlink`     Target URL (or JavaScript link etc.)
`dsize`     Download size in KiB
`dchck`     Time last checked (format like `creation` field)
`dver`      Download version (human readable format)
`dchcksm`   SHA-256 of download contents

## Example Document

This section shows a typical legacy D5Man document as a more intuitive
description in addition to the format as described above.

~~~
--------------------------------------------------------------[ Meta ]--

name		test
section		42
description	D5Man Test Document
tags		d5man detached legacy
compliance	public
lang		en
creation	2019/11/29 09:06:28
version		1.0.0

--------------------------------------------------------------[ Test ]--

This is a D5Man Legacy Format example showing some use of the features.
For the repository see url(https://github.com/m7a/bp-d5man-legacy).

Advantages of the Legacy Format
 + Allows for automatic symbol replacement: -> is an arrow.
 + Allows for generation of quotation marks: ``quoted text''
 + Supports efficient inclusion of inline math:
   {$f(x)=m\cdot x+b$}
 + D5Man supports keyboard shortcuts in documentation. Press [CTRL]-[S]
   to stop terminal output and [CTRL]-[Q] to resume.

Problems with the Legacy Format
 - It is incredibly hard to parse
 - Nested lists are supported but need to strictly follow the syntax.
   Odd things like numbered lists with more than 9 entries needing
   leading zeroes to have all numerals being equally wide for instance.
 - There are some bugs in d5man conversion.
 - `e` for replacement by the ``in'' symbol was possibly not the
   smartest choice. There used to be `E` for ``exists'' as well!

Finally, this test document concludes with a table:

        Overview on the executables in D5Man Legacy and New
      ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
        Legacy Original       Legacy Distribution     New Distribution
      +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        `d5manui`             ~                       `d5mantui` (Java)
        `d5manserver`         ~                       `d5manapi` (Erlang)
        `d5man2xml`           `d5manlegacy2xml`       ~
        `d5mancompliancedup`  ~                       ~
        `d5mandbdelete`       ~                       ~
        `d5mandbinit`         ~                       ~
        `d5mandbsync`         ~                       ~
        `d5mandelete`         ~                       ~
        `d5mandetach`         ~                       ~
        `d5manexport`         `d5manlegacyconvert`    ~
        `d5manexportautoftp`  ~                       ~
        `d5manimport`         ~                       ~
        `d5manioresolve`      `d5manlegacyioresolve`  ~
        `d5manmassdelete`     ~                       ~
        `d5manmirror`         ~                       ~
        `d5manquery`          ~                       ~
        `d5manvalidate`       ~                       ~
      ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
~~~

Structure of the Legacy Distribution
====================================

This repository is structured as follows:

`d5manlegacy2xml`
:   Legacy `d5man2xml` application which takes as input a D5Man text
    and produces in output a proprietary XML format for processing by other
    D5Man-related tools (see
    `d5manlegacyexport/ma/d5man/lib/dtd/d5manxml.dtd` for the associated
    DTD including).
`d5manlegacycommonres`
:   Provides logos and resources needed for the export. File
    `template.tex` is the LaTeX template used for the PDF export and
    `website_fs_override` contains a template XML used for the XHTML
    export.
`d5manlegacyexport`
:   Contains all Java parts of this distribution. This includes large parts
    of the old `d5manexport` Java implementation and the `libd5manexport`
    library. Parts relying on external components (such as SnuggleTex for
    LaTeX to MathML conversion) have been removed.
`d5manlegacyioresolve`
:   Legacy `d5manioresolve` application for converting D5Man page names
    to file names. This is retained because it is invoked by the D5Man
    export to find the file names associated to input files.
`stylesheets`
:   Some sample XSLT styles to transform D5Man proprietary XML to
    other text formats. These have been intended to be used in conjunction
    with systems that in turn convert the text formats to HTML. As such, it
    does not produce a _nice_ textual representation but one that will be
    displayed correctly. Some of these conversions require manual
    post-processing for optimal results.
`d5manlegacyconvert.pl`
:   Script to invoke the legacy export without needing to explicitly provide
    all the D5Man file structure, database and configuration file
    (configuration files and file structure are generated on-the-fly).

`d5manlegacyconvert.pl`
=======================

## Name

`d5manlegacyconvert` -- export legacy D5Man files to (mainly) PDF.

## Synopsis

	d5manlegacyconvert file.d5i [opt]
	d5manlegacyconvert file.d5i xslt  style.xsl
	d5manlegacyconvert file.d5i xhtml outdir    [opt]

## Description

One and two argument invocation
:   This script reads `file.d5i` and exports it to file.pdf. It implements a
    functionality provided by legacy D5Man which is relevant for large
    pieces of text which should remain exportable even after legacy
    D5Man has been uninstalled. It simplifies the interface to the legacy
    D5Man very much by performing all required actions (query, export, make)
    in a single invocation.
Three and four argument invocation
:   This exposes the other parts export functionality. It is retained to be
    able to invoke the XSLT transformation for legacy README-files and to
    be able to simplify the transition of website contents.

`[opt]` allows for optional arguments to be passed to the `d5manexport` process.
