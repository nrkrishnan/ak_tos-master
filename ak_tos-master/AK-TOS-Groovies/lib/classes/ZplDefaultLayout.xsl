<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:argo="http://www.navis.com/argo" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:output method="text" version="1.0" indent="yes"/>

<xsl:template match="/">
<xsl:text>^XA
^MMT
^LL1015
^PW448
^LS0
^FT38,79^A0N,39,38^FH\^FDN4 Gate Pass^FS
^FO22,34^GB270,59,4^FS</xsl:text>
        <xsl:call-template name="argo:docBody"/>
<xsl:text>^XZ</xsl:text>
</xsl:template>

<xsl:template match="argo:docDescription">
</xsl:template>

<xsl:template name="argo:docBody">
        <xsl:apply-templates/>
    </xsl:template>

<xsl:template match="argo:truckVisit">
^FT20,130^A0N,25,26^FH\^FDBat Number: <xsl:value-of select="tvdtlsBatNbr"/>^FS
^FT20,160^A0N,25,26^FH\^FDTruck Lic: <xsl:value-of select="tvdtlsLicNbr"/>^FS
^FT20,190^A0N,25,26^FH\^FDContainer: <xsl:value-of select="../argo:trkTransaction/tranCtrNbr"/>^FS
^FT20,220^A0N,25,26^FH\^FDChassis: <xsl:value-of select="../argo:trkTransaction/tranChsNbr"/>^FS
^FT20,250^A0N,25,26^FH\^FDTime: <xsl:value-of select="../argo:trkTransaction/tranCreated"/>^FS
^FT20,280^A0N,25,26^FH\^FDUser: <xsl:value-of select="../argo:trkTransaction/tranCreator"/>^FS
^FT20,350^A0N,25,26^FH\^FDNote: <xsl:value-of select="../argo:trkTransaction/tranNotes"/>^FS
</xsl:template>

<xsl:template match="argo:trkTransaction">
</xsl:template>


</xsl:stylesheet>
