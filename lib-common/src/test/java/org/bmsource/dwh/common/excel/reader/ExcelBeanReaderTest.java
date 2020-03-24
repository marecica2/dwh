package org.bmsource.dwh.common.excel.reader;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExcelBeanReaderTest {

    @Test
    public void testRowsReading() throws IOException {
        InputStream stream = new ClassPathResource("/spends.xlsx").getInputStream();
        ExcelBeanReader<Bean> reader = new ExcelBeanReader<>(stream, Bean.class, mapping);
        List<String> header = reader.getHeader();

        int rowsCount = 1;
        while (reader.hasNextRow()) {
            rowsCount++;
            reader.nextRow();
        }
        reader.close();
        assertEquals(38, header.size());
        assertEquals("S. No.", header.get(0));
        assertEquals(rowsCount, reader.getTotalRowsCount());
        assertEquals(rowsCount, reader.getReadRowsCount());
    }

    Map<String, String> mapping = new HashMap<String, String>() {{
        put("S. No.", "transactionId");
        put("Supplier Name", "supplierName");
        put("Business Unit", "businessUnit");
        put("Origin-City", "originCity");
        put("Origin-State", "originState");
        put("Zone", "zone");
        put("Date of Shipment (MM/DD/YYYY)", "shipmentDate");
        put("Date of delivery (MM/DD/YYYY)", "deliveryDate");
        put("Service Type", "serviceType");
        put("Billable Weight (lb)", "billableWeight");
        put("Cost (USD)", "cost");
        put("Accessorial service 1", "accessorialService1");
        put("Accessorial Charge 1 (USD)", "accessorialCharge1");
        put("Discount (%)", "discount");
        put("Distance in Miles", "distance");
    }};

    public static class Bean {
        @NotNull
        String transactionId;
        @NotNull
        String supplierName;
        String businessUnit;
        String originCity;
        String originState;
        @NotNull
        String zone;
        @NotNull
        Date shipmentDate;
        @NotNull
        Date deliveryDate;
        @NotNull
        String serviceType;
        @NotNull
        Double billableWeight;
        @NotNull
        @Min(0)
        BigDecimal cost;
        String accessorialService1;
        @Min(0)
        BigDecimal accessorialCharge1;
        @Max(1)
        @Min(0)
        Double discount;
        @Min(0)
        Double distance;

        @Override
        public String toString() {
            return "Bean{" +
                "transactionId='" + transactionId + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", businessUnit='" + businessUnit + '\'' +
                ", originCity='" + originCity + '\'' +
                ", originState='" + originState + '\'' +
                ", zone='" + zone + '\'' +
                ", shipmentDate=" + shipmentDate +
                ", deliveryDate=" + deliveryDate +
                ", serviceType='" + serviceType + '\'' +
                ", billableWeight=" + billableWeight +
                ", cost=" + cost +
                ", accessorialService1='" + accessorialService1 + '\'' +
                ", accessorialCharge1=" + accessorialCharge1 +
                ", discount=" + discount +
                ", distance=" + distance +
                '}';
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

        public BigDecimal getAccessorialCharge1() {
            return accessorialCharge1;
        }

        public void setAccessorialCharge1(BigDecimal accessorialCharge1) {
            this.accessorialCharge1 = accessorialCharge1;
        }

        public Double getDiscount() {
            return discount;
        }

        public void setDiscount(Double discount) {
            this.discount = discount;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }
    }

    List<String> header = Arrays.asList(
        "S. No.",
        "Supplier Name",
        "Business Unit",
        "Origin-City",
        "Origin-State",
        "Origin-Country",
        "Origin-Zipcode",
        "Destination-City",
        "Destination-State",
        "Destination-Country",
        "Destination-Zip code",
        "Zone",
        "Date of Shipment (MM/DD/YYYY)",
        "Date of delivery (MM/DD/YYYY)",
        "Service Type",
        "Billable Weight (lb)",
        "Actual Weight (lb)",
        "Dimensional Weight (lb)",
        "Length of package (inches)",
        "Width of Package (inches)",
        "Height of Package (inches)",
        "Cost (USD)",
        "Cost-Local Currency",
        "Currency conversion",
        "Insurance Cost (USD)",
        "Surcharge (USD)",
        "Accessorial service 1",
        "Accessorial service 2",
        "Accessorial service 3",
        "Accessorial Charge 1 (USD)",
        "Accessorial Charge 2 (USD)",
        "Accessorial Charge 3 (USD)",
        "Total Accessorial charge (USD)",
        "Discount (%)",
        "Total transit time (in days)",
        "Agreed transit time (in days)",
        "Distance in Miles", "Source");
}
