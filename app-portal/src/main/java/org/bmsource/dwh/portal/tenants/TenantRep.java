package org.bmsource.dwh.portal.tenants;

import org.bmsource.dwh.common.portal.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRep extends JpaRepository<Tenant, String> {
}
