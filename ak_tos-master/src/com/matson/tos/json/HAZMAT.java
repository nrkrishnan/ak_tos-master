package com.matson.tos.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HAZMAT {
    @Override
    public String toString() {
        String _toString= "HAZMAT{" +
                "associationId=" + associationId +
                ", bookingNumber='" + bookingNumber + '\'' +
                ", containerNumber='" + containerNumber + '\'' +
                ", alfrescoDocId=" + alfrescoDocId +
                ", isActive='" + isActive + '\'' +
                ", createUser='" + createUser + '\'' +
                ", createDate='" + createDate + '\'' +
                ", lastUpdateUser='" + lastUpdateUser + '\'' +
                ", lastUpdateDate='" + lastUpdateDate + '\'' +
                ", unitID='" + unitID + '\'' +
                ", hazardousCommodityLines=[";
        for (HazardousCommodityLine commodityLine : getHazardousCommodityLines()) {
            _toString += commodityLine.toString();
        }
        return _toString + "]}";
    }

    private Integer associationId;
    private String bookingNumber;
    private String containerNumber;
    private Object alfrescoDocId;
    private String isActive;
    private String createUser;
    private String createDate;
    private String lastUpdateUser;
    private String lastUpdateDate;
    private String unitID;
    private List<HazardousCommodityLine> hazardousCommodityLines = new ArrayList<HazardousCommodityLine>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The associationId
     */
    public Integer getAssociationId() {
        return associationId;
    }

    /**
     *
     * @param associationId
     * The associationId
     */
    public void setAssociationId(Integer associationId) {
        this.associationId = associationId;
    }

    /**
     *
     * @return
     * The bookingNumber
     */
    public String getBookingNumber() {
        return bookingNumber;
    }

    /**
     *
     * @param bookingNumber
     * The bookingNumber
     */
    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    /**
     *
     * @return
     * The containerNumber
     */
    public String getContainerNumber() {
        return containerNumber;
    }

    /**
     *
     * @param containerNumber
     * The containerNumber
     */
    public void setContainerNumber(String containerNumber) {
        this.containerNumber = containerNumber;
    }

    /**
     *
     * @return
     * The alfrescoDocId
     */
    public Object getAlfrescoDocId() {
        return alfrescoDocId;
    }

    /**
     *
     * @param alfrescoDocId
     * The alfrescoDocId
     */
    public void setAlfrescoDocId(Object alfrescoDocId) {
        this.alfrescoDocId = alfrescoDocId;
    }

    /**
     *
     * @return
     * The isActive
     */
    public String getIsActive() {
        return isActive;
    }

    /**
     *
     * @param isActive
     * The isActive
     */
    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    /**
     *
     * @return
     * The createUser
     */
    public String getCreateUser() {
        return createUser;
    }

    /**
     *
     * @param createUser
     * The createUser
     */
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    /**
     *
     * @return
     * The createDate
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     *
     * @param createDate
     * The createDate
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    /**
     *
     * @return
     * The lastUpdateUser
     */
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    /**
     *
     * @param lastUpdateUser
     * The lastUpdateUser
     */
    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    /**
     *
     * @return
     * The lastUpdateDate
     */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     *
     * @param lastUpdateDate
     * The lastUpdateDate
     */
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getUnitID() {
        return unitID;
    }

    public void setUnitID(String unitID) {
        this.unitID = unitID;
    }

    /**
     *
     * @return
     * The hazardousCommodityLines
     */
    public List<HazardousCommodityLine> getHazardousCommodityLines() {
        return hazardousCommodityLines;
    }

    /**
     *
     * @param hazardousCommodityLines
     * The hazardousCommodityLines
     */
    public void setHazardousCommodityLines(List<HazardousCommodityLine> hazardousCommodityLines) {
        this.hazardousCommodityLines = hazardousCommodityLines;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }



}
