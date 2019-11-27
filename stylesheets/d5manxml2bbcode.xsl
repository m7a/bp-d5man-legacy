<?xml version="1.0" encoding="UTF-8"?>

<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform">

	<output method="text" indent="no" encoding="UTF-8"/>

	<!-- .................................................. Space Reflow -->

	<strip-space elements="*" />

	<!-- see http://stackoverflow.com/questions/324822 -->
	<!-- see http://www.dpawson.co.uk/xsl/sect2/normalise.html -->
	<template match="node/@TEXT|text()">
		<!-- alt. "concat('&#x20;',normalize-space(.),'&#x20;')" -->
		<if test="normalize-space(.)">
			<value-of select="translate(translate(., '&#x0a;', ' '),
								'&#9;', ' ')"/>
		</if>
		<apply-templates/>
	</template>

	<!-- .......................................................... Meta -->

	<template match="page">
		<text>&#x0a;-------------------------------------------</text>
		<text>-------------------------------------&#x0a;&#x0a;</text>
		<text>[color=#ffffff]</text>
		<apply-templates/>
		<text>[/color]</text>
	</template>
	<template match="meta">
		<apply-templates/>
		<text>&#x0a;</text>
	</template>
	<template match="kv">
		<choose>
			<when test="@k='description'">
				<text>[size=6][b]</text>
				<value-of select="@v"/>
				<text>[/b][/size]&#x0a;&#x0a;</text>
			</when>
			<when test="@k='version'">
				<text>[color=#aaaaaa][size=2]Version </text>
				<value-of select="@v"/>
				<text>[/size][/color]&#x0a;</text>
			</when>
			<when test="@k='copyright'">
				<text>[color=#aaaaaa][size=2]</text>
				<value-of select="@v"/>
				<text>[/size][/color]&#x0a;</text>
			</when>
		</choose>
	</template>

	<!-- ....................................................... Special -->
	<template match="section">
		<text>[color=#ff6060][size=4][b]</text>
		<value-of select="@title"/>
		<text>[/b][/size][/color]</text>
		<apply-templates/>
	</template>

	<template match="table">
		<text>[color=#aaaaaa][size=2](Table missing)</text>
		<text>[/size][/color]&#x0a;</text>
	</template>

	<template match="codeblock">
		<text>[font=Courier New][list]</text>
		<apply-templates/>
		<text>[/list][/font]&#x0a;</text>
	</template>

	<template match="par">
		<text>&#x0a;&#x0a;</text>
	</template>

	<template match="q">
		<choose>
			<when test="@lang='de'"><text>„</text></when>
			<otherwise><text>“</text></otherwise>
		</choose>
		<apply-templates/>
		<text>”</text>
	</template>

	<template match="exp">
		<value-of select="@base"/>
		<text>^</text>
		<value-of select="@exponent"/>
	</template>

	<template match="em">
		<text>[i]</text>
		<apply-templates/>
		<text>[/i]</text>
	</template>

	<template match="c">
		<text>[font=Courier New]</text>
		<apply-templates/>
		<text>[/font]</text>
	</template>

	<template match="link">
		<choose>
			<when test="@url!=''">
				<choose>
					<when test="@title=''">
						<text>[url]</text>
						<value-of select="@url"/>
					</when>
					<otherwise>
						<text>[url=</text>
						<value-of select="@url"/>
						<text>]</text>
						<value-of select="@title"/>
					</otherwise>
				</choose>
				<text>[/url]</text>
			</when>
			<otherwise>
				<!-- TODO z LINK TO MAWEBSITE FOR THIS -->
				<text>[color=#00ffff]</text>
				<value-of select="@title"/>
				<text>(</text>
				<value-of select="@section"/>
				<text>)[/color]</text>
			</otherwise>
		</choose>
	</template>

	<template match="sym">
		<value-of select="@raw"/>
	</template>

	<template match="shortcut|math">
		<apply-templates/>
	</template>

	<template match="key">
		<text>\[</text>
		<value-of select="@x"/>
		<text>\]</text>
	</template>

	<template match="section/tex|section/raw_xml">
		<text>[color=#aaaaaa]Unsupported TeX: </text>
		<apply-templates/>
		<text>[/color]</text>
	</template>

	<!-- .......................................................... List -->
	<template match="lg">
		<apply-templates/>
	</template>
	<template match="lt">
		<text>[color=#ffcd00]</text>
		<apply-templates/>
		<text>[/color]</text>
	</template>

	<!-- TODO z PROBLEM: NESTED LISTS ARE UNSUPPORTED -->
	<template match="list">
		<choose>
			<when test="@type='num'">[list=1]</when>
			<otherwise>[list]</otherwise>
		</choose>
		<apply-templates select="i"/>
		<text>[/list]</text>
	</template>
	<template match="list[@type='desc']/i">
		<apply-templates/>
	</template>
	<template match="list[@type!='desc']/i">
		<text>[*]</text>
		<apply-templates/>
		<text>&#x0a;</text>
	</template>

</stylesheet>
