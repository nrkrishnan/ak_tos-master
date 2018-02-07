package com.matson.tos.reports;

public class ReportMain {
	
	public static void main(String args[]){
        try{
        	String vesvoy = "MNA250";
        //	NewvesReport.createReport("MTYContainerReport",vesvoy);
        	/*NewvesReport.createReport("MixPortContainerReport",vesvoy);
        	NewvesReport.createReport("BlankConsigneeCSReport",vesvoy);		
        	NewvesReport.createReport("IncorrectDischargePortReport",vesvoy);
        	NewvesReport.createReport("RobContainersOnVesselReport",vesvoy);	

        	NewvesReport.createReport("MultiContainerCellsReport",vesvoy);		
        	NewvesReport.createReport("DuplicateContainerReport",vesvoy);		
        	NewvesReport.createReport("ReeferForFandMContainersReport",vesvoy);
        	NewvesReport.createReport("SFTagReport",vesvoy);			
        	NewvesReport.createReport("DamageCodeReport",vesvoy);*/

        	//NewvesReport.createReport("ProduceReport",vesvoy);
        	/*NewvesReport.createReport("ParadiseBeveragesContainersReport",vesvoy);
        	NewvesReport.createReport("TagConsigneeCallSheetReport",vesvoy);
        	NewvesReport.createReport("DPortChangesReport",vesvoy);
        	NewvesReport.createReport("CustomsReport",vesvoy);*/
        	
        	//NewvesReport.createReport("AGContainerInspectionsReport",vesvoy);        	
        	//NewvesReport.createReport("MISReeferReport",vesvoy);*/
        	
        }catch(Exception e){
        	System.out.println("Error in creating the report");
        	e.printStackTrace();
        }
		
	}	

}
