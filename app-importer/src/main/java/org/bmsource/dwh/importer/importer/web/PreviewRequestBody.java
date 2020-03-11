package org.bmsource.dwh.importer.importer.web;

import java.util.Map;

class PreviewRequestBody {

    private Map<String, String> mapping;

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }
}
