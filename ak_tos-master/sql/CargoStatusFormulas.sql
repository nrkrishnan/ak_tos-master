SET DEFINE OFF;
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (1, 'POD=="HON" && FreightKind=="FCL" && GoodsConsigneeName != updt_GoodsConsigneeName && userRole=="Email" && CommodityDescription!="AUTO" && CommodityDescription!="AUTO CON"', '1SN4CargoStatusAcct@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (2, 'POD=="HON" && (FreightKind != updt_FreightKind) && (FreightKind=="FCL" ||updt_FreightKind=="FCL") && userRole=="Email" && CommodityDescription!="AUTO" && CommodityDescription!="AUTO CON"', '1SN4CargoStatusAcct@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (4, 'POD=="HON" && (CommodityDescription=="AUTO" || updt_CommodityDescription=="AUTO") && ( CommodityDescription != updt_CommodityDescription) && userRole=="Email"', '1SN4CargoStatusAutoLot@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (5, 'Destination=="MIX" && (FreightKind != updt_FreightKind) && (FreightKind=="LCL" || updt_FreightKind=="LCL") && userRole=="Email"', '1SN4CargoStatusAutoLot@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (6, 'POD=="HON" && updt_POD!="HON" && CommodityDescription=="AUTO" && userRole=="Email"', '1SN4CargoStatusAutoLot@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (7, 'CommodityDescription=="AUTO" && (EVENT_ID=="CUS_HOLD" || EVENT_ID == "CUS_RELEASE") && userRole=="Email"', '1SN4CargoStatusAutoLot@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (8, 'POD=="LNI" && ( EVENT_ID=="CC_HOLD" ||  EVENT_ID=="CC_RELEASE" || EVENT_ID== "HP_HOLD" || EVENT_ID == "HP_RELEASE") && userRole=="Email"', '1SN4CargoStatusCops@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (9, 'POD=="LNI" && ( EVENT_ID=="ON_HOLD" ||  EVENT_ID=="ON_RELEASE") && userRole=="Email"', '1SN4CargoStatusCops@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (10, 'POD=="MOL" && ( EVENT_ID=="CC_HOLD" ||  EVENT_ID=="CC_RELEASE" || EVENT_ID== "HP_HOLD" || EVENT_ID == "HP_RELEASE") && userRole=="Email"', '1SN4CargoStatusCops@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (11, 'POD=="MOL" && ( EVENT_ID=="ON_HOLD" ||  EVENT_ID=="ON_RELEASE") && userRole=="Email"', '1SN4CargoStatusCops@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (12, '(POD=="LNI" && updt_POD!="LNI") && userRole=="Email"', '1SN4CargoStatusCops@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (13, '(POD=="MOL" && updt_POD!="MOL") && userRole=="Email"', '1SN4CargoStatusCops@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (14, 'DrayStatus != updt_DrayStatus && (DrayStatus=="DRAY OUT AND BACK" || DrayStatus=="DRAY IN") && userRole=="Email" && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (15, 'POD=="HON" && FreightKind=="FCL" && ( EVENT_ID=="AG_HOLD" || EVENT_ID=="AG_RELEASE" || EVENT_ID=="XT_HOLD" || EVENT_ID=="XT_RELEASE") && userRole=="Email" && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (16, 'FreightKind=="FCL" && ( EVENT_ID =="INB_HOLD" || EVENT_ID == "INB_RELEASE") && userRole=="Email"  && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (17, 'POD=="HON" && FreightKind=="FCL" && GoodsConsigneeName != updt_GoodsConsigneeName && userRole=="Email" && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (18, 'POD=="HON" && FreightKind != updt_FreightKind && (FreightKind == "FCL" || updt_FreightKind=="FCL") && userRole=="Email"  && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (19, 'POD=="HON" && FreightKind=="FCL" && ( EVENT_ID =="CC_HOLD" || EVENT_ID == "CC_RELEASE" || EVENT_ID=="HP_HOLD" || EVENT_ID=="HP_RELEASE") && userRole=="Email"  && CommodityDescription!="AUTO" && CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (20, 'POD=="HON" && FreightKind=="FCL" && ( EVENT_ID =="ON_HOLD" || EVENT_ID == "ON_RELEASE") && userRole=="Email"  && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (21, 'POD=="HON" && updt_POD!="HON" && userRole=="Email" && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (22, 'FreightKind=="FCL" && DrayStatus != updt_DrayStatus && (DrayStatus=="DRAY OUT AND BACK" || DrayStatus=="DRAY IN") && userRole=="Email" && CommodityDescription!="AUTO" &&  CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (23, 'FreightKind=="FCL" && ( EVENT_ID =="CUS_HOLD" || EVENT_ID == "CUS_RELEASE") && userRole=="Email" && CommodityDescription!="AUTO" && CommodityDescription!="AUTO CON"', '1SN4CargoStatusCy@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (24, '(POD=="HIL" && updt_POD!="HIL") || (updt_POD=="HIL" && POD!="HIL")', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (25, '(POD=="KAH" && updt_POD!="KAH") || (updt_POD=="KAH" && POD!="KAH")', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (26, '(POD=="KHI" && updt_POD!="KHI") || (updt_POD=="KHI" && POD!="KHI")', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (27, '(POD=="NAW" && updt_POD!="NAW") || (updt_POD=="NAW" && POD!="NAW")', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (28, '(POD=="HIL") && (FreightKind != updt_FreightKind)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (29, '(POD=="HIL") && (GoodsConsigneeName != updt_GoodsConsigneeName)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (30, '(POD=="HIL") && (CommodityDescription != updt_CommodityDescription)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (31, '(POD=="HIL") && (DrayStatus != updt_DrayStatus)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (32, '(POD=="HIL") && (UnitHoldsAndPermissions != updt_UnitHoldsAndPermissions)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (33, '(POD=="HIL") && (SpecialStow != updt_SpecialStow)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (34, '(POD=="HIL") && (RoutingGroup != updt_RoutingGroup)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (35, '(POD=="HIL") && (GoodsBlNbr != updt_GoodsBlNbr)', '1SN4CargoStatusHil@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (36, '(POD=="KAH") && (FreightKind != updt_FreightKind)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (37, '(POD=="KAH") && (GoodsConsigneeName != updt_GoodsConsigneeName)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (38, '(POD=="KAH") && (CommodityDescription != updt_CommodityDescription)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (39, '(POD=="KAH") && (DrayStatus != updt_DrayStatus)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (40, '(POD=="KAH") && (UnitHoldsAndPermissions != updt_UnitHoldsAndPermissions)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (41, '(POD=="KAH") && (SpecialStow != updt_SpecialStow)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (42, '(POD=="KAH") && (RoutingGroup != updt_RoutingGroup)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (43, '(POD=="KAH") && (GoodsBlNbr != updt_GoodsBlNbr)', '1SN4CargoStatusKah@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (44, '(POD=="KHI") && (FreightKind != updt_FreightKind)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (45, '(POD=="KHI") && (GoodsConsigneeName != updt_GoodsConsigneeName)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (46, '(POD=="KHI") && (CommodityDescription != updt_CommodityDescription)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (47, '(POD=="KHI") && (DrayStatus != updt_DrayStatus)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (48, '(POD=="KHI") && (UnitHoldsAndPermissions != updt_UnitHoldsAndPermissions)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (49, '(POD=="KHI") && (SpecialStow != updt_SpecialStow)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (50, '(POD=="KHI") && (RoutingGroup != updt_RoutingGroup)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (51, '(POD=="KHI") && (GoodsBlNbr != updt_GoodsBlNbr)', '1SN4CargoStatusKhi@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (52, '(POD=="NAW") && (FreightKind != updt_FreightKind)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (53, '(POD=="NAW") && (GoodsConsigneeName != updt_GoodsConsigneeName)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (54, '(POD=="NAW") && (CommodityDescription != updt_CommodityDescription)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (55, '(POD=="NAW") && (DrayStatus != updt_DrayStatus)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (56, '(POD=="NAW") && (UnitHoldsAndPermissions != updt_UnitHoldsAndPermissions)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (57, '(POD=="NAW") && (SpecialStow != updt_SpecialStow)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (58, '(POD=="NAW") && (RoutingGroup != updt_RoutingGroup)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (59, '(POD=="NAW") && (GoodsBlNbr != updt_GoodsBlNbr)', '1SN4CargoStatusNaw@matson.com,sysreports@matson.com');
Insert into TOS_CARGO_STATUS_FORMULA_MZ
   (ID, FORMULA_STR, EMAIL_ADDRESSES)
 Values
   (60, 'POD=="HON" && (FreightKind != updt_FreightKind) && (FreightKind=="LCL" || updt_FreightKind=="LCL") && userRole=="Email"', '1SN4CargoStatusAutoLot@matson.com,sysreports@matson.com');
COMMIT;
