<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text" indent="no" omit-xml-declaration="yes" encoding="utf-8"/>
    <xsl:template name="xsl:initial-template" match="/Fragment/Model">
        <xsl:text>&#10;@startuml&#10;set namespaceSeparator none&#10;skinparam linetype ortho&#10;&#10;</xsl:text>
        <xsl:for-each select="Node">
            <xsl:if test="@is_abstract = 'true'"><xsl:text>abstract </xsl:text></xsl:if><xsl:text>class </xsl:text><xsl:value-of select="replace(@uri, 'modicio:', '')" /><xsl:text> { &#10;</xsl:text>
            <xsl:text>   variantId= </xsl:text><xsl:value-of select="Annotation/@variant_id"/><xsl:text>&#10;</xsl:text>
            <xsl:text>   variantTime= </xsl:text><xsl:value-of select="Annotation/@variant_time"/><xsl:text>&#10;</xsl:text>
            <xsl:text>   versionId= </xsl:text><xsl:value-of select="Annotation/@version_id"/><xsl:text>&#10;</xsl:text>
            <xsl:text>   versionTime= </xsl:text><xsl:value-of select="Annotation/@version_time"/><xsl:text>&#10;</xsl:text>
            <xsl:text>   ..&#10;</xsl:text>
            <xsl:for-each select="Attribute">
                <xsl:text>   </xsl:text><xsl:value-of select="replace(@name, 'modicio:', '')" /><xsl:text> : </xsl:text><xsl:value-of select="@dType"/><xsl:text>&#10;</xsl:text>
            </xsl:for-each>
            <xsl:text>} &#10;</xsl:text>
            <xsl:for-each select="AssociationRelation">
                <xsl:value-of select="replace(../@uri, 'modicio:', '')" /><xsl:text> --> </xsl:text><xsl:value-of select="replace(@target, 'modicio:', '')" /><xsl:text> : </xsl:text><xsl:value-of select="@name"/><xsl:text>&#10;</xsl:text>
                <xsl:text>(</xsl:text><xsl:value-of select="replace(../@uri, 'modicio:', '')" /><xsl:text>, </xsl:text><xsl:value-of select="replace(@target, 'modicio:', '')"/><xsl:text>) .. I</xsl:text><xsl:value-of select="replace(../@name, ' ', '')"/><xsl:value-of select="replace(../@name, ' ', '')"/><xsl:text>&#10;</xsl:text>
                <xsl:text>Interface I</xsl:text><xsl:value-of select="replace(../@name, ' ', '')"/><xsl:value-of select="replace(../@name, ' ', '')"/><xsl:text>{}</xsl:text>
            </xsl:for-each>
            <xsl:for-each select="ParentRelation">
                <xsl:value-of select="replace(../@uri, 'modicio:', '')" /><xsl:text> --|> </xsl:text><xsl:value-of select="replace(@uri, 'modicio:', '')" /><xsl:text>&#10;</xsl:text>
            </xsl:for-each>
            <xsl:for-each select="Composition">
                <xsl:value-of select="replace(../@uri, 'modicio:', '')" /><xsl:text> *-- </xsl:text><xsl:value-of select="replace(@target, 'modicio:', '')"/><xsl:text> : </xsl:text><xsl:value-of select="@role"/><xsl:text>&#10;</xsl:text>
            </xsl:for-each>
            <xsl:text>&#10;</xsl:text>
        </xsl:for-each>
        <xsl:text>@enduml&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>