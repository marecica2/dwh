package org.bmsource.dwh.importer.importer.web;

import java.util.Map;

class UploadRequestBody {

    private Map<String, Object> config;

    private Map<String, String> mapping;

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }
}
