#!/usr/bin/perl
my $copyright_and_usage = <<'PLEASE_NOTE';
Ma_Sys.ma D5Man Legacy PDF Export 1.0.0, Copyright (c) 2019 Ma_Sys.ma.
For further info send an e-mail to Ma_Sys.ma@web.de.

USAGE12 d5manlegacyconvert file.d5i [opt]
USAGE3  d5manlegacyconvert file.d5i xslt  style.xsl
USAGE34 d5manlegacyconvert file.d5i xhtml outdir    [opt]
PLEASE_NOTE

# ARGV index               0        1     2         3

#------------------------------------------------------------------[ Imports ]--
use strict;
use warnings FATAL => 'all';
use autodie;

require XML::DOM;              # libxml-dom-perl
require File::Temp;
require File::Copy;
require File::Copy::Recursive; # libfile-copy-recursive-perl
require IPC::Run3;             # libipc-run3-perl
require File::Basename;
require Cwd; # abs_path

use Data::Dumper 'Dumper'; # debug only

#------------------------------------------------------------------[ General ]--

my $argc = scalar @ARGV;
if(($argc < 1) or $ARGV[0] eq "--help") {
	print $copyright_and_usage;
	exit(0);
}

my $fabs = Cwd::abs_path($ARGV[0]);
my $newr = File::Temp->newdir();

#------------------------------------------------------------[ Configuration ]--

my $exportjar = "./d5manlegacyexport/d5manlegacyexport.jar";
my %conf = (
	common_res      => "/usr/share/mdvl/d5man-legacy/d5manlegacycommonres",
	compl_a         => "$newr/root", # XML: io_compl_a / io_compl_b
	compl_b         => "$newr/root",
	d5man2xml       => "d5manlegacy2xml",
	db_search       => "$newr/d5man.conf", # XML: dbloc_real
	db_sync         => ":",
	file_root       => "$newr/root",
	io_resolver     => "d5manlegacyioresolve",
	media_converter => "/usr/bin/rsvg-convert -f pdf /dev/stdin",
	vim             => "/usr/bin/vim",
	vim_plugin      => "$newr/d5man.conf",
);

#-----------------------------------------------------[ Parse with D5Man2XML ]--

# cmd, in, out, [err=undef]
my $d5man2xml_output;
IPC::Run3::run3([$conf{d5man2xml}], $fabs, \$d5man2xml_output);
if($? != 0) {
	print("d5man2xml invocation failed with exit code $?.".
						"See error message above.\n");
	exit(1);
}

my $dom_parser = new XML::DOM::Parser;
my $d5man_doc = $dom_parser->parse($d5man2xml_output);
my $metael = $d5man_doc->getElementsByTagName("meta")->item(0);
my %d5mankv;
my $metakv = $metael->getElementsByTagName("kv");
for(my $i = 0; $i < $metakv->getLength; $i++) {
	my $curkv = $metakv->item($i);
	$d5mankv{$curkv->getAttribute("k")} = $curkv->getAttribute("v");
}
$d5man_doc->dispose();

#---------------------------------------------------[ Generate D5Man FS Tree ]--

my($fname, $fdir, $fext) = File::Basename::fileparse($fabs, qr"\..[^.]*$");

open(my $fh, '>:encoding(UTF-8)', "$newr/d5man.conf");
print $fh join("\n", map { "D5MAN_".uc($_)."=$conf{$_}" } keys %conf);
close($fh);

mkdir("$newr/root");
mkdir("$newr/root/$d5mankv{section}");
mkdir("$newr/out");

my $target = "$newr/root/$d5mankv{section}/$fname$fext";
# autodie does not seem to be invoked here :(
if(File::Copy::copy($fabs, $target) == 0) {
	die("ERROR cp $fabs $target: $!");
}
my $src = $fdir.$fname."_att";
$target = "$newr/root/$d5mankv{section}/${fname}_att";
mkdir($target);
if(defined($d5mankv{attachments}) and
			not File::Copy::Recursive::dircopy($src, $target)) {
	die("ERROR cpr $src $target: $!");
}

#---------------------------------------------------[ Generate Query Results ]--

my $tags        = join(" ", map { "<tag v=\"$_\"/>" }
					split(/ /, $d5mankv{tags}));
my $attachments = join(" ", map { "<attachment name=\"$_\" modified=\"0\"/>" }
					split(/ /, $d5mankv{attachments}));
my $conf_xml    = join(" ", map {
	if($_ eq "file_root") {
		""; # skip
	} elsif(($_ eq "compl_a") or ($_ eq "compl_b")) {
		"io_$_=\"$conf{$_}\"";
	} elsif($_ eq "db_search") {
		"dbloc_real=\"$conf{$_}\"";
	} else {
		"$_=\"$conf{$_}\"";
	}
} keys(%conf));

my $xml_input = <<"EOF";
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE tables SYSTEM "d5manqueryxml.dtd">
<tables>
	<conf $conf_xml>
		<io_provider_commands/>
		<io_roots><io_root v="$newr/root"/></io_roots>
	</conf>
	<pages>
		<page section="$d5mankv{section}" name="$d5mankv{name}"
				modified="0" lang="$d5mankv{lang}"
				compliance="$d5mankv{compliance}" md5="0">
			<tags>$tags</tags>
			<attachments>$attachments</attachments>
		</page>
	</pages>
</tables>
EOF

#--------------------------------------------------------[ Invoke Conversion ]--

$ENV{D5MAN_CONF} = "$newr/d5man.conf";
my $mode = $argc < 3? "latex": $ARGV[1];
my @basic_cmd = ("java", "-jar", $exportjar, "-m", $mode, "-o", "$newr/out");
if(($argc == 2) or ($mode eq "xslt")) {
	push @basic_cmd, "-c", $ARGV[$#ARGV];
} elsif($argc == 4) {
	push @basic_cmd, "-c", $ARGV[2]
}
print Dumper(@basic_cmd);
IPC::Run3::run3(\@basic_cmd, \$xml_input);
if($? != 0) {
	die("Failed to invoke $exportjar. See error above.\n");
}

#-----------------------------------------------------------[ Process Result ]--

if($argc < 3) {
	IPC::Run3::run3(["make", "-C", "$newr/out"]);
	if($? != 0) {
		die("Failed to invoke pdfLaTeX. See error above.\n");
	}
	File::Copy::copy("$newr/out/main.pdf", "$fdir$fname.pdf");
} elsif($ARGV[1] eq "xslt") {
	$src = "$newr/out/$d5mankv{section}/${fname}.txt";
	$target = "$fdir$fname.txt";
	if(File::Copy::copy($src, $target) == 0) {
		die("cp $src $target: $!");
	}
} else { # xhtml
	my $destdir = $ARGV[2];
	mkdir($destdir) if(not -d "$destdir");
	File::Copy::Recursive::dircopy("$newr/out", $destdir);
}
