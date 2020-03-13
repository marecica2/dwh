package org.bmsource.dwh.common.courier;

import org.bmsource.dwh.common.BaseFact;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "fact_raw")
public class RawFact extends BaseFact {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private BigDecimal id;

  String transactionId;

  String supplierName;

  String businessUnit;

  String originCity;

  String originState;

  String originCountry;

  String originZip;

  String destinationCity;

  String destinationState;

  String destinationCountry;

  String destinationZip;

  String zone;

  Date shipmentDate;

  Date deliveryDate;

  @NotNull
  String serviceType;

  @NotNull
  Double billableWeight;

  Double actualWeight;

  Double length;

  Double width;

  Double height;

  @NotNull
  BigDecimal cost;

  String accessorialService1;

  String accessorialService2;

  String accessorialService3;

  BigDecimal accessorialCharge1;

  BigDecimal accessorialCharge2;

  BigDecimal accessorialCharge3;

  BigDecimal discount;

  Double distance;

  public BigDecimal getId() {
    return id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getSupplierName() {
    return supplierName;
  }

  public void setSupplierName(String supplierName) {
    this.supplierName = supplierName;
  }

  public String getBusinessUnit() {
    return businessUnit;
  }

  public void setBusinessUnit(String businessUnit) {
    this.businessUnit = businessUnit;
  }

  public String getOriginCity() {
    return originCity;
  }

  public void setOriginCity(String originCity) {
    this.originCity = originCity;
  }

  public String getOriginState() {
    return originState;
  }

  public void setOriginState(String originState) {
    this.originState = originState;
  }

  public String getOriginCountry() {
    return originCountry;
  }

  public void setOriginCountry(String originCountry) {
    this.originCountry = originCountry;
  }

  public String getOriginZip() {
    return originZip;
  }

  public void setOriginZip(String originZip) {
    this.originZip = originZip;
  }

  public String getDestinationCity() {
    return destinationCity;
  }

  public void setDestinationCity(String destinationCity) {
    this.destinationCity = destinationCity;
  }

  public String getDestinationState() {
    return destinationState;
  }

  public void setDestinationState(String destinationState) {
    this.destinationState = destinationState;
  }

  public String getDestinationCountry() {
    return destinationCountry;
  }

  public void setDestinationCountry(String destinationCountry) {
    this.destinationCountry = destinationCountry;
  }

  public String getDestinationZip() {
    return destinationZip;
  }

  public void setDestinationZip(String destinationZip) {
    this.destinationZip = destinationZip;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  public Date getShipmentDate() {
    return shipmentDate;
  }

  public void setShipmentDate(Date shipmentDate) {
    this.shipmentDate = shipmentDate;
  }

  public Date getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(Date deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public Double getBillableWeight() {
    return billableWeight;
  }

  public void setBillableWeight(Double billableWeight) {
    this.billableWeight = billableWeight;
  }

  public Double getActualWeight() {
    return actualWeight;
  }

  public void setActualWeight(Double actualWeight) {
    this.actualWeight = actualWeight;
  }

  public Double getLength() {
    return length;
  }

  public void setLength(Double length) {
    this.length = length;
  }

  public Double getWidth() {
    return width;
  }

  public void setWidth(Double width) {
    this.width = width;
  }

  public Double getHeight() {
    return height;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public String getAccessorialService1() {
    return accessorialService1;
  }

  public void setAccessorialService1(String accessorialService1) {
    this.accessorialService1 = accessorialService1;
  }

  public String getAccessorialService2() {
    return accessorialService2;
  }

  public void setAccessorialService2(String accessorialService2) {
    this.accessorialService2 = accessorialService2;
  }

  public String getAccessorialService3() {
    return accessorialService3;
  }

  public void setAccessorialService3(String accessorialService3) {
    this.accessorialService3 = accessorialService3;
  }

  public BigDecimal getAccessorialCharge1() {
    return accessorialCharge1;
  }

  public void setAccessorialCharge1(BigDecimal accessorialCharge1) {
    this.accessorialCharge1 = accessorialCharge1;
  }

  public BigDecimal getAccessorialCharge2() {
    return accessorialCharge2;
  }

  public void setAccessorialCharge2(BigDecimal accessorialCharge2) {
    this.accessorialCharge2 = accessorialCharge2;
  }

  public BigDecimal getAccessorialCharge3() {
    return accessorialCharge3;
  }

  public void setAccessorialCharge3(BigDecimal accessorialCharge3) {
    this.accessorialCharge3 = accessorialCharge3;
  }

  public BigDecimal getDiscount() {
    return discount;
  }

  public void setDiscount(BigDecimal discount) {
    this.discount = discount;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  @Override
  public String toString() {
    return "Fact{" +
        "transactionId='" + transactionId + '\'' +
        ", supplierName='" + supplierName + '\'' +
        ", businessUnit='" + businessUnit + '\'' +
        ", zone='" + zone + '\'' +
        ", shipmentDate=" + shipmentDate +
        ", serviceType='" + serviceType + '\'' +
        ", billableWeight=" + billableWeight +
        ", discount=" + discount +
        ", distance=" + distance +
        '}';
  }
}
