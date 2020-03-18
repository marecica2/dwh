package org.bmsource.dwh.common.portal;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String schemaName;

    @Column(name = "created_on")
    private Date createdAt;

    @Column(name = "modified_on")
    private Date updatedAt;

    public Tenant() {}

    public Tenant(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
