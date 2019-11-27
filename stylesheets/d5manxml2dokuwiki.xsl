<?xml version="1.0" encoding="UTF-8"?>

<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform">

	<output method="text" indent="no" encoding="UTF-8"/>

	<!-- .................................................. Space Reflow -->

	<strip-space elements="*"/>

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
		<apply-templates/>
	</template>
	<template match="meta">
		<apply-templates/>
		<text>&#x0a;</text>
	</template>
	<template match="kv">
		<choose>
			<when test="@k='description'">
				<text>====== </text>
				<value-of select="@v"/>
				<text> ======&#x0a;&#x0a;</text>
			</when>
			<when test="@k='version'">
				<!-- TODO Kleinschrift -->
				<text>Version: </text>
				<value-of select="@v"/>
				<text>&#x0a;</text>
			</when>
		</choose>
	</template>

	<!-- ....................................................... Special -->
	<template match="page/section">
		<text>===== </text>
		<value-of select="@title"/>
		<text> =====&#x0a;</text>
		<apply-templates/>
	</template>

	<template match="section/section">
		<text>==== </text>
		<value-of select="@title"/>
		<text> ====&#x0a;</text>
		<apply-templates/>
	</template>

	<template match="codeblock">
		<text>&lt;code&gt;</text>
		<apply-templates/>
		<text>&lt;/code&gt;</text>
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
		<text>&lt;sup&gt;</text>
		<value-of select="@exponent"/>
		<text>&lt;/sup&gt;</text>
	</template>

	<template match="em">
		<text>//</text>
		<apply-templates/>
		<text>//</text>
	</template>

	<template match="c">
		<text>''</text>
		<apply-templates/>
		<text>''</text>
	</template>

	<template match="link">
		<choose>
			<when test="@url!=''">
				<choose>
					<when test="@title='' or @title='url'">
						<value-of select="@url"/>
					</when>
					<otherwise>
						<text>[[</text>
						<value-of select="@url"/>
						<text>|</text>
						<value-of select="@title"/>
						<text>]]</text>
					</otherwise>
				</choose>
			</when>
			<otherwise>
				<!-- TODO z LINK TO MAWEBSITE FOR THIS -->
				<value-of select="@title"/>
				<text>(</text>
				<value-of select="@section"/>
				<text>)</text>
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
		<text>[</text>
		<value-of select="@x"/>
		<text>]</text>
	</template>

	<!-- ignore -->
	<template match="meta/tex"></template>

	<!-- pass raw xml unhindered -->
	<template match="raw_xml"><apply-templates/></template>

	<template match="section/tex">
		<variable name="strcnt">
			<apply-templates/>
		</variable>
		<variable name="c1"><value-of select="substring-after($strcnt,
							'\img{')"/></variable>
		<text>{{ </text>
		<value-of select="ancestor::page/meta/kv[@k='x_dokuwiki_pre'
									]/@v"/>
		<value-of select="substring-before($c1, '}{')"/>
		<value-of select="ancestor::page/meta/kv[@k='x_dokuwiki_post'
									]/@v"/>
		<text> |}}&#x0a;</text>
		<value-of select="substring-before(substring-after($c1, '}{'),
									'}')"/>
	</template>

	<!-- ........................................................ Tables -->
	<template match="table">
		<text>**</text>
		<value-of select="@title"/>
		<text>**&#x0a;</text>
		<choose>
			<when test="tms">
				<apply-templates
				select="tms/preceding-sibling::*" mode="th"/>
				<apply-templates
				select="tms/following-sibling::*" mode="td"/>
			</when>
			<otherwise>
				<apply-templates mode="td"/>
			</otherwise>
		</choose>
	</template>

	<template match="tnl" mode="td"><text> |&#x0a;</text></template>

	<template match="tf" mode="td">
		<text> | </text>
		<apply-templates/>
	</template>

	<template match="tnl" mode="th"><text> ^&#x0a;</text></template> 

	<template match="tf" mode="th">
		<text> ^ </text>
		<apply-templates/>
	</template>


	<!-- .......................................................... List -->
	<template match="lg">
		<apply-templates/>
	</template>
	<template match="lt">
		<text>**</text>
		<apply-templates/>
		<text>**\\&#x0a;</text>
	</template>

	<template match="list"><apply-templates/></template>
	<template match="list[@type='desc']/i"><apply-templates/></template>

	<template match="list[@type='ul' or @type='pro' or @type='con']/i">
		<text>  * </text>
		<apply-templates/>
		<text>&#x0a;</text>
	</template>

	<template match="list[@type='num']/i">
		<text>  - </text>
		<apply-templates/>
		<text>&#x0a;</text>
	</template>

</stylesheet>
