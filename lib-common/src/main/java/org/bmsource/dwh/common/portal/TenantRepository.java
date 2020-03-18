package org.bmsource.dwh.common.portal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "tenants", path = "tenants")
public interface TenantRepository extends JpaRepository<Tenant, String> {

    List<Tenant> findAll();

}
