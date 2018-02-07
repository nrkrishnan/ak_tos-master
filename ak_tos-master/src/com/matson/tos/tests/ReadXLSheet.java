package com.matson.tos.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.DateCell;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

 
public class ReadXLSheet {
 
	public void procCrew(String filePath) {
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(new File(filePath));
			
			ArrayList crewList =  contentReading(fs);
			StringBuffer strBulder = new StringBuffer("<CREW_LIST>");
				
			for(int i=0; i< crewList.size(); i++){
				LinkedHashSet crewDetail = (LinkedHashSet)crewList.get(i); 
				Iterator it = crewDetail.iterator();
				StringBuffer tempCrewDate = new StringBuffer();
				String tempStr = null;
				int count=0;
				while(it.hasNext()){
	
				  //System.out.println(it.next());
				  String temp = (String)it.next();	
				   if(temp == null || temp.trim().length() == 0){
					 //Email FSS Team to rectify CREW Information 
					  System.out.println("Rectify Crew Information");
				  }
				  if(count ==0){  tempStr="<LAST_NAME>"+temp+"</LAST_NAME>";
				      tempCrewDate.append(tempStr);
				  }else if(count == 1){ tempStr="<FIRST_NAME>"+temp+"</FIRST_NAME>"; 
				      tempCrewDate.append(tempStr);
				  }else if (count == 2){
					  //def gvyEventUtil = getGroovyClassInstance("GvyEventUtil")
					  temp = formatDate(temp,"MM/dd/yyyy", "yyyy-MM-dd");
					  tempStr="<BIRTH_DT>"+temp+"</BIRTH_DT>";
					  tempCrewDate.append(tempStr);
				  }else if (count == 3){ tempStr="<GENDER>"+temp+"</GENDER>";
				      tempCrewDate.append(tempStr);
				  }else if (count == 4){ tempStr="<NATIONALITY>"+temp+"</NATIONALITY>";
				      tempCrewDate.append(tempStr);
				  }else if (count == 5){ tempStr="<ID_NUM>"+temp+"</ID_NUM>";
				      tempCrewDate.append(tempStr); 
				  }else if (count == 6){ tempStr="<POSITION>"+temp+"</POSITION>";
				      tempCrewDate.append(tempStr);
				  }else if (count == 7){ tempStr="<EMBARK_PLACE>"+temp+"</EMBARK_PLACE>";
				      tempCrewDate.append(tempStr);
				  }else if (count == 8){
					  temp = formatDate(temp,"MM/dd/yyyy", "yyyy-MM-dd");
					  tempStr="<EMBARK_DATE>"+temp+"</EMBARK_DATE>";
					  tempCrewDate.append(tempStr); 
				  }
					count++;
					
				}
				count=0;
				if(tempCrewDate != null && tempCrewDate.length()!= 0){
					//System.out.println("tempCrewDate =="+tempCrewDate);
				  strBulder.append(crewListAddon()+tempCrewDate.toString()+"</CREW>");
				}
			}
			strBulder.append("</CREW_LIST>");
			System.out.println("strBulder ===="+strBulder);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
    
	
	//Returns the Headings used inside the excel sheet
	public void getHeadingFromXlsFile(Sheet sheet) {
		int columnCount = sheet.getColumns();
		for (int i = 0; i < columnCount; i++) {
			System.out.println("Heading"+sheet.getCell(i, 0).getContents());
		}
	}
 
	public ArrayList contentReading(InputStream fileInputStream) {
		WorkbookSettings ws = null;
		Workbook workbook = null;
		Sheet s = null;
		Cell rowData[] = null;
		int rowCount = '0';
		int columnCount = '0';
		DateCell dc = null;
		int totalSheet = 0;
		ArrayList crewList = null;
		try {
			ws = new WorkbookSettings();
			ws.setLocale(new Locale("en", "EN"));
			workbook = Workbook.getWorkbook(fileInputStream, ws);
 
			totalSheet = workbook.getNumberOfSheets();
			if(totalSheet > 0) {
				System.out.println("Total Sheet Found:" + totalSheet);
				for(int j=0;j<totalSheet ;j++) {
					System.out.println("Sheet Name:" + workbook.getSheet(j).getName());
				}
			}
 
			//Getting Default Sheet i.e. 0
			s = workbook.getSheet(0);
 
			//Reading Individual Cell
			//getHeadingFromXlsFile(s);
 
			//Total Total No Of Rows in Sheet, will return you no of rows that are occupied with some data
			System.out.println("Total Rows inside Sheet:" + s.getRows());
			rowCount = s.getRows();
 
			//Total Total No Of Columns in Sheet
			System.out.println("Total Column inside Sheet:" + s.getColumns());
			columnCount = s.getColumns();
 
		LinkedHashSet crewSet = null;
		crewList = new ArrayList();
		
		for (int i = 0; i < rowCount; i++) {
				//Get Individual Row
			System.out.println("i =="+i);
			    crewSet = new LinkedHashSet();
				rowData = s.getRow(i);
				if (i > 5 && rowData[0].getContents() != null) { // the first date column must not null
					for (int j = 0; j < columnCount; j++) {
						System.out.println("Column count"+j);
						if(j > 0 && j < 9){ //Skip the First Column
							//System.out.println(" rowData="+rowData[j].getContents());
							crewSet.add(rowData[j].getContents());
						}
					}
				}
			  crewList.add(crewSet);
			}
			
			workbook.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
		return crewList;
	}
	
	
 
	
	 //A7 - Convert Date String from One Format to another
	  public static String formatDate(String str_date,String fromDtFormat, String toDtFormat)
	  {
	       java.text.DateFormat formatter = null ; Date date = null;  String finalDate = null;
	        try
	        {
	            formatter = new java.text.SimpleDateFormat(fromDtFormat);
	            date = (Date)formatter.parse(str_date);  
	            java.text.SimpleDateFormat reqformat = new java.text.SimpleDateFormat(toDtFormat);
	            finalDate = reqformat.format(date);
	        }catch(Exception e){
	             e.printStackTrace();
	        }
	        return finalDate;
	    }
	  
		public static void main(String[] args) {
		try {
			ReadXLSheet xlReader = new ReadXLSheet();
			xlReader.procCrew("C:/Documents and Settings/gxr/Desktop/NOA_CHANGE/KOKUA.xls");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public String crewListAddon(){
		String crewlist = "<CREW>"+
			"<MIDDLE_NAME></MIDDLE_NAME>"+
			"<NATIONALITY_CODE>US</NATIONALITY_CODE>"+
			"<COUNTRY_RESIDENCE>UNITED STATES</COUNTRY_RESIDENCE>"+
			"<COUNTRY_RESIDENCE_CODE>US</COUNTRY_RESIDENCE_CODE>"+
			"<ID_TYPE>Passport Number</ID_TYPE>"+
			"<ID_COUNTRY>UNITED STATES</ID_COUNTRY>"+
			"<ID_COUNTRY_CODE>US</ID_COUNTRY_CODE>"+
			"<ID_EXPIRATION_DT xsi:nil='true'></ID_EXPIRATION_DT>"+
			"<EMBARK_COUNTRY>UNITED STATES</EMBARK_COUNTRY>"+
			"<EMBARK_COUNTRY_CODE>US</EMBARK_COUNTRY_CODE>"+
			"<EMBARK_STATE>HAWAII</EMBARK_STATE>"+
			"<EMBARK_PORT_NAME>HONOLULU</EMBARK_PORT_NAME>"+
			"<EMBARK_PORT_CODE>HON</EMBARK_PORT_CODE>"+
			"<DEBARK_COUNTRY></DEBARK_COUNTRY>"+
			"<DEBARK_COUNTRY_CODE></DEBARK_COUNTRY_CODE>"+
			"<DEBARK_STATE></DEBARK_STATE>"+
			"<DEBARK_PORT_NAME></DEBARK_PORT_NAME>"+
			"<DEBARK_PORT_CODE></DEBARK_PORT_CODE>"+
			"<DEBARK_PLACE></DEBARK_PLACE>"+ 
			"<DEBARK_DATE xsi:nil='true'></DEBARK_DATE>"; 
		return crewlist;
	
	}
} 
