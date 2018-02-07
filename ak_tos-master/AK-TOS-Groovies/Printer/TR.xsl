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
 
      <!-- ***** Page No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H0;o25,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="tranUnitFlexString02"/>	 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** TERMINAL ID *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H2;o250,50;f0;c25;w28;h28;d3,</xsl:text> 
     <xsl:value-of select="tranUfvFlexString01"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H61;o700,50;f0;c25;w28;h28;d3,TIR &lt;ETX&gt;</xsl:text>  
 
	 <!-- ***** Company Legal Form *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H1;o250,100;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranUfvFlexString02"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>
	 
	<!-- ***** TRANSACTION TYPE *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H3;o900,50;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="tranSubType"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 

     <!-- ***** EquipType&Grade Id *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H4;o900,85;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranCtrTypeId"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

	 <!-- ***** EquipType&Grade Desc*****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H60;o900,110;f0;c25;w12;h12;d3,</xsl:text> 
     <xsl:value-of select="tranGradeId"/>
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 

     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L5;o25,145;f0;l1375;w3;</xsl:text> 
	 
     <!-- ***** TIR No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H6;o25,150;f0;c25;w12;h12;d3,TIR No:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** OUT Time *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H7;o250,150;f0;c25;w12;h12;d3,OUT:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** IN Time *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H8;o500,150;f0;c25;w12;h12;d3,IN:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Damages *****  -->
	<xsl:variable name="tranType" select="tranSubType"/>
    <xsl:choose>
		<xsl:when test="$tranType='Receive Work Order'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o775,150;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:text>Chassis Defects Reported:</xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
         </xsl:when>
        <xsl:when test="$tranType='Receive Chassis'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o775,150;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:text>Chassis Defects Reported:</xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Export'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o775,150;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:text>Chassis Defects Reported:</xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Empty'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o775,150;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:text>Chassis Defects Reported:</xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Break-Bulk'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o775,150;f0;c25;w12;h12;d3,</xsl:text> 
			<xsl:text>Chassis Defects Reported:</xsl:text>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>			  
         <xsl:otherwise>
			<xsl:variable name="ufvFlex09" select="tranUfvFlexString09"/>
			<xsl:if test="$ufvFlex09='DO NOT BACKLOAD'">
				<xsl:text>Chassis Defects Reported:</xsl:text>
				<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H9;o775,150;f0;c25;w28;h28;d3,DO NOT BACKLOAD</xsl:text> 
				<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
			</xsl:if>		
         </xsl:otherwise> 		
    </xsl:choose>	 
			
	 <!-- ***** TIR No. text *****  -->	
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H10;o25,175;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** OUT Time text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H11;o250,175;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="../argo:truckVisit/tvdtlsTrkEndTime"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** IN Time text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H12;o500,175;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="../argo:truckVisit/tvdtlsTrkStartTime"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Damages text *****  -->
	<xsl:variable name="ttype" select="tranSubType"/>
    <xsl:choose>
		<xsl:when test="$ttype='Receive Work Order'">
			<xsl:for-each select="argo:tranCtrDmg">
				<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H13;o775,175;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="dmgitemType"/>
				<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
			</xsl:for-each>		 
         </xsl:when>
        <xsl:when test="$ttype='Receive Chassis'">
			<xsl:for-each select="argo:tranCtrDmg">
				<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H13;o775,175;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="dmgitemType"/>
				<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
			</xsl:for-each>	
        </xsl:when>
        <xsl:when test="$ttype='Receive Export'">
			<xsl:for-each select="argo:tranCtrDmg">
				<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H13;o775,175;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="dmgitemType"/>
				<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
			</xsl:for-each>	
        </xsl:when>
        <xsl:when test="$ttype='Receive Empty'">
			<xsl:for-each select="argo:tranCtrDmg">
				<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H13;o775,175;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="dmgitemType"/>
				<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
			</xsl:for-each>	
        </xsl:when>
        <xsl:when test="$ttype='Receive Break-Bulk'">
			<xsl:for-each select="argo:tranCtrDmg">
				<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H13;o775,175;f0;c25;w10;h10;d3,</xsl:text> 
				<xsl:value-of select="dmgitemType"/>
				<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>	 
			</xsl:for-each>	
        </xsl:when>			  
    </xsl:choose>	 


     <!-- ***** Container No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H14;o25,225;f0;c25;w12;h12;d3,Cont.#/Opr:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	  
	 <!-- ***** Chassis No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H15;o250,225;f0;c25;w12;h12;d3,Chassis#/Own:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Genset *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H16;o500,225;f0;c25;w12;h12;d3,Genset#:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Reefer *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H17;o650,225;f0;c25;w12;h12;d3,Reefer:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** Temp *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H18;o775,225;f0;c25;w12;h12;d3,Temp:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Seal No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H19;o900,225;f0;c25;w12;h12;d3,Seal No:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Service *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H20;o1050,225;f0;c25;w12;h12;d3,Service:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Booking No. *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H21;o1225,225;f0;c25;w12;h12;d3,Bkg/BOL No:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 


     <!-- ***** Container No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H22;o25,250;f0;c25;w12;h12;d3,</xsl:text>
	 <xsl:value-of select="tranCtrNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	  
	 <!-- ***** Chassis No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H23;o250,250;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranChsNbr"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Genset text *****  -->
	 <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H24;o500,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranCtrAccNbr"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Reefer text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H25;o650,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranFlexString08"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** Temp text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H26;o775,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranFlexString06"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <!-- ***** Seal No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H27;o900,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranSealNbr1"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Service text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H28;o1050,250;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranFlexString07"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Booking No. text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H29;o1225,250;f0;c25;w12;h12;d3,</xsl:text> 
	 <xsl:value-of select="tranEqoNbr"/>     
	 <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 

     <!-- ***** VVD *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H30;o25,300;f0;c25;w12;h12;d3,VVD:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** OPD *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H31;o250,300;f0;c25;w12;h12;d3,OPD:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 
	 
     <!-- ***** Dest City *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H32;o500,300;f0;c25;w12;h12;d3,Dest City:</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Yard Row *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H33;o775,300;f0;c25;w12;h12;d3,Yard Row:</xsl:text>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 
	 <!-- ***** Shipper/Consignee *****  -->
	 <xsl:variable name="tranType" select="tranSubType"/>
     <xsl:choose>
		<xsl:when test="$tranType='Receive Work Order'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1050,300;f0;c25;w12;h12;d3,Shipper:</xsl:text> 
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Chassis'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1050,300;f0;c25;w12;h12;d3,Shipper:</xsl:text> 
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Export'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1050,300;f0;c25;w12;h12;d3,Shipper:</xsl:text> 
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Empty'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1050,300;f0;c25;w12;h12;d3,Shipper:</xsl:text> 
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>
        <xsl:when test="$tranType='Receive Break-Bulk'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1050,300;f0;c25;w12;h12;d3,Shipper:</xsl:text> 
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
        </xsl:when>			  
         <xsl:otherwise>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H34;o1050,300;f0;c25;w12;h12;d3,Consignee:</xsl:text> 
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>  
         </xsl:otherwise> 		
    </xsl:choose> 

     <!-- ***** VVD text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H35;o25,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="argo:tranCarrierVisit/cvId"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text>

     <!-- ***** OPD text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H36;o250,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="argo:tranDischargePoint1/pointId"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Destination text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H37;o500,325;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="tranDestination"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Yard Row text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H38;o775,325;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranFlexString03"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Shipper/Consignee text *****  -->
	 <xsl:variable name="tranType" select="tranSubType"/>
     <xsl:choose>
		<xsl:when test="$tranType='Receive Work Order'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o1050,325;f0;c25;w10;h10;d3,</xsl:text>
			<xsl:value-of select="tranShipperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
        </xsl:when>
        <xsl:when test="$tranType='Receive Chassis'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o1050,325;f0;c25;w10;h10;d3,</xsl:text>
			<xsl:value-of select="tranShipperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
        </xsl:when>
        <xsl:when test="$tranType='Receive Export'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o1050,325;f0;c25;w10;h10;d3,</xsl:text>
			<xsl:value-of select="tranShipperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
        </xsl:when>
        <xsl:when test="$tranType='Receive Empty'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o1050,325;f0;c25;w10;h10;d3,</xsl:text>
			<xsl:value-of select="tranShipperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
        </xsl:when>
        <xsl:when test="$tranType='Receive Break-Bulk'">
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o1050,325;f0;c25;w10;h10;d3,</xsl:text>
			<xsl:value-of select="tranShipperName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
        </xsl:when>			  
         <xsl:otherwise>
			<xsl:text disable-output-escaping="yes"> &lt;STX&gt; H39;o1050,325;f0;c25;w10;h10;d3,</xsl:text>
			<xsl:value-of select="tranConsigneeName"/>
			<xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
         </xsl:otherwise> 		
    </xsl:choose>	 

	 <!-- ***** NOTE  *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H40;o25,375;f0;c25;w12;h12;d3,Cargo Notes: &lt;ETX&gt;</xsl:text> 

	 <!-- ***** NOTE text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H41;o25,400;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranNotes"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H58;o25,425;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString04"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H59;o25,450;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="tranFlexString05"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 	 
	 
	 <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L42;o25,475;f0;l1375;w3;</xsl:text> 

	 <!-- ***** Trucking Company *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H43;o25,485;f0;c25;w12;h12;d3,Trucking Co: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Trucking Code *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H44;o400,485;f0;c25;w12;h12;d3,Truck Code: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Trucker Name *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H45;o600,485;f0;c25;w12;h12;d3,Trucker Name: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Cont. Wgt *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H46;o900,485;f0;c25;w12;h12;d3,Cont. Wgt:</xsl:text>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Clerk User *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H47;o1225,485;f0;c25;w12;h12;d3,Clerk: </xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 
	 <!-- ***** Trucking Company text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H48;o25,515;f0;c25;w10;h10;d3,</xsl:text> 
     <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompanyName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** Trucking Code text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H49;o400,515;f0;c25;w10;h10;d3,</xsl:text> 
	 <xsl:value-of select="../argo:truckVisit/tvdtlsTrkCompany"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Trucker Name text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H50;o600,515;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="../argo:truckVisit/tvdtlsDriverName"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

	 <!-- ***** Cont. Wgt text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H51;o900,515;f0;c25;w10;h10;d3,</xsl:text>
	 <xsl:value-of select="tranCtrGrossWeight"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 <!-- ***** Clerk User text *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H52;o1225,515;f0;c25;w10;h10;d3,</xsl:text>
     <xsl:value-of select="tranCreator"/>
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
	 
     <!-- ***** Line Separator *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; L53;o25,550;f0;l1375;w3;</xsl:text> 

     <!-- ***** DISCLAIMER MESSAGES *****  -->
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H54;o25,560;f0;c25;w8;h8;d3,I hereby certify that on the date stated, I carefully inspected the equipment described above and that this is a true and correct</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H55;o25,575;f0;c25;w8;h8;d3,report of the results of such an inspection and that possession of such equipment was taken on behalf of the carrier or above</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 
	 
     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H56;o25,590;f0;c25;w8;h8;d3,named steamship line at the place and date indicated. The interchange is made subject to the terms and conditions of the currently</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <xsl:text disable-output-escaping="yes"> &lt;STX&gt; H57;o25,605;f0;c25;w8;h8;d3,effective trailer interchange contractual provisions between named steamship line and the above mentioned carrier.</xsl:text> 
     <xsl:text disable-output-escaping="yes"> &lt;ETX&gt; </xsl:text> 

     <!-- ***** End of Transfer Block *****  -->
     <xsl:text disable-output-escaping="yes"> 
         &lt;STX&gt; R; &lt;ESC&gt;E1; CAN; &lt;ETX&gt;
         &lt;STX&gt;&lt;ETB&gt;&lt;ETX&gt;
     </xsl:text> 

</xsl:template>

</xsl:stylesheet>