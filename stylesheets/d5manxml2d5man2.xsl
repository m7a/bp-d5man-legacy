<?xml version="1.0" encoding="UTF-8"?>
<!--
	Ma_Sys.ma Stylesheet to convert from legacy D5Man (XML) to the new
	Markdown-based variant. Based on the BB code stylehsheet.
-->
<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform">

	<output method="text" indent="no" encoding="UTF-8"/>

	<!-- .................................................. Space Reflow -->

	<strip-space elements="*" />

	<!-- see http://stackoverflow.com/questions/324822 -->
	<!-- see http://www.dpawson.co.uk/xsl/sect2/normalise.html -->
	<!--
	<template match="node/@TEXT|text()">
		<if test="normalize-space(.)">
			<value-of select="translate(translate(., '&#x0a;', ' '),
								'&#9;', ' ')"/>
		</if>
		<apply-templates/>
	</template>
	-->

	<!-- .......................................................... Meta -->

	<template match="page">
		<apply-templates/>
	</template>

	<template match="meta">
		<text>---&#x0a;</text>
		<apply-templates/>
		<text>x-masysma-repository: https://www.github.com/m7a/</text>
		<text>...&#x0a;x-masysma-owned: 1&#x0a;</text>
		<text>---&#x0a;</text>
	</template>
	<template match="kv">
		<choose>
<!-- begin wrongly indented -->
<when test="@k='description'"><text>title: </text>
			<value-of select="@v"/><text>&#x0a;</text></when>
<when test="@k='name'"><text>x-masysma-name: </text>
			<value-of select="@v"/><text>&#x0a;</text></when>
<when test="@k='section'"><text>section: </text>
			<value-of select="@v"/><text>&#x0a;</text></when>
<when test="@k='copyright'"><text>x-masysma-copyright: </text>
			<value-of select="@v"/><text>&#x0a;</text></when>
<when test="@k='version'"><text>version: </text>
			<value-of select="@v"/><text>&#x0a;</text></when>
<when test="@k='tags'"><text>keywords: [&quot;</text>
			<value-of select="@v"/><text>&quot;]&#x0a;</text></when>
<when test="@k='creation'"><text>date: </text>
			<value-of select="@v"/><text>&#x0a;</text></when>
<when test="@k='lang'"><text>lang: </text>
			<value-of select="@v"/><text>-</text>
			<choose><when test="@v='de'"><text>DE</text></when>
				<when test="@v='en'"><text>US</text></when>
				</choose>
			<text>&#x0a;</text></when>
<!-- end wrongly indented -->
		</choose>
	</template>

	<!-- ....................................................... Special -->
	<template match="section">
		<value-of select="@title"/><text>&#x0a;</text>
		<value-of select="translate(@title,
			'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz',
			'====================================================')"
			/>
		<text>&#x0a;&#x0a;</text>
		<apply-templates/>
	</template>

	<template match="table">
		<text>_!!! MISSING: TABLE_&#x0a;</text>
	</template>

	<template match="codeblock">
		<text>~~~&#x0a;</text><apply-templates/><text>~~~&#x0a;</text>
	</template>

	<template match="par"><text>&#x0a;&#x0a;</text></template>

	<template match="q">
		<choose><when test="@lang='de'"><text>„</text></when>
			<otherwise><text>“</text></otherwise></choose>
		<apply-templates/>
		<text>”</text>
	</template>

	<template match="exp">
		<value-of select="@base"/>
		<text>^</text><value-of select="@exponent"/><text>^</text>
	</template>

	<template match="em">
		<text>_</text><apply-templates/><text>_</text>
	</template>

	<template match="c">
		<text>`</text><apply-templates/><text>`</text>
	</template>

	<template match="link">
		<choose>
			<when test="@url!=''">
				<choose>
					<when test="@title='url'">
						<text>&lt;</text>
						<value-of select="@url"/>
						<text>&gt;</text>
					</when>
					<otherwise>
						<text>[</text>
						<value-of select="@title"/>
						<text>](</text>
						<value-of select="@url"/>
						<text>)</text>
					</otherwise>
				</choose>
			</when>
			<otherwise>
				<text>[</text>
				<value-of select="@title"/>
				<text>(</text>
				<value-of select="@section"/>
				<text>)](../</text>
				<value-of select="@section"/>
				<text>/</text>
				<value-of select="translate(@title, '/', '_')"/>
				<text>.xhtml)</text>
			</otherwise>
		</choose>
	</template>

	<template match="sym"><value-of select="@raw"/></template>
	<template match="shortcut|math"><apply-templates/></template>

	<template match="key">
		<text>[</text><value-of select="@x"/><text>]</text>
	</template>

	<template match="section/tex|section/raw_xml">
		<text>_!!! BEGIN UNSUPPORTED TEX_ </text>
		<apply-templates/>
		<text>_!!! END UNSUPPORTED TEX_ </text>
	</template>

	<!-- .......................................................... List -->
	<template match="lg"><apply-templates/></template>
	<template match="lt">
		<text>### </text>
		<apply-templates/>
		<text>&#x0a;</text>
	</template>

	<!-- Nested lists are unsupported... -->
	<template match="list"><apply-templates select="i"/></template>
	<template match="list[@type='desc']/i"><apply-templates/></template>
	<template match="list[@type!='desc']/i">
		<text> * </text>
		<apply-templates/>
		<text>&#x0a;</text>
	</template>

</stylesheet>
