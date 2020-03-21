package org.bmsource.dwh.common.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
public class TenantDao {

    private DataSource dataSource;

    private JdbcTemplate template;

    @Autowired
    private DataSourceProperties properties;

    @PostConstruct
    public void initialize() {
        this.dataSource = createDatasource();
        this.template = new JdbcTemplate(dataSource);
    }

    @PreDestroy
    public void destroy() throws SQLException {
        this.dataSource.getConnection().close();
    }

    public List<Tenant> findAll() {
        try {
            List<Tenant> tenants = template.query("SELECT id, name, created_on, modified_on FROM master.tenants",
                new TenantDao.TenantEntityMapper());
            return tenants;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public Optional<Tenant> findById(String uuid) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        List<Tenant> tenants = template.query("SELECT id, name, created_on, modified_on FROM master.tenants " +
            "WHERE master.tenants.id = ? ", new Object[]{ uuid }, new TenantDao.TenantEntityMapper());
        if (tenants.size() == 1)
            return Optional.of(tenants.get(0));
        return Optional.empty();
    }

    private class TenantEntityMapper implements RowMapper<Tenant> {
        public Tenant mapRow(ResultSet rs, int rowNum) throws SQLException {
            Tenant tenant = new Tenant();
            tenant.setId(rs.getString("id"));
            tenant.setSchemaName(rs.getString("id"));
            return tenant;
        }
    }

    private DataSource createDatasource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
            .driverClassName(properties.getDriverClassName())
            .url(properties.getUrl())
            .username(properties.getUsername())
            .password(properties.getPassword());
        if (properties.getType() != null) {
            dataSourceBuilder.type(properties.getType());
        }
        return dataSourceBuilder.build();
    }
}
