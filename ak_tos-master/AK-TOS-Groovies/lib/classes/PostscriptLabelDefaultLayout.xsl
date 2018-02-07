<?xml version="1.0" encoding="UTF-8"?>
<!-- Layout needs to be defined to support the Dropoff layout. This is just a copy of the default layout -->

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:argo="http://www.navis.com/argo"
                exclude-result-prefixes="fo">
    <xsl:template name="PrintDate">
        <SCRIPT language="JavaScript">
            document.write(new Date();
        </SCRIPT>
    </xsl:template>

    <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
    <!-- root element: -->
    <xsl:template match="/">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="roll80mm" page-height="indefinite" page-width="80mm" margin-top="10mm" margin-bottom="0mm"
                                       margin-left="5mm" margin-right="5mm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="roll80mm">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="10pt">
                        <fo:table table-layout="fixed">
                            <fo:table-column column-width="7cm"/>
                            <fo:table-body>
                                <xsl:apply-templates/>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="argo:docDescription">
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="20pt" font-weight="bold">
                    <xsl:value-of select="docName"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
	</xsl:template>

    <xsl:template match="argo:truckVisit">
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Bat Number
                    <xsl:value-of select="tvdtlsBatNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Time:
                    <xsl:value-of select="../argo:trkTransaction/tranCreated"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">User:
                    <xsl:value-of select="../argo:trkTransaction/tranCreator"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Tran Nbr:
                    <xsl:value-of select="../argo:trkTransaction/tranNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Truck Id:
                    <xsl:value-of select="tvdtlsTrkId"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Carrier:
                    <xsl:value-of select="tvdtlsTrkCompany"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Truck In Time:
                    <xsl:value-of select="tvdtlsTrkStartTime"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Truck Out Time:
                    <xsl:value-of select="tvdtlsTrkEndTime"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Truck Turn Time:
                    <xsl:value-of select="tvdtlsDuration"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>
    </xsl:template>

    <xsl:template match="argo:trkTransaction">
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Container:
                    <xsl:value-of select="tranCtrNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">ISO:
                    <xsl:value-of select="tranCtrTypeId"/>
                    Wt:
                    <xsl:value-of select="tranCtrGrossWeight"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Type:
                    <xsl:value-of select="tranSubType"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Ves Ref:
                    <xsl:value-of select="argo:tranCarrierVisit/cvId"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">DP:
                    <xsl:value-of select="argo:tranDischargePoint1/pointId"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Line Op:
                    <xsl:value-of select="argo:tranCtrOperator/bizuId"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Ves Name:
                    <xsl:value-of select="argo:tranCarrierVisit/cvCvdCarrierVehicleName"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Inbound Vyg Nbr :
                    <xsl:value-of select="argo:tranCarrierVisit/cvCvdCarrierIbVygNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Outbound Vyg Nbr:
                    <xsl:value-of select="argo:tranCarrierVisit/cvCvdCarrierObVygNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">ETA:
                    <xsl:value-of select="argo:tranCarrierVisit/cvCvdETA"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">ETD :
                    <xsl:value-of select="argo:tranCarrierVisit/cvCvdETD"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">BIL Nbr:
                    <xsl:value-of select="argo:trkTransaction/tranBlNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Commodity:
                    <xsl:value-of select="argo:tranCommodity/cmdyId"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Ctrl Temp:
                    <xsl:value-of select="tranTempRequired"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Hazard Code:
                    <xsl:value-of select="argo:tranHazard/hzrdiImdgCode"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Piece Count:
                    <xsl:value-of select="argo:tranHazard/hzrdiQuantity"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Package Type :
                    <xsl:value-of select="argo:tranHazard/hzrdiPackageType"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Proper Name:
                    <xsl:value-of select="argo:tranHazard/hzrdiProperName"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Emergency Telephone:
                    <xsl:value-of select="argo:tranHazard/hzrdiEmergencyTelephone"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Packaging Group :
                    <xsl:value-of select="argo:tranHazard/hzrdiPackingGroup"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Limited Qty:
                    <xsl:value-of select="argo:tranHazard/hzrdiLtdQty"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Hazard Class:
                    <xsl:value-of select="argo:tranHazard/hzrdiDescription"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">UN number :
                    <xsl:value-of select="argo:tranHazard/hzrdiUNnum"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Weight:
                    <xsl:value-of select="argo:tranHazard/hzrdiWeight"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row/>

        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Release/Booking:
                    <xsl:value-of select="tranEqoNbr"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" space-after="5mm">Position:
                    <xsl:value-of select="argo:tranCtrPosition/posLocId"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt">Is OOG:
                    <xsl:value-of select="tranIsOog"/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
            <fo:table-cell>
                <fo:block space-after="5mm"/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="argo:Messages">
        <fo:table-row>
            <fo:table-cell>
                <fo:block font-size="10pt" font-weight="bold">Messages:</fo:block>
            </fo:table-cell>
        </fo:table-row>
        <xsl:call-template name="retrieveAllData"/>
        <fo:table-row>
            <fo:table-cell>
                <fo:block space-before="5mm">.</fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="retrieveAllData">
        <xsl:for-each select="*|@*|text()">
            <fo:table-row>
                <fo:table-cell>
                    <fo:block>
                        <xsl:value-of select="."/>
                    </fo:block>
                </fo:table-cell>
            </fo:table-row>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
