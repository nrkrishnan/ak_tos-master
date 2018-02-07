<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:argo="http://www.navis.com/argo" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:output method="text" version="1.0" indent="yes"/>

<xsl:template match="/">

<xsl:text>
    
</xsl:text>

<xsl:call-template name="argo:docBody"/>
 
</xsl:template>

<xsl:template match="argo:docDescription">
</xsl:template>

<xsl:template name="argo:docBody">
        <xsl:apply-templates/>
    </xsl:template>

<xsl:template match="argo:truckVisit">

</xsl:template>

<xsl:template match="argo:trkTransaction">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text> 
 
	 <!-- ***** Container Number *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o550,125;f0;c25;w24;h24;d3,</xsl:text> 
     <xsl:value-of select="tranCtrNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
     <!-- ***** Do Not Backload text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,200;f0;c25;w70;h70;d3,DO NOT BACKLOAD &lt;ETX&gt;</xsl:text> 
 
	 <!-- ***** Shipper *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o25,400;f0;c25;w12;h12;d3,Shipper :&lt;ETX&gt;</xsl:text> 

	 <!-- ***** Shipper text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o225,400;f0;c25;w12;h12;d3,</xsl:text>
		<xsl:value-of select="tranShipperName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 

	 <!-- ***** Matson customer service text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o800,400;f0;c25;w12;h12;d3,Matson Customer Service :&lt;ETX&gt;</xsl:text> 

	 <!-- ***** Matson contact number text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H5;o800,440;f0;c25;w12;h12;d3,(907) 263-5002 &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Consignee *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H6;o25,440;f0;c25;w12;h12;d3,Consignee :&lt;ETX&gt;</xsl:text> 

	 <!-- ***** Consignee text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H7;o225,440;f0;c25;w12;h12;d3,</xsl:text>
		<xsl:value-of select="tranConsigneeName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Trucking Co. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H8;o25,480;f0;c25;w12;h12;d3,Trucking Co.:&lt;ETX&gt;</xsl:text> 

	 <!-- ***** Trucking Co. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o225,480;f0;c25;w12;h12;d3,</xsl:text>
		<xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** End of Transfer Block *****  -->
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text> 

</xsl:template>

</xsl:stylesheet>