package org.bmsource.dwh.migrator;

import org.bmsource.dwh.common.portal.Tenant;
import org.bmsource.dwh.common.portal.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MigratorController {

    @Autowired
    Migrator migrator;

    @Autowired
    TenantRepository repository;

    @PostMapping("/migrate")
    public String migrate() {
        migrator.migrate();
        return "ok";
    }

    @PostMapping("/migrate/tenant/{id}")
    public String migrateTenant(@PathVariable("id") String tenantId) {
        Optional<Tenant> tenant = repository.findById(tenantId);
        migrator.createNewTenant(tenant.get());
        return "ok";
    }
}
