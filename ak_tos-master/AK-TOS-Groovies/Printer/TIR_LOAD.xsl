<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:argo="http://www.navis.com/argo" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:saxon="http://icl.com/saxon" extension-element-prefixes="saxon">

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
 
	<!-- ***** stop no. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** Transaction Number *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
     <xsl:value-of select="tranNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
     <!-- ***** TERMINAL ID *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
      <!-- ***** Truck Visit End Time *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

     <!-- ***** Trucking Company *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
    <xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
    <xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
    <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
    <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 1">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 2">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 3">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
	<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &lt;= 3">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>
	       <!-- ***** Page 2 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;3">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 2 *****  -->
	
	<!-- ***** stop no. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 4">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 5">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 6">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 3 and position() &lt;= 6">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 3 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;6">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 3 *****  -->
	
	<!-- ***** stop no. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 7">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 8">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 9">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 6 and position() &lt;= 9">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 4 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;9">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 4 *****  -->
	 	
	<!-- ***** stop no. *****  -->
	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1375;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 10">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 11">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 12">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 9 and position() &lt;= 12">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 5 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;12">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 5 *****  -->
	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 13">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 14">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 15">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 12 and position() &lt;= 15">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>				
	       <!-- ***** Page 6 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;15">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 6 *****  -->
	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1375;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 16">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 17">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 18">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 15 and position() &lt;= 18">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>			
	       <!-- ***** Page 7 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;18">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 7 *****  -->
		
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 19">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 20">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 21">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 18 and position() &lt;= 21">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 8 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;21">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 8 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 22">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 23">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 24">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 21 and position() &lt;= 24">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 9 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;24">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 9 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 25">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 26">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 27">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 24 and position() &lt;= 27">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 10 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;27">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 10 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1375;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 28">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 29">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 30">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 27 and position() &lt;= 30">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 11 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;31">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 11 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 31">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 32">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 33">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 30 and position() &lt;= 33">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 12 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;33">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 12 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 34">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 35">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 36">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 33 and position() &lt;= 36">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 13 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;36">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 13 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 37">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 38">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 39">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 36 and position() &lt;= 39">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>
	       <!-- ***** Page 14 *****  -->
	<xsl:choose>
		<xsl:when test="count(argo:tranHazard) &gt;39">

     <xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text>
	<!-- ***** Page 14 *****  -->
	 	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H126;o25,25;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Transaction Number *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o25,100;f0;c25;w12;h12;d3,NO:</xsl:text> 
	 <xsl:value-of select="tranNbr"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
	 <!-- ***** TERMINAL ID *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,TIR Load Information&lt;ETX&gt;</xsl:text> 
 
	  <!-- ***** Truck Visit End Time *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o950,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

	 <!-- ***** Trucking Company *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o950,100;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** Box *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o25,130;f0;l1350;h475;w4;</xsl:text> 
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>   
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L6;o25,160;f0;l1350;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L7;o25,190;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L8;o25,220;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L9;o25,250;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L10;o25,550;f0;l1350;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L11;o65,220;f3;l330;w4;</xsl:text> 	 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L12;o570,130;f3;l90;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L13;o165,220;f3;l330;w4;</xsl:text> 
 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L14;o770,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L15;o850,220;f3;l330;w4;</xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L16;o930,220;f3;l330;w4;</xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L17;o1025,220;f3;l330;w4;</xsl:text> 

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L18;o1090,220;f3;l330;w4;</xsl:text>

	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L19;o1230,220;f3;l330;w4;</xsl:text>

 
	<!-- ***** Consignee *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o29,128;f0;c25;w10;h10;d3,To: (Consignee)</xsl:text> 
	<xsl:value-of select="tranConsigneeName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Shipper *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o626,128;f0;c25;w10;h10;d3,From: (Shipper)</xsl:text> 
	<xsl:value-of select="tranShipperName"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Operator *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o29,158;f0;c25;w12;h12;d3,Operator:</xsl:text> 
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Container *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o626,158;f0;c25;w12;h12;d3,Container:</xsl:text> 
	 <xsl:value-of select="tranCtrNbr"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	

	<!-- ***** 24 hr Emergency number *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o29,188;f0;c25;w10;h10;d3,24 Hour Emergency Contact Telephone Number:</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** 24 hr Emergency number text *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o626,188;f0;c25;w10;h10;d3,</xsl:text> 
	<xsl:value-of select="tranFlexString01"/>
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>				

	<!-- ***** HM *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o29,218;f0;c25;w12;h12;d3,HM</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** UN # *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o67,218;f0;c25;w12;h12;d3,UN/NA#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	
	<!-- ***** Description *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o340,218;f0;c25;w12;h12;d3,Description</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** Class *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o772,218;f0;c25;w12;h12;d3,Class</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** P Grp *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o852,218;f0;c25;w12;h12;d3,P Grp</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	<!-- ***** FP *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o932,218;f0;c25;w12;h12;d3,FP</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	
	<!-- ***** ERG# *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o1027,218;f0;c25;w12;h12;d3,ERG#</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- ***** Qty/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o1092,218;f0;c25;w12;h12;d3,Qty/Unit</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	
	<!-- ***** Total/Unit *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1232,218;f0;c25;w12;h12;d3,Total(LB)</xsl:text> 
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
	<!-- Hazard Text -->
	<xsl:for-each select="argo:tranHazard">
		<xsl:if test="position() = 40">
				<!-- ***** Page no. *****  -->
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:value-of select="hzrdiPageNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o29,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o67,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o167,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o772,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o852,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o932,250;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o1027,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H42;o1092,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o1232,250;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o167,265;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o167,280;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>		
		<xsl:if test="position() = 41">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o29,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o67,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o167,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o772,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o852,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o932,310;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1027,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H53;o1092,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o1232,310;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o167,325;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o167,340;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
		<xsl:if test="position() = 42">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o29,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiInhalationZone"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o67,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiUNnum"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o167,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiProperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o772,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiDescription"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o852,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiPackingGroup"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H62;o932,370;f0;c25;w10;h10;d3,</xsl:text>
				<xsl:value-of select="hzrdiMFAG"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1027,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiERGNumber"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1092,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiLtdQty"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o1232,370;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:if test="hzrdiWeight != ''">
					<xsl:value-of select='format-number(hzrdiWeight, "0000.00")'/>
				</xsl:if>				
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o167,385;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiHazIdUpper"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o167,400;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="hzrdiSubstanceLower"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>			
		</xsl:if>
	</xsl:for-each> 
		<!-- ***** Placard *****  -->
	<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H125;o29,560;f0;c25;w12;h12;d3,Placard:</xsl:text>
		<xsl:for-each select="argo:tranHazard">
			<xsl:if test="position() &gt; 39 and position() &lt;= 42">
				<xsl:for-each select="argo:tranHazardPlacard">
					<xsl:value-of select="placardText"/>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
			</xsl:if>
	</xsl:for-each>
		<!-- ***** End of Transfer Block *****  -->		
	<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text>			 
	 	</xsl:when>
	</xsl:choose>	
	<xsl:text disable-output-escaping="yes"> 
   
         &lt;STX&gt; &lt;ESC&gt;C                        &lt;ETX&gt;
         &lt;STX&gt; &lt;ESC&gt;P                        &lt;ETX&gt;
         &lt;STX&gt; E1;F1;                              &lt;ETX&gt;
     </xsl:text> 
	 
	 <!-- Printer Enhancement below section is updated to move the field o50 on each field to print the VVD and Booking number vertically  -->
 
      <!-- ***** TERMINAL ID *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H65;o350,25;f0;c25;w28;h28;d3,Delivery Receipt &lt;ETX&gt; </xsl:text>

	 <!-- ***** TRANSACTION TYPE *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o900,25;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="tranSubType"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	
	 
     <!-- ***** EquipType&Grade Id *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o900,50;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranCtrTypeId"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** EquipType&Grade Desc*****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o900,75;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranGradeId"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	
	 
	 <!-- *****  Company Legal Form *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o75,75;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranUfvFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
     <!-- ***** Page No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o75,110;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="tranUnitFlexString02"/>	 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Stop No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o300,110;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString02"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
 
      <!-- ***** Cons PO#. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o550,110;f0;c25;w12;h12;d3,Cons PO#:</xsl:text>
	 <xsl:value-of select="tranUnitFlexString01"/>	 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Line Separator *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L62;o75,145;f0;l945;w3;</xsl:text> 	

     <!-- ***** Box1 *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; W5,Box1;o1020,110;f0;l400;h190;w3;</xsl:text>

	 <!-- ***** TIR No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H6;o75,150;f0;c25;w12;h12;d3,TIR No:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** OUT Time *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H7;o300,150;f0;c25;w12;h12;d3,OUT:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Shipper *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H8;o550,150;f0;c25;w12;h12;d3,Shipper:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Consignee Signature *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o1025,115;f0;c25;w12;h12;d3,Consignee Signature</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 

	 <!-- ***** TIR No. text *****  -->	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H10;o75,175;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** OUT Time text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H11;o300,175;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Shipper text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H12;o550,175;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranShipperName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Container No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H14;o75,225;f0;c25;w12;h12;d3,Cont.#/Opr:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	  
	 <!-- ***** Chassis No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H15;o300,225;f0;c25;w12;h12;d3,Chassis#/Own:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Genset *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H16;o550,225;f0;c25;w12;h12;d3,Genset#:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Reefer *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H17;o700,225;f0;c25;w12;h12;d3,Reefer:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** Temp *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H18;o805,225;f0;c25;w12;h12;d3,Temp:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Seal No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H19;o900,225;f0;c25;w12;h12;d3,Seal No:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Consignee Name *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H63;o1025,225;f0;c25;w12;h12;d3,Consignee Name</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Container No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o75,250;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="tranCtrNbr"/>
	 <xsl:text> </xsl:text>
	 <xsl:value-of select="argo:tranCtrOperator/bizuId"/>	 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	  
	 <!-- ***** Chassis No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o300,250;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranChsNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Genset text *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o550,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranCtrAccNbr"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Reefer text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o700,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranFlexString08"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** Temp text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o805,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranFlexString06"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** Consignee Name Text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H64;o1025,250;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranConsigneeName"/>	 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 
	 
     <!-- ***** Seal No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o900,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranSealNbr1"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** VVD *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o75,300;f0;c25;w12;h12;d3,VVD:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** OPD *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o300,300;f0;c25;w12;h12;d3,OPD:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 
	 
     <!-- ***** Dest City *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o550,300;f0;c25;w12;h12;d3,Dest City:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Yard Row *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o825,300;f0;c25;w12;h12;d3,Yard Row:</xsl:text>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Service *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o1075,300;f0;c25;w12;h12;d3,Service:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** Booking *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o1200,300;f0;c25;w12;h12;d3,Bkg/BOL No:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 

     <!-- ***** VVD text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o75,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="argo:tranCarrierVisit/cvId"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** OPD text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o300,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="argo:tranDischargePoint1/pointId"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Destination text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o550,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranDestination"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Yard Row text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o825,325;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranFlexString03"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Service text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o1075,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranFlexString07"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Booking text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o1200,325;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="tranEqoNbr"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Consignee Address text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o75,350;f0;c25;w10;h10;d3,Consignee Address:</xsl:text> 
     <xsl:value-of select="tranConsigneeId"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 

	 <!-- ***** NOTE text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o75,400;f0;c25;w10;h10;d3,Cargo Notes:</xsl:text> 
     <xsl:value-of select="tranNotes"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o75,415;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString04"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o75,430;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString05"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L42;o75,465;f0;l1325;w3;</xsl:text> 

	 <!-- ***** Trucking Company *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o75,485;f0;c25;w12;h12;d3,Trucking Co: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Trucking Code *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o450,485;f0;c25;w12;h12;d3,Truck Code: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Trucker Name *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o650,485;f0;c25;w12;h12;d3,Trucker Name: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Cont. Wgt *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o950,485;f0;c25;w12;h12;d3,Cont. Wgt:</xsl:text>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Clerk User *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o1200,485;f0;c25;w12;h12;d3,Clerk: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 
	 <!-- ***** Trucking Company text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o75,515;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Trucking Code text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o450,515;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompany"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Trucker Name text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o650,515;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsDriverName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Cont. Wgt text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o950,515;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranCtrGrossWeight"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Clerk User text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1200,515;f0;c25;w10;h10;d3,</xsl:text>
     <xsl:value-of select="tranCreator"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L53;o75,560;f0;l1325;w3;</xsl:text> 

     <!-- ***** DISCLAIMER MESSAGES *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o75,570;f0;c25;w8;h8;d3,I hereby certify that on the date stated, I carefully inspected the equipment described above and that this is a true and correct</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o75,585;f0;c25;w8;h8;d3,report of the results of such an inspection and that possession of such equipment was taken on behalf of the carrier or above</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o75,600;f0;c25;w8;h8;d3,named steamship line at the place and date indicated. The interchange is made subject to the terms and conditions of the currently</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o75,615;f0;c25;w8;h8;d3,effective trailer interchange contractual provisions between named steamship line and the above mentioned carrier.</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	<!-- Printer Enhancement to include booking and VVD vertically at the begining of the ticket Start-->
	 
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H66;o0,525;f1;c25;w16;h16;d3,</xsl:text> 
	 <xsl:value-of select="argo:tranCarrierVisit/cvId"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H67;o0,325;f1;c25;w16;h16;d3,</xsl:text> 
	 <xsl:value-of select="tranEqoNbr"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- Printer Enhancement to include booking and VVD vertically at the begining of the ticket End-->

     <!-- ***** End of Transfer Block *****  -->
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text> 
</xsl:template>
</xsl:stylesheet>