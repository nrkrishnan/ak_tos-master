package com.matson.tos.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HazardousCommodityLine {
    private Integer commodityLineId;
    private String hazType;
    private String hazNumber;
    private String hazCommodityName;
    private String hazPrimaryClass;
    private String hazPrimaryClassName;
    private String hazSecondaryClass;
    private String hazTertiaryClass;
    private String hazEmergencyContactName;
    private String hazEmergencyContactPhone;
    private String hazSecondaryEmergencyContactName;
    private String hazSecondaryEmergencyContactPhone;
    private Integer hazPackageGroup;
    private Integer hazPieces;
    private String hazPiecesUomCode;
    private Double hazWeight;
    private String hazWeightUomCode;
    private String hazFlashPoint;
    private String hazFlashPointUomCode;
    private String hazImdgCfrIndicator;
    private String hazLimitedQuantity;
    private String hazMarinePollutant;
    private Object hazExplosivePowderWeight;
    private String hazExplosivePowderWeightUomCode;
    private Object hazSpecialPermitNumber;
    private String isActive;
    private String createUser;
    private String createDate;
    private String lastUpdateUser;
    private String lastUpdateDate;
    private String notes;
    private Object stowageRestriction;
    private Object explosivePowderWeightApplicable;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @Override
    public String toString() {
        return "HazardousCommodityLine{" +
                "commodityLineId=" + commodityLineId +
                ", hazType='" + hazType + '\'' +
                ", hazNumber='" + hazNumber + '\'' +
                ", hazCommodityName='" + hazCommodityName + '\'' +
                ", hazPrimaryClass='" + hazPrimaryClass + '\'' +
                ", hazPrimaryClassName='" + hazPrimaryClassName + '\'' +
                ", hazSecondaryClass='" + hazSecondaryClass + '\'' +
                ", hazTertiaryClass='" + hazTertiaryClass + '\'' +
                ", hazEmergencyContactName='" + hazEmergencyContactName + '\'' +
                ", hazEmergencyContactPhone='" + hazEmergencyContactPhone + '\'' +
                ", hazSecondaryEmergencyContactName='" + hazSecondaryEmergencyContactName + '\'' +
                ", hazSecondaryEmergencyContactPhone='" + hazSecondaryEmergencyContactPhone + '\'' +
                ", hazPackageGroup=" + hazPackageGroup +
                ", hazPieces=" + hazPieces +
                ", hazPiecesUomCode='" + hazPiecesUomCode + '\'' +
                ", hazWeight=" + hazWeight +
                ", hazWeightUomCode='" + hazWeightUomCode + '\'' +
                ", hazFlashPoint='" + hazFlashPoint + '\'' +
                ", hazFlashPointUomCode='" + hazFlashPointUomCode + '\'' +
                ", hazImdgCfrIndicator='" + hazImdgCfrIndicator + '\'' +
                ", hazLimitedQuantity='" + hazLimitedQuantity + '\'' +
                ", hazMarinePollutant='" + hazMarinePollutant + '\'' +
                ", hazExplosivePowderWeight=" + hazExplosivePowderWeight +
                ", hazExplosivePowderWeightUomCode='" + hazExplosivePowderWeightUomCode + '\'' +
                ", hazSpecialPermitNumber=" + hazSpecialPermitNumber +
                ", isActive='" + isActive + '\'' +
                ", createUser='" + createUser + '\'' +
                ", createDate='" + createDate + '\'' +
                ", lastUpdateUser='" + lastUpdateUser + '\'' +
                ", lastUpdateDate='" + lastUpdateDate + '\'' +
                ", notes='" + notes + '\'' +
                ", stowageRestriction=" + stowageRestriction +
                ", explosivePowderWeightApplicable=" + explosivePowderWeightApplicable +
                '}';
    }

    /**
     * @return The commodityLineId
     */
    public Integer getCommodityLineId() {
        return commodityLineId;
    }

    /**
     * @param commodityLineId The commodityLineId
     */
    public void setCommodityLineId(Integer commodityLineId) {
        this.commodityLineId = commodityLineId;
    }

    /**
     * @return The hazType
     */
    public String getHazType() {
        return hazType;
    }

    /**
     * @param hazType The hazType
     */
    public void setHazType(String hazType) {
        this.hazType = hazType;
    }

    /**
     * @return The hazNumber
     */
    public String getHazNumber() {
        return hazNumber;
    }

    /**
     * @param hazNumber The hazNumber
     */
    public void setHazNumber(String hazNumber) {
        this.hazNumber = hazNumber;
    }

    /**
     * @return The hazCommodityName
     */
    public String getHazCommodityName() {
        return hazCommodityName;
    }

    /**
     * @param hazCommodityName The hazCommodityName
     */
    public void setHazCommodityName(String hazCommodityName) {
        this.hazCommodityName = hazCommodityName;
    }

    /**
     * @return The hazPrimaryClass
     */
    public String getHazPrimaryClass() {
        return hazPrimaryClass;
    }

    /**
     * @param hazPrimaryClass The hazPrimaryClass
     */
    public void setHazPrimaryClass(String hazPrimaryClass) {
        this.hazPrimaryClass = hazPrimaryClass;
    }

    /**
     * @return The hazPrimaryClassName
     */
    public String getHazPrimaryClassName() {
        return hazPrimaryClassName;
    }

    /**
     * @param hazPrimaryClassName The hazPrimaryClassName
     */
    public void setHazPrimaryClassName(String hazPrimaryClassName) {
        this.hazPrimaryClassName = hazPrimaryClassName;
    }

    /**
     * @return The hazSecondaryClass
     */
    public String getHazSecondaryClass() {
        return hazSecondaryClass;
    }

    /**
     * @param hazSecondaryClass The hazSecondaryClass
     */
    public void setHazSecondaryClass(String hazSecondaryClass) {
        this.hazSecondaryClass = hazSecondaryClass;
    }

    /**
     * @return The hazTertiaryClass
     */
    public String getHazTertiaryClass() {
        return hazTertiaryClass;
    }

    /**
     * @param hazTertiaryClass The hazTertiaryClass
     */
    public void setHazTertiaryClass(String hazTertiaryClass) {
        this.hazTertiaryClass = hazTertiaryClass;
    }

    /**
     * @return The hazEmergencyContactName
     */
    public String getHazEmergencyContactName() {
        return hazEmergencyContactName;
    }

    /**
     * @param hazEmergencyContactName The hazEmergencyContactName
     */
    public void setHazEmergencyContactName(String hazEmergencyContactName) {
        this.hazEmergencyContactName = hazEmergencyContactName;
    }

    /**
     * @return The hazEmergencyContactPhone
     */
    public String getHazEmergencyContactPhone() {
        return hazEmergencyContactPhone;
    }

    /**
     * @param hazEmergencyContactPhone The hazEmergencyContactPhone
     */
    public void setHazEmergencyContactPhone(String hazEmergencyContactPhone) {
        this.hazEmergencyContactPhone = hazEmergencyContactPhone;
    }

    /**
     * @return The hazSecondaryEmergencyContactName
     */
    public String getHazSecondaryEmergencyContactName() {
        return hazSecondaryEmergencyContactName;
    }

    /**
     * @param hazSecondaryEmergencyContactName The hazSecondaryEmergencyContactName
     */
    public void setHazSecondaryEmergencyContactName(String hazSecondaryEmergencyContactName) {
        this.hazSecondaryEmergencyContactName = hazSecondaryEmergencyContactName;
    }

    /**
     * @return The hazSecondaryEmergencyContactPhone
     */
    public String getHazSecondaryEmergencyContactPhone() {
        return hazSecondaryEmergencyContactPhone;
    }

    /**
     * @param hazSecondaryEmergencyContactPhone The hazSecondaryEmergencyContactPhone
     */
    public void setHazSecondaryEmergencyContactPhone(String hazSecondaryEmergencyContactPhone) {
        this.hazSecondaryEmergencyContactPhone = hazSecondaryEmergencyContactPhone;
    }

    /**
     * @return The hazPackageGroup
     */
    public Integer getHazPackageGroup() {
        return hazPackageGroup;
    }

    /**
     * @param hazPackageGroup The hazPackageGroup
     */
    public void setHazPackageGroup(Integer hazPackageGroup) {
        this.hazPackageGroup = hazPackageGroup;
    }

    /**
     * @return The hazPieces
     */
    public Integer getHazPieces() {
        return hazPieces;
    }

    /**
     * @param hazPieces The hazPieces
     */
    public void setHazPieces(Integer hazPieces) {
        this.hazPieces = hazPieces;
    }

    /**
     * @return The hazPiecesUomCode
     */
    public String getHazPiecesUomCode() {
        return hazPiecesUomCode;
    }

    /**
     * @param hazPiecesUomCode The hazPiecesUomCode
     */
    public void setHazPiecesUomCode(String hazPiecesUomCode) {
        this.hazPiecesUomCode = hazPiecesUomCode;
    }

    /**
     * @return The hazWeight
     */
    public Double getHazWeight() {
        return hazWeight;
    }

    /**
     * @param hazWeight The hazWeight
     */
    public void setHazWeight(Double hazWeight) {
        this.hazWeight = hazWeight;
    }

    /**
     * @return The hazWeightUomCode
     */
    public String getHazWeightUomCode() {
        return hazWeightUomCode;
    }

    /**
     * @param hazWeightUomCode The hazWeightUomCode
     */
    public void setHazWeightUomCode(String hazWeightUomCode) {
        this.hazWeightUomCode = hazWeightUomCode;
    }

    /**
     * @return The hazFlashPoint
     */
    public String getHazFlashPoint() {
        return hazFlashPoint;
    }

    /**
     * @param hazFlashPoint The hazFlashPoint
     */
    public void setHazFlashPoint(String hazFlashPoint) {
        this.hazFlashPoint = hazFlashPoint;
    }

    /**
     * @return The hazFlashPointUomCode
     */
    public String getHazFlashPointUomCode() {
        return hazFlashPointUomCode;
    }

    /**
     * @param hazFlashPointUomCode The hazFlashPointUomCode
     */
    public void setHazFlashPointUomCode(String hazFlashPointUomCode) {
        this.hazFlashPointUomCode = hazFlashPointUomCode;
    }

    /**
     * @return The hazImdgCfrIndicator
     */
    public String getHazImdgCfrIndicator() {
        return hazImdgCfrIndicator;
    }

    /**
     * @param hazImdgCfrIndicator The hazImdgCfrIndicator
     */
    public void setHazImdgCfrIndicator(String hazImdgCfrIndicator) {
        this.hazImdgCfrIndicator = hazImdgCfrIndicator;
    }

    /**
     * @return The hazLimitedQuantity
     */
    public String getHazLimitedQuantity() {
        return hazLimitedQuantity;
    }

    /**
     * @param hazLimitedQuantity The hazLimitedQuantity
     */
    public void setHazLimitedQuantity(String hazLimitedQuantity) {
        this.hazLimitedQuantity = hazLimitedQuantity;
    }

    /**
     * @return The hazMarinePollutant
     */
    public String getHazMarinePollutant() {
        return hazMarinePollutant;
    }

    /**
     * @param hazMarinePollutant The hazMarinePollutant
     */
    public void setHazMarinePollutant(String hazMarinePollutant) {
        this.hazMarinePollutant = hazMarinePollutant;
    }

    /**
     * @return The hazExplosivePowderWeight
     */
    public Object getHazExplosivePowderWeight() {
        return hazExplosivePowderWeight;
    }

    /**
     * @param hazExplosivePowderWeight The hazExplosivePowderWeight
     */
    public void setHazExplosivePowderWeight(Object hazExplosivePowderWeight) {
        this.hazExplosivePowderWeight = hazExplosivePowderWeight;
    }

    /**
     * @return The hazExplosivePowderWeightUomCode
     */
    public String getHazExplosivePowderWeightUomCode() {
        return hazExplosivePowderWeightUomCode;
    }

    /**
     * @param hazExplosivePowderWeightUomCode The hazExplosivePowderWeightUomCode
     */
    public void setHazExplosivePowderWeightUomCode(String hazExplosivePowderWeightUomCode) {
        this.hazExplosivePowderWeightUomCode = hazExplosivePowderWeightUomCode;
    }

    /**
     * @return The hazSpecialPermitNumber
     */
    public Object getHazSpecialPermitNumber() {
        return hazSpecialPermitNumber;
    }

    /**
     * @param hazSpecialPermitNumber The hazSpecialPermitNumber
     */
    public void setHazSpecialPermitNumber(Object hazSpecialPermitNumber) {
        this.hazSpecialPermitNumber = hazSpecialPermitNumber;
    }

    /**
     * @return The isActive
     */
    public String getIsActive() {
        return isActive;
    }

    /**
     * @param isActive The isActive
     */
    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    /**
     * @return The createUser
     */
    public String getCreateUser() {
        return createUser;
    }

    /**
     * @param createUser The createUser
     */
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    /**
     * @return The createDate
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     * @param createDate The createDate
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    /**
     * @return The lastUpdateUser
     */
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    /**
     * @param lastUpdateUser The lastUpdateUser
     */
    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    /**
     * @return The lastUpdateDate
     */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate The lastUpdateDate
     */
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return The notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes The notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return The stowageRestriction
     */
    public Object getStowageRestriction() {
        return stowageRestriction;
    }

    /**
     * @param stowageRestriction The stowageRestriction
     */
    public void setStowageRestriction(Object stowageRestriction) {
        this.stowageRestriction = stowageRestriction;
    }

    /**
     * @return The explosivePowderWeightApplicable
     */
    public Object getExplosivePowderWeightApplicable() {
        return explosivePowderWeightApplicable;
    }

    /**
     * @param explosivePowderWeightApplicable The explosivePowderWeightApplicable
     */
    public void setExplosivePowderWeightApplicable(Object explosivePowderWeightApplicable) {
        this.explosivePowderWeightApplicable = explosivePowderWeightApplicable;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
