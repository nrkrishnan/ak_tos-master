package com.matson.tos.jatb;

/**
 * Created by psethuraman on 5/21/2016.
 */
public class Avgx {

    //AVGX¦05182016¦112213  ¦¦¦MATU246909 ¦1401007          ¦INSERT¦1¦4500       ¦0          ¦0
    // ¦0          ¦LBS¦rpallerla@                                   ¦
    private String txnCode;
    private String Date;
    private String time;
    private String filler1;
    private String nextId;
    private String equipmentNumber;
    private String bookingNumber;
    private String status;
    private String method;
    private String grossWeight;
    private String cargoWeight;
    private String tareWeight;
    private String dunnageWeight;
    private String weightUOM;
    private String verifierId;

    /*public String toString() {
        return new StringBuffer(txnCode)
                .append("\n CtrNum|").append(equipmentNumber)
                .append("|BookingNum|").append(bookingNumber)
                .append("|status|").append(status)
                .append("|GrossWt|").append(grossWeight)
                .append("|cargoWt|").append(cargoWeight)
                .append("|tareWt|").append(tareWeight)
                .append("|UOM|").append(weightUOM)
                .append("|authId|").append(verifierId).toString();
    }*/

    public String getTxnCode() {
        return txnCode;
    }

    public void setTxnCode(String txnCode) {
        this.txnCode = txnCode;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFiller1() {
        return filler1;
    }

    public void setFiller1(String filler1) {
        this.filler1 = filler1;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public String getEquipmentNumber() {
        return equipmentNumber;
    }

    public void setEquipmentNumber(String equipmentNumber) {
        this.equipmentNumber = equipmentNumber;
    }

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(String grossWeight) {
        this.grossWeight = grossWeight;
    }

    public String getCargoWeight() {
        return cargoWeight;
    }

    public void setCargoWeight(String cargoWeight) {
        this.cargoWeight = cargoWeight;
    }

    public String getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(String tareWeight) {
        this.tareWeight = tareWeight;
    }

    public String getDunnageWeight() {
        return dunnageWeight;
    }

    public void setDunnageWeight(String dunnageWeight) {
        this.dunnageWeight = dunnageWeight;
    }

    public String getWeightUOM() {
        return weightUOM;
    }

    public void setWeightUOM(String weightUOM) {
        this.weightUOM = weightUOM;
    }

    public String getVerifierId() {
        return verifierId;
    }

    public void setVerifierId(String verifierId) {
        this.verifierId = verifierId;
    }

}
