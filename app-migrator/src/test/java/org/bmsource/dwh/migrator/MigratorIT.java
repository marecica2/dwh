package org.bmsource.dwh.migrator;

import org.bmsource.dwh.common.portal.Tenant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ActiveProfiles("integration-test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MigratorConfiguration.class })
@Import(TenantRepositoryImpl.class)
public class MigratorIT {

    @Autowired
    Migrator migrator;

    @Autowired
    JdbcTemplate template;

    @BeforeEach
    @AfterEach
    public void before() {
        template.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", TenantRepositoryImpl.tenant1.getId()));
        template.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", TenantRepositoryImpl.tenant2.getId()));
    }

    @Test
    public void migrateTenants() {
        migrator.migrate();
        List<Map<String, Object>> schemas = querySchemas();
        Optional<Map<String, Object>> schema1 = findSchema(schemas, TenantRepositoryImpl.tenant1);
        Optional<Map<String, Object>> schema2 = findSchema(schemas, TenantRepositoryImpl.tenant2);
        Assertions.assertTrue(schema1.isPresent());
        Assertions.assertTrue(schema2.isPresent());
        Assertions.assertTrue(countTablesInSchema(TenantRepositoryImpl.tenant1) > 0);
    }

    @Test
    public void addTenant() {
        migrator.createNewTenant(TenantRepositoryImpl.tenant3);
        List<Map<String, Object>> schemas = querySchemas();
        Optional<Map<String, Object>> schema3 = findSchema(schemas, TenantRepositoryImpl.tenant3);
        Assertions.assertTrue(schema3.isPresent());
        Assertions.assertTrue(countTablesInSchema(TenantRepositoryImpl.tenant3) > 0);
    }

    private List<Map<String, Object>> querySchemas() {
        String sql = "SELECT schema_name FROM information_schema.schemata;";
        return template.queryForList(sql);
    }

    private Integer countTablesInSchema(Tenant tenant) {
        String sql = "select count(*) " +
            "from information_schema.tables " +
            "where table_schema = '" + tenant.getId() + "';";
        return template.queryForObject(sql, Integer.class);
    }

    private Optional<Map<String, Object>> findSchema(List<Map<String, Object>> schemas, Tenant tenant) {
        return schemas
            .stream()
            .filter(schema -> tenant.getId().equals(schema.get("schema_name")))
            .findFirst();
    }
}
