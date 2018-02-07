package com.matson.tos.jatb;

/**
 * Created by brajamanickam on 7/17/2017.
 */
public class Cupd {

    //CUPD|MATZ900123X|YYYYMMDD|YYYYMMDD
	
    private String txnCode;

    private String chassisNumber;
    private String inspectionDuedate;
    private String inspectionDate;


    public String getTxnCode() {
        return txnCode;
    }

    public void setTxnCode(String txnCode) {
        this.txnCode = txnCode;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public String getInspectionDuedate() {
        return inspectionDuedate;
    }

    public void setInspectionDuedate(String inspectionDuedate) {
        this.inspectionDuedate = inspectionDuedate;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(String inspectionDate) {
        this.inspectionDate = inspectionDate;
    }


}
