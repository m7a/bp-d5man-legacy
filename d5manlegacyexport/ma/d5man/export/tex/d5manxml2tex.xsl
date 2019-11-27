<?xml version="1.0" encoding="UTF-8"?>

<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform">

	<output method="text" indent="no" encoding="UTF-8"/>

	<!-- ............................................. Page and Metadata -->

	<template match="page">
		<apply-templates select="meta/tex"/>

		<text>\begin{dpage}{</text>
		<value-of select="meta/kv[@k='section']/@v"/>
		<text>/</text>
		<call-template name="string-replace-all">
			<with-param name="text"
						select="meta/kv[@k='name']/@v"/>
			<with-param name="replace" select="'/'"/>
			<with-param name="by"      select="'_'"/>
		</call-template>
		<text>_att}</text>

		<call-template name="tpl_optkv"><with-param name="kn"
					select="'section'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'name'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'description'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'compliance'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'lang'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'version'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'copyright'"/></call-template>
		<call-template name="tpl_optkv"><with-param name="kn"
					select="'expires'"/></call-template>

		<apply-templates select="section"/>

		<text>\end{dpage}&#x0a;</text>
	</template>

	<template match="kv">
		<text>{</text><value-of select="@v"/><text>}</text>
	</template>

	<template name="tpl_optkv">
		<param name="kn"/>
		<text>{</text>
		<if test="meta/kv[@k=$kn]">
			<call-template name="tpl_valcharsonly">
				<with-param name="data"
						select="meta/kv[@k=$kn]/@v"/>
			</call-template>
		</if>
		<text>}</text>
	</template>

	<!-- ................................................ Block Elements -->

	<template match="section/section">
		<text>&#x0a;\begin{dsubsection}{</text>
		<call-template name="tpl_valcharsonly">
			<with-param name="data" select="@title"/>
		</call-template>
		<text>}&#x0a;</text>
		<apply-templates/>
		<text>\end{dsubsection}&#x0a;</text>
	</template>

	<template match="section">
		<text>&#x0a;\begin{dsection}{</text>
		<call-template name="tpl_valcharsonly">
			<with-param name="data" select="@title"/>
		</call-template>
		<text>}&#x0a;</text>
		<apply-templates/>
		<text>\end{dsection}&#x0a;</text>
	</template>

	<template match="par"><text>&#x0a;&#x0a;</text></template>

	<template match="codeblock">
		<text>\begin{dcode}&#x0a;</text>
		<value-of select="text()"/>
		<text>\end{dcode}&#x0a;</text>
	</template>

	<!-- ........................................................ Tables -->

	<template match="table">
		<text>\begin{dtable}&#x0a;</text>
			<if test="@title">
				<text>\caption{</text>
				<call-template name="tpl_valcharsonly">
					<with-param name="data"
							select="@title"/>
				</call-template>
				<text>}&#x0a;</text>
			</if>
			<text>\begin{dtabular}</text>
			<apply-templates/>
			<text>\end{dtabular}&#x0a;</text>
		<text>\end{dtable}&#x0a;</text>
	</template>

	<template match="tf">
		<apply-templates/>
		<!-- TODO ONLY WORKS FOR LAST PART... NEED THIS FOR EVERY LINE... -->
		<if test="following-sibling::tf"><text> &amp; </text></if>
	</template>

	<template match="tnl"><text> \\&#x0a;</text></template>
	<template match="tms"><text>\midrule&#x0a;</text></template>

	<!-- ......................................................... Lists -->

	<template match="lg">
		<text>\begin{dlistgroup}&#x0a;</text>
		<apply-templates select="list"/>
		<text>\end{dlistgroup}&#x0a;</text>
	</template>

	<template match="list">
		<text>\begin{dlist</text>
		<value-of select="@type"/>
		<text>}{</text>
		<!-- http://stackoverflow.com/questions/19457502/
				xpath-to-select-only-direct-siblings-with-
				matching-attributes -->
		<apply-templates select="preceding-sibling::*[1][self::lt]"/>
		<text>}&#x0a;</text>
		<apply-templates select="i"/>
		<text>\end{dlist</text>
		<value-of select="@type"/>
		<text>}&#x0a;</text>
	</template>

	<template match="lt"><apply-templates/></template>

	<template match="i">
		<text>\ditem </text>
		<apply-templates/>
		<text>&#x0a;</text>
	</template>

	<!-- ............................................... Inline Elements -->

	<template match="q">
		<choose>
			<when test="@lang='de'">
				<text>"`</text><apply-templates/><text>"'</text>
			</when>
			<when test="@lang='en'">
				<text>``</text><apply-templates/><text>''</text>
			</when>
			<otherwise>
				<message terminate="yes">
					XSL Stylesheet Processing Error:
					Quotation language
					`<value-of select="@lang"/>`
					unknown.
				</message>
			</otherwise>
		</choose>
	</template>

	<template match="exp">
		<text>${</text>
		<value-of select="@base"/>
		<text>}^{</text>
		<value-of select="@exponent"/>
		<text>}$</text>
	</template>

	<template match="em">
		<text>\emph{</text><apply-templates/><text>}</text>
	</template>

	<template match="c">
		<text>\cmd{</text>
		<call-template name="tpl_verbesc">
			<with-param name="data">
				<apply-templates/>
			</with-param>
		</call-template>
		<text>}</text>
	</template>

	<template match="link">
		<variable name="tt">
			<call-template name="tpl_valcharsonly">
				<with-param name="data">
					<value-of select="@title"/>
				</with-param>
			</call-template>
		</variable>
		<choose>
			<when test="@url">
				<variable name="url_san">
					<call-template name="tpl_urlrepl">
						<with-param name="data"
								select="@url"/>
					</call-template>
				</variable>
				<choose>
					<when test="@title and 
							not(@title='url')">
						<text>\href{</text>
						<value-of select="$url_san"/>
						<text>}{</text>
						<value-of select="$tt"/>
						<text>}\footnote{\url{</text>
						<value-of select="$url_san"/>
						<text>}}</text>
					</when>
					<otherwise>
						<text>\url{</text>
						<value-of select="$url_san"/>
						<text>}</text>
					</otherwise>
				</choose>
			</when>
			<otherwise>
				<text>\dlink{</text>
				<value-of select="$tt"/>
				<text>(</text>
				<value-of select="@section"/>
				<text>)}</text>
			</otherwise>
		</choose>
	</template>

	<template name="tpl_urlrepl">
	<param name="data"/>
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
		<call-template name="tpl_escs">
			<with-param name="data" select="$data"/>
			<with-param name="chr" select="'#'"/>
		</call-template></with-param>
	<with-param name="chr" select="'%'"/></call-template></with-param>
	<with-param name="chr" select="'{'"/></call-template></with-param>
	<with-param name="chr" select="'}'"/></call-template>
	</template>

	<template match="sym">
		<choose>
			<when test="@v='half_space'"><text>\,</text></when>
			<when test="@v='space_forced'"><text>~</text></when>
			<when test="@v='rightarrow1'">
				<text>$\rightarrow$</text>
			</when>
			<when test="@v='rightarrow2'">
				<text>$\Rightarrow$</text>
			</when>
			<when test="@v='leftarrow'">
				<text>$\leftarrow$</text>
			</when>
			<when test="@v='dots'"><text>\dots</text></when>
			<!-- requires wasysym -->
			<when test="@v='smiley'"><text>\smiley</text></when>
			<when test="@v='math_in'"><text>$\in$</text></when>
			<when test="@v='dash'"><text>--</text></when>
			<when test="@v='exclamation'">
				<!-- cf. http://tex.stackexchange.com/
					questions/159669/how-to-print-a-warning-
					sign-triangle-with-exclamation-point -->
				<text>{\fontencoding{U}\fontfamily{futs}</text>
				<text>\selectfont\char 66\relax}</text>
			</when>
			<otherwise>
				<message terminate="yes">
					XSL Stylesheet Processing Error:
					Symbol with name
					`<value-of select="@v"/>`
					unknown.
				</message>
			</otherwise>
		</choose>
	</template>

	<template match="shortcut"><apply-templates/></template>

	<template match="key">
		<text>\keystroke{</text>
		<call-template name="tpl_valcharsonly">
			<with-param name="data" select="@x"/>
		</call-template>
		<text>}</text>
	</template>

	<template match="math">
		<text>$</text><value-of select="text()"/><text>$</text>
	</template>

	<template match="tex"><value-of select="text()"/></template>

	<!-- ............................................. Space Elemination -->

	<template match="text()">
		<call-template name="tpl_valcharsonly">
			<with-param name="data">
				<call-template name="tpl_normalize_spc_m01">
					<with-param name="data">
						<call-template
							name="tpl_normalize_nl">
							<with-param name="data"
								select="."/>
						</call-template>
					</with-param>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc_m01">
		<param name="data"/>
		<call-template name="tpl_normalize_spc">
			<with-param name="data">
				<call-template name="tpl_normalize_spc_m02">
					<with-param name="data" select="$data"/>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc_m02">
		<param name="data"/>
		<call-template name="tpl_normalize_spc">
			<with-param name="data">
				<call-template name="tpl_normalize_spc_m03">
					<with-param name="data" select="$data"/>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc_m03">
		<param name="data"/>
		<call-template name="tpl_normalize_spc">
			<with-param name="data">
				<call-template name="tpl_normalize_spc_m04">
					<with-param name="data" select="$data"/>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc_m04">
		<param name="data"/>
		<call-template name="tpl_normalize_spc">
			<with-param name="data">
				<call-template name="tpl_normalize_spc_m05">
					<with-param name="data" select="$data"/>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc_m05">
		<param name="data"/>
		<call-template name="tpl_normalize_spc">
			<with-param name="data">
				<call-template name="tpl_normalize_spc_m06">
					<with-param name="data" select="$data"/>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc_m06">
		<param name="data"/>
		<call-template name="tpl_normalize_spc">
			<with-param name="data">
				<call-template name="tpl_normalize_spc">
					<with-param name="data" select="$data"/>
				</call-template>
			</with-param>
		</call-template>
	</template>

	<template name="tpl_normalize_spc">
		<param name="data"/>
		<call-template name="string-replace-all">
			<with-param name="text"    select="$data"/>
			<with-param name="replace" select="'  '"/>
			<with-param name="by"      select="' '"/>
		</call-template>
	</template>

	<template name="tpl_normalize_nl">
		<param name="data"/>
		<if test="normalize-space($data)">
			<value-of select="translate(translate($data, '&#x0a;',
							' '), '&#9;', ' ')"/>
		</if>
	</template>

	<!-- ..................................... General Utility Templates -->

	<template name="tpl_valcharsonly">
	<!-- indentation rules changed to make this possible... -->
	<param name="data"/>
	<call-template name="tpl_singular2"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
		<call-template name="string-replace-all">
			<with-param name="text" select="$data"/>
			<with-param name="replace" select="'\'"/>
			<with-param name="by" select="'\textbackslash '"/>
		</call-template></with-param>
	<with-param name="chr" select="'&amp;'"/></call-template></with-param>
	<with-param name="chr" select="'%'"/></call-template></with-param>
	<with-param name="chr" select="'$'"/></call-template></with-param>
	<with-param name="chr" select="'#'"/></call-template></with-param>
	<with-param name="chr" select="'_'"/></call-template></with-param>
	<with-param name="chr" select="'{'"/></call-template></with-param>
	<with-param name="chr" select="'}'"/></call-template></with-param>
	<with-param name="chr" select="'^'"/></call-template>
	</template>

	<template name="tpl_verbesc">
	<param name="data"/>
	<call-template name="tpl_singular2"><with-param name="data">
	<call-template name="tpl_singular2"><with-param name="data">
	<call-template name="tpl_singular2"><with-param name="data">
	<call-template name="tpl_singular2"><with-param name="data">
	<call-template name="tpl_singular"><with-param name="data">
		<call-template name="string-replace-all">
			<!-- TODO DOES THIS WORK AS EXPECTED? IT SEEMS IT DOES NOT... (ALSO CONFLICTS W/ OUTWARD TILDE REPLACING... ASTAT) -->
			<with-param name="text"    select="$data"/>
			<with-param name="replace" select="'  '"/>
			<with-param name="by"      select="'~~'"/>
		</call-template>
	</with-param><with-param name="chr" select="'-'"/></call-template>
	</with-param><with-param name="chr" select="'~'"/></call-template>
	<!-- TODO DISSATISFYING: WE GET TWO DOTS ABOVE SPACE INSTEAD OF " -->
	</with-param><with-param name="chr" select="'&quot;'"/></call-template>
	</with-param><with-param name="chr" select="'`'"/></call-template>
	</with-param><with-param name="chr">&apos;</with-param></call-template>
	</template>

	<template name="tpl_singular">
		<param name="data"/><param name="chr"/>
		<call-template name="string-replace-all">
			<with-param name="text"    select="$data"/>
			<with-param name="replace" select="$chr"/>
			<with-param name="by"      select="concat(concat('{',
								$chr), '}')"/>
		</call-template>
	</template>

	<template name="tpl_singular2">
		<param name="data"/><param name="chr"/>
		<call-template name="string-replace-all">
			<with-param name="text"    select="$data"/>
			<with-param name="replace" select="$chr"/>
			<with-param name="by"      select="concat(concat('\',
								$chr), '{}')"/>
		</call-template>
	</template>
	<!--
	<template name="tpl_codeesc">
	<param name="data"/>
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
	<call-template name="tpl_escs"><with-param name="data">
		<call-template name="tpl_escs">
			<with-param name="data" select="$data"/>
			<with-param name="chr" select="'\'"/>
		</call-template></with-param>
	<with-param name="chr" select="'{'"/></call-template></with-param>
	<with-param name="chr" select="'}'"/></call-template></with-param>
	<with-param name="chr" select="'~'"/></call-template></with-param>
	<with-param name="chr" select="'%'"/></call-template>
	</template>
	-->

	<template name="tpl_escs">
		<param name="data"/><param name="chr"/>
		<call-template name="string-replace-all">
			<with-param name="text"    select="$data"/>
			<with-param name="replace" select="$chr"/>
			<with-param name="by"      select="concat('\', $chr)"/>
		</call-template>
	</template>

	<!-- String replacing function from http://geekswithblogs.net/Erik/
					archive/2008/04/01/120915.aspx -->
	<template name="string-replace-all">
		<param name="text"/><param name="replace"/><param name="by"/>
		<choose>
			<when test="contains($text, $replace)">
				<value-of select="substring-before($text,
								$replace)"/>
				<value-of select="$by"/>
				<call-template name="string-replace-all">
					<with-param name="text"
						select="substring-after($text,
								$replace)"/>
					<with-param name="replace"
							select="$replace"/>
					<with-param name="by" select="$by"/>
				</call-template>
			</when>
			<otherwise><value-of select="$text"/></otherwise>
		</choose>
	</template>

</stylesheet>
