package org.bmsource.dwh.common.db;

import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class CustomPostgreSQLDialect extends PostgreSQL95Dialect {

    public CustomPostgreSQLDialect() {
        super();
        registerHibernateType(Types.OTHER, StringArrayType.class.getName());
        registerHibernateType(Types.OTHER, IntArrayType.class.getName());
        registerHibernateType(Types.OTHER, JsonStringType.class.getName());
        registerHibernateType(Types.OTHER, JsonBinaryType.class.getName());
        registerHibernateType(Types.OTHER, JsonNodeBinaryType.class.getName());
        registerHibernateType(Types.OTHER, JsonNodeStringType.class.getName());
    }
}
