package org.bmsource.dwh.importer.importer.web;

import org.bmsource.dwh.common.courier.RawFact;

import java.util.List;
import java.util.Map;

public class PreviewResponse {
    private RawFact entity;
    private boolean valid;
    private Map<String, List<String>> errors;

    public PreviewResponse(RawFact entity, Map<String, List<String>> errors, boolean valid) {
        this.entity = entity;
        this.errors = errors;
        this.valid = valid;
    }

    public RawFact getEntity() {
        return entity;
    }

    public void setEntity(RawFact entity) {
        this.entity = entity;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
