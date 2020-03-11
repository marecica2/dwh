package org.bmsource.dwh.importer.masterdata;

public interface MasterDataService {

    void init();

    String getStandardServiceType(String supplierServiceType);

    String getServiceGroup(String supplierServiceType);
}
