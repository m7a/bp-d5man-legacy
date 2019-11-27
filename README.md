section: 11
x-masysma-name: d5manlegacytopdf
keywords: ["d5man", "legacy", "d5manexport", "d5man2xml"]
---
Name
====

`d5manlegacytopdf` -- export legacy D5Man files to PDF.

Synopsis
========

	d5manlegacytopdf file.d5i

Description
===========

This script reads `file.d5i` and exports it to file.pdf. It implements a
functionality provided by legacy D5Man which is relevant for large pieces
of text which should remain exportable even after legacy D5Man has been
uninstalled. It simplifies the interface to the legacy D5Man very much by
performing all required actions (query, export, make) in a single invocation.
