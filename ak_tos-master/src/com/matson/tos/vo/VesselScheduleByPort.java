/**
 * 
 */
package com.matson.tos.vo;

import java.util.Date;


/**
 * @author dthakur
 *
 */
public class VesselScheduleByPort {
	private String serviceCode;
	private String vesselDescription;
	private String vesselCode;
	private String voyageNumber;
	private String directionSequence;
	private String portTypeA;
	private String portTypeB;
	private String portTypeC;
	private String portTypeD;
	private Date arrivalDate;
	private String arrivalDay;
	private String arrivalTime;
	private String arrivalST;
	private Date departDate;
	private String departDay;
	private String departTime;
	private String departST;
	private String nextPort;
	private String portTypes;

	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	public String getVesselDescription() {
		return vesselDescription;
	}
	public void setVesselDescription(String vesselDescription) {
		this.vesselDescription = vesselDescription;
	}
	public String getVesselCode() {
		return vesselCode;
	}
	public void setVesselCode(String vesselCode) {
		this.vesselCode = vesselCode;
	}
	public String getVoyageNumber() {
		return voyageNumber;
	}
	public void setVoyageNumber(String voyageNumber) {
		this.voyageNumber = voyageNumber;
	}
	public String getDirectionSequence() {
		return directionSequence;
	}
	public void setDirectionSequence(String directionSequence) {
		this.directionSequence = directionSequence;
	}
	public String getPortTypeA() {
		return portTypeA;
	}
	public void setPortTypeA(String portTypeA) {
		this.portTypeA = portTypeA;
	}
	public String getPortTypeB() {
		return portTypeB;
	}
	public void setPortTypeB(String portTypeB) {
		this.portTypeB = portTypeB;
	}
	public String getPortTypeC() {
		return portTypeC;
	}
	public void setPortTypeC(String portTypeC) {
		this.portTypeC = portTypeC;
	}
	public String getPortTypeD() {
		return portTypeD;
	}
	public void setPortTypeD(String portTypeD) {
		this.portTypeD = portTypeD;
	}
	public String getArrivalDay() {
		return arrivalDay;
	}
	public void setArrivalDay(String arrivalDay) {
		this.arrivalDay = arrivalDay;
	}
	public String getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public String getArrivalST() {
		return arrivalST;
	}
	public void setArrivalST(String arrivalST) {
		this.arrivalST = arrivalST;
	}
	public String getDepartDay() {
		return departDay;
	}
	public void setDepartDay(String departDay) {
		this.departDay = departDay;
	}
	public String getDepartTime() {
		return departTime;
	}
	public void setDepartTime(String departTime) {
		this.departTime = departTime;
	}
	public String getDepartST() {
		return departST;
	}
	public void setDepartST(String departST) {
		this.departST = departST;
	}
	public String getNextPort() {
		return nextPort;
	}
	public void setNextPort(String nextPort) {
		this.nextPort = nextPort;
	}
	public Date getArrivalDate() {
		return arrivalDate;
	}
	public void setArrivalDate(Date arrivalDate) {
		this.arrivalDate = arrivalDate;
	}
	public Date getDepartDate() {
		return departDate;
	}
	public void setDepartDate(Date departDate) {
		this.departDate = departDate;
	}
	/**
	 * @return the portTypes
	 */
	public String getPortTypes() {
		return portTypes;
	}
	/**
	 * @param portTypes the portTypes to set
	 */
	public void setPortTypes(String portTypes) {
		this.portTypes = portTypes;
	}
	
	
}
