package org.bmsource.dwh.common.excel;

import org.bmsource.dwh.common.excel.reader.ExcelRowValidator;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataRow<Fact> {
    private List<Object> row;
    private ExcelRowValidator.ValidationErrors errors;
    private Fact fact;
    private Map<String, String> columnMapping;
    private Map<String, String> propertyMapping;
    private boolean valid = true;

    public List<Object> getRow() {
        return row;
    }

    public void setRow(List<Object> row) {
        this.row = row;
    }

    public ExcelRowValidator.ValidationErrors getErrors() {
        return errors;
    }

    public void setErrors(ExcelRowValidator.ValidationErrors errors) {
        this.errors = errors;
    }

    public Fact getFact() {
        return fact;
    }

    public void setFact(Fact fact) {
        this.fact = fact;
    }

    public Map<String, String> getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(Map<String, String> columnMapping) {
        this.propertyMapping = columnMapping;
        this.columnMapping = columnMapping.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        ;
    }

    public void validate() {
        for (Field field : fact.getClass().getDeclaredFields()) {
            if (field.getAnnotation(NotNull.class) != null && errors.getFieldErrors().get(field.getName()) != null) {
                valid = false;
            } else if (errors.getFieldErrors().get(field.getName()) != null) {
                try {
                    field.set(fact, null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return "DataRow{" +
            "errors=" + errors +
            ", fact=" + fact +
            ", valid=" + valid +
            '}';
    }
}
