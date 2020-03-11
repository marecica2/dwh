package org.bmsource.dwh.importer.importer.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bmsource.dwh.common.courier.RawFact;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class MappingResponseTest {

    private List<String> columns = Arrays.asList("Sup. name", "Business unit", "Unmapped field", "Origin-City",
        "Origin-State",
        "Origin-Country", "Origin-Zip code", "Destination-City", "Destination-State", "Destination-Country",
        "Destination-Zip code", "Zone", "Date of Shipment (MM/DD/YYYY)", "Date of delivery (MM/DD/YYYY)",
        "Service Type", "Billable Weight (lb)", "Actual Weight (lb)", "Dimensional Weight (lb)",
        "Length of package (inches)", "Width of Package (inches)", "Height of Package (inches)",
        "Cost (USD)", "Cost-Local Currency", "Currency conversion",
        "Accessorial service 1", "Accessorial service 2", "Accessorial service 3", "Accessorial Charge 1 (USD)",
        "Accessorial Charge 2 (USD)", "Accessorial Charge 3 (USD)", "Total Accessorial charge (USD)", "Discount (%)");

    private List<Object> previewValues = Arrays.asList(new String[]{"UPS", "BU1"});

    private MappingResponse mappingResponse = MappingResponse
        .builder()
        .setSourceFields(columns, previewValues)
        .setFactModel(RawFact.class)
        .autoSuggestionMapping()
        .build();

    @Test
    public void testFactFields() {
        Map<String, Object> cost = mappingResponse.getFactFields().get("cost");
        assertThat(cost).isNotNull();
        assertThat(cost.get("required")).isEqualTo(true);
        assertThat(cost.get("type")).isEqualTo("BigDecimal");
    }

    @Test
    public void testSourceFields() {
        Object preview = mappingResponse.getInputFields().get("Sup. name");
        assertThat(preview).isNotNull();
        assertThat(preview).isEqualTo("UPS");
    }

    @Test
    public void testAutoSuggestion() {
        Map<String, String> mapping = mappingResponse.getMapping();
        assertThat(mapping.get("Sup. name")).isEqualTo("supplierName");
        assertThat(mapping.get("Business unit")).isEqualTo("businessUnit");
        assertThat(mapping.get("Unmapped field")).isEqualTo(null);
    }
}
