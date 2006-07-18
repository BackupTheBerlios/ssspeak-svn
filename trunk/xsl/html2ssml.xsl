<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
                version="1.0">

  <xsl:param name="marklinks">1</xsl:param>

  <xsl:param name="speed">medium</xsl:param>

  <xsl:param name="volume">medium</xsl:param>

  <xsl:param name="sounddir">SET_ME</xsl:param>

  <xsl:param name="voice">male1</xsl:param>

  <xsl:output method="xml" indent="yes" encoding="utf-8" omit-xml-declaration="no"/>

  <xsl:template match="/">
    <speak>
      <prosody rate="{$speed}" volume="{$volume}">
        <metadata>
          <mark-index>
            <xsl:apply-templates select="a[@href]|xhtml:a[@href]" mode="link-index"/>
          </mark-index>
        </metadata>
        <voice name="{$voice}">
          <xsl:apply-templates select="*"/>
        </voice>
      </prosody>
    </speak>
  </xsl:template>

  <xsl:template name="embedAudio">
    <xsl:param name="sound"/>

    <xsl:variable name="wavfile">
      <xsl:choose>
        <xsl:when test="$sound = 'title'">html-title.wav</xsl:when>
        <xsl:when test="$sound = 'header'">header.wav</xsl:when>
        <xsl:when test="$sound = 'item'">click.wav</xsl:when>
        <xsl:when test="$sound = 'link'">link.wav</xsl:when>
      </xsl:choose>
    </xsl:variable>

    <audio src="{concat('file:', $sounddir, '/', $wavfile)}"/>
  </xsl:template>

  <xsl:template match="head|xhtml:head">
    <xsl:apply-templates select="title[normalize-space() != '']|xhtml:title[normalize-space() != '']"/>
  </xsl:template>

  <xsl:template match="body|xhtml:body">
    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="a[@href]|xhtml:a[@href]" mode="link-index">
    <mark-info type="link" content="{@href}"/>
  </xsl:template>

  <xsl:template match="a[@href or @id or @name]|xhtml:a[@href or @id or @name]">

    <xsl:if test="$marklinks = '1'">
      <xsl:choose>
        <xsl:when test="@id"><mark name="{@id}"/></xsl:when>
        <xsl:otherwise><mark name="{@href}"/></xsl:otherwise>
      </xsl:choose>

    </xsl:if>

    
    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="title|xhtml:title">
    <xsl:call-template name="embedAudio">
      <xsl:with-param name="sound">title</xsl:with-param>
    </xsl:call-template>

    <emphasis level="strong">
      <xsl:apply-templates select="*|text()"/>
    </emphasis>.<break/>
  </xsl:template>

  <xsl:template match="h1|h2|h3|h4|xhtml:h1|xhtml:h2|xhtml:h3|xhtml:h4">

    <xsl:call-template name="embedAudio">
      <xsl:with-param name="sound">header</xsl:with-param>
    </xsl:call-template>

    <emphasis level="strong">
      <xsl:apply-templates select="*|text()"/>
    </emphasis>.<break/>
  </xsl:template>

  <xsl:template match="tr|xhtml:tr">
    <xsl:call-template name="embedAudio">
      <xsl:with-param name="sound">item</xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="*[local-name() = 'li' or local-name() = 'LI']">
    <xsl:if test="not((node()[1][normalize-space() = ''] and node()[2][local-name() = 'a']/@href) or node()[1][local-name() = 'a']/@href)">
      <xsl:call-template name="embedAudio">
        <xsl:with-param name="sound">item</xsl:with-param>
      </xsl:call-template>
    </xsl:if>

    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="b|i|em|xhtml:b|xhtml:i|xhtml:em">
    <emphasis><xsl:apply-templates select="*|text()"/></emphasis>
  </xsl:template>

  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="p|xhtml:p">
    <p>
      <xsl:apply-templates select="*|text()"/>
    </p>
  </xsl:template>

  <xsl:template match="br|hr|xhtml:br|xhtml:hr">
    <break/>
  </xsl:template>

  <xsl:template match="*">
    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="script">
  </xsl:template>


</xsl:stylesheet>
