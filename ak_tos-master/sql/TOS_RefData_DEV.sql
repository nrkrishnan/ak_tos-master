-- Truncate tables to avoid duplicated records
TRUNCATE TABLE TOSCSTMMGR.TOS_APP_PARAMETER;
TRUNCATE TABLE TOSCSTMMGR.TOS_DEST_POD_LOOKUP;
TRUNCATE TABLE TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP;
Commit;

-- File FTP Proxy IDs. This needs to be modified for each env. The value of all FTP IDs are created by
-- CAS FTP Admin web application
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'VES_FILES_FTP_ID', '101');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'DCM_IN_FTP_ID', '102');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'DCM_OUT_FTP_ID', '103');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'FTP_DCM_REP', '203');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'FTP_ID_N4', '451');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'FTP_ID_CMIS', '452');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'FTP_ID_UPLOAD', '501');


-- TOS app param
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'MAIL_HOST', 'mailhost');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'SUPPORT_EMAIL', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'TIMER_DCM', '7');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'TIMER_NEW_VES', '9');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'TIMER_STIF', '5');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'TIMER_VES_SCHEDULE', '11');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'TIMER_CMIS_FTP', '10');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'MAX_MESSAGE_TO_SEND', '3');


-- for DCM emails
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_KAU', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_LHE', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_LUR', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MAT', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MAU', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MHI', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MKA', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MKI', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MLE', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MLI', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MNA', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_MWI', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_RJP', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_KAH', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_NAW', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_GM', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_LHMA', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_BMA', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_NCB', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_SHIPP_HON', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_HIL', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( 'EMAIL_DCM_KHI', '1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( ‘EMAIL_GATE_TEMP’,'1tdpqa@matson.com');
insert into TOSCSTMMGR.TOS_APP_PARAMETER ( KEY, VALUE) values ( ‘EMAIL_USCG_GRP’,'1tdpqa@matson.com');


-- Insert for STIF file lookup
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'HIL', 'HIL', '', 'Hilo');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'KHI', 'KHI', '', 'Kawaihae');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'NAW', 'NAW', '', 'Nawiliwili');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'KAH', 'KAH', '', 'Kahului');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'HON', 'HON', '', 'Honolulu');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'MOL', 'MOL', '', 'Molokai');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'LNI', 'LNI', '', 'Lanai');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'MIX', 'HON', '', 'Mixed Ports');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'WAK', 'HON', '', 'Wake Island');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'OPT', 'OPT', '', 'Optional');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'GUM', 'GUM', 'GUM', 'Guam');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'KMI', 'GUM', 'KMI', 'Kosrae');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'PNP', 'GUM', 'PNP', 'Pohnpei');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'PUX', 'GUM', 'PUX', 'Palau');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'RTA', 'GUM', 'RTA', 'Rota');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'SPN', 'GUM', 'SPN', 'Saipan');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'TIN', 'GUM', 'TIN', 'Tinian');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'TMGU', 'GUM', 'GUM', 'Tamuning');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'UUK', 'GUM', 'UUK', 'Chuuk');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'YAP', 'GUM', 'YAP', 'Yap');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'EBY', 'GUM', 'EBY', 'Ebeye');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'KWJ', 'GUM', 'KWJ', 'Kwajalein');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'MAJ', 'GUM', 'MAJ', 'Majuro');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'SHA', 'SHA', '', 'Shanghai');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'NGB', 'NGB', '', 'Ningbo');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'SEA', '', '', 'Seattle');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'OAK', '', '', 'Oakland');
insert into TOSCSTMMGR.TOS_DEST_POD_LOOKUP ( DEST_ID, POD1_ID, POD2_ID, DESCRIPTION) values ( 'LAX', '', '', 'Los Angeles');
commit;

-- Hold code lookup
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'AG', 'AG', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'CC', 'CC', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'CG', 'CG', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'COM','COM','HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'CUS','CUS','HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'DOC','DOC','HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'GR', 'GR', 'HOLD', 'Equipment');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'GX', 'GX', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'HLD','HLD','HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'HP', 'HP', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'INB','INB','HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'MDA','MDA','HOLD', 'Equipment');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'ON', 'ON', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'RM', 'RM', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'STG','ST', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'TD', 'TD', 'HOLD', 'Unit');
insert into TOSCSTMMGR.TOS_HOLD_PERM_LOOKUP ( ACETS_ID, SN4_ID, TYPE, APPLY_TO) values ( 'XT', 'XT', 'HOLD', 'Unit');

commit;

