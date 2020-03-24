package org.bmsource.dwh.common.excel.reader;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelRowValidator<Bean> {

    private final Validator validator;
    private final Map<String, String> mapping;

    public ExcelRowValidator(Map<String, String> columnPropertyMapping) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.mapping = inverseMapping(columnPropertyMapping);
    }

    public ValidationErrors getValidationErrors(Bean bean) {
        Map<String, List<String>> columnErrors = new HashMap<>();
        Map<String, List<String>> fieldErrors = new HashMap<>();

        Set<ConstraintViolation<Object>> validationErrors = validator.validate(bean);
        if (validationErrors != null && validationErrors.size() > 0) {
            for (ConstraintViolation<Object> error : validationErrors) {
                String beanPropertyName = error.getPropertyPath().toString();
                String columnName = getErrorKey(beanPropertyName);
                List<String> errorMessages = Collections.singletonList(error.getMessage());

                if (columnErrors.get(columnName) == null) {
                    columnErrors.put(columnName, errorMessages);
                } else {
                    columnErrors.get(columnName).addAll(errorMessages);
                }

                if (fieldErrors.get(beanPropertyName) == null) {
                    fieldErrors.put(beanPropertyName, errorMessages);
                } else {
                    fieldErrors.get(beanPropertyName).addAll(errorMessages);
                }
            }
        }
        return new ValidationErrors(columnErrors, fieldErrors);
    }

    public static class ValidationErrors {
        Map<String, List<String>> columnErrors;
        Map<String, List<String>> fieldErrors;

        public ValidationErrors(Map<String, List<String>> columnErrors, Map<String, List<String>> fieldErrors) {
            this.columnErrors = columnErrors;
            this.fieldErrors = fieldErrors;
        }

        public Map<String, List<String>> getColumnErrors() {
            return columnErrors;
        }

        public Map<String, List<String>> getFieldErrors() {
            return fieldErrors;
        }
    }

    private String getErrorKey(String beanPropertyName) {
        return mapping.get(beanPropertyName);
    }

    private Map<String, String> inverseMapping(Map<String, String> columnPropertyMapping) {
        return columnPropertyMapping.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
}
