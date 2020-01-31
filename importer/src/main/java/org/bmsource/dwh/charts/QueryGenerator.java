package org.bmsource.dwh.charts;

import org.bmsource.dwh.common.utils.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

public class QueryGenerator {
    private static final String SUM = "sum_";
    private static final String COUNT = "count_";
    private static final String AVG = "avg_";
    private static final String ORDER_ASC = "asc_";
    private static final String ORDER_DESC = "desc_";

    private static final List<String> measurePrefixes = new ArrayList<>();

    static {
        measurePrefixes.add(COUNT);
        measurePrefixes.add(AVG);
        measurePrefixes.add(SUM);
    }

    private static final List<String> orderPrefixes = new ArrayList<>();

    static {
        orderPrefixes.add(ORDER_ASC);
        orderPrefixes.add(ORDER_DESC);
    }

    @Autowired
    DataSource dataSource;

    private String rootTable;

    private Function<String, String> caseConversion;

    private Function<String, String> caseBackConversion;

    private QueryGenerator(Builder builder) {
        dataSource = builder.dataSource;
        rootTable =  builder.caseConversion.apply(builder.rootTable);
        caseConversion = builder.caseConversion;
        caseBackConversion = builder.caseBackConversion;
    }

    public static IDataSource builder() {
        return new Builder();
    }

    private static String snakeCaseConversion(String s) {
        return StringUtils.camelToSnake(s);
    }

    private static String snakeCaseToCamelCase(String s) {
        return StringUtils.snakeToCamel(s);
    }

    private static String noCaseConversion(String s) {
        return s;
    }

    public String queryDistinctValues(String dimensionParam) {
        String dimension = caseConversion(dimensionParam);
        try (DSLContext create = initDSLContext()) {
            Select<?> q = create
                .selectDistinct(field(dimension))
                .from(table(rootTable))
                .orderBy(field(dimension))
                .limit(1000);
            return q.getSQL();
        }
    }

    public String queryAggregate(List<String> measures,
                                 List<String> dimensions,
                                 List<String> ordering,
                                 Map<String, ?> filters) {
        try (DSLContext create = initDSLContext()) {
            Select<?> q = generateAggregateQuery(measures, dimensions, ordering, filters);
            return create.renderNamedParams(q);
        }
    }

    public List<Map<String, Object>> convertCase(List<Map<String, Object>> result) {
        return result
            .stream()
            .map(i -> i.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                    e -> caseBackConversion.apply(e.getKey()),
                    e -> e.getValue())))
            .collect(Collectors.toList());
    }

    private Select<?> generateAggregateQuery(List<String> measuresParam,
                                             List<String> dimensionsParam,
                                             List<String> orderingParam,
                                             Map<String, ?> filtersParam) {
        List<String> measures = caseConversion(measuresParam);
        List<String> dimensions = caseConversion(dimensionsParam);
        List<String> ordering = caseConversion(orderingParam);

        try (DSLContext ctx = initDSLContext()) {
            List<Field<?>> dimensionsFields = dimensions
                .stream()
                .map(DSL::field).collect(Collectors.toList());

            List<Field<?>> aggregateMeasureFields = getAggregateMeasureFields(measures);
            List<Field<?>> simpleMeasureFields = getSimpleMeasureFields(measures);
            List<OrderField<?>> orderFields = getOrderFields(ordering);

            List<Field<?>> selectFields = new ArrayList<>();
            selectFields.addAll(dimensionsFields);
            selectFields.addAll(aggregateMeasureFields);
            selectFields.addAll(simpleMeasureFields);

            List<Condition> conditions = getFilters(filtersParam);

            return ctx.select(selectFields)
                .from(table(rootTable))
                .where(conditions)
                .groupBy(dimensionsFields)
                .orderBy(orderFields);
        }
    }

    private List<Condition> getFilters(Map<String, ?> filters) {
        return filters
            .entrySet()
            .stream()
            .map(e -> {
                Condition c = null;
                if (e.getValue() instanceof Collection) {
                    c = fieldWithCase(e.getKey())
                        .in(field(param(e.getKey(), "")));
                } else {
                    c = fieldWithCase(e.getKey())
                        .equal(param(e.getKey(), e.getValue()));
                }
                return c;
            })
            .collect(Collectors.toList());
    }

    private Field<Object> fieldWithCase(String field) {
        return field(caseConversion(field));
    }

    private List<Field<?>> getSimpleMeasureFields(List<String> measures) {
        return measures
            .stream()
            .filter(item ->
                !org.apache.commons.lang3.StringUtils.startsWithAny(item,
                    measurePrefixes.toArray(new String[]{})))
            .map(item -> field(item, Object.class))
            .collect(Collectors.toList());
    }

    private List<Field<?>> getAggregateMeasureFields(List<String> measures) {
        return measures
            .stream()
            .map(dim -> StringUtils.splitByPrefix(measurePrefixes, dim))
            .filter(Objects::nonNull)
            .map(item -> {
                Field<?> measure = null;
                switch (item[0]) {
                    case SUM:
                        measure =
                            sum(field(item[1], BigDecimal.class)).as(SUM + item[1]);
                        break;
                    case AVG:
                        measure =
                            avg(field(item[1], BigDecimal.class)).as(AVG + item[1]);
                        break;
                    case COUNT:
                        measure =
                            count(field(item[1])).as(item[1]).as(COUNT + item[1]);
                        break;
                    default:
                        break;
                }
                return measure;
            })
            .collect(Collectors.toList());
    }

    private List<OrderField<?>> getOrderFields(List<String> orderFields) {
        return orderFields
            .stream()
            .map(sort -> StringUtils.splitByPrefix(orderPrefixes, sort))
            .filter(Objects::nonNull)
            .map(item -> {
                OrderField<?> measure = null;
                switch (item[0]) {
                    case ORDER_ASC:
                        measure = field(item[1]).asc();
                        break;
                    case ORDER_DESC:
                        measure = field(item[1]).desc();
                        break;
                    default:
                        break;
                }
                return measure;
            })
            .collect(Collectors.toList());
    }

    private DSLContext initDSLContext() {
        return using(dataSource, SQLDialect.POSTGRES);
    }

    private String caseConversion(String s) {
        return caseConversion.apply(s);
    }

    private List<String> caseConversion(List<String> list) {
        return list
            .stream()
            .map(this::caseConversion)
            .collect(Collectors.toList());
    }

    interface IBuild {
        QueryGenerator build();
    }

    interface IRootTable {
        ICaseConversion withRootTable(String val);
    }

    interface ICaseConversion {
        IBuild usingSnakeCaseConversion();
        IBuild usingNoCaseConversion();
    }

    interface IDataSource {
        IRootTable withDataSource(DataSource val);
    }

    public static final class Builder implements IRootTable, IDataSource, ICaseConversion, IBuild {
        private String rootTable;
        private DataSource dataSource;
        private Function<String, String> caseConversion;
        private Function<String, String> caseBackConversion;

        private Builder() {
        }

        @Override
        public ICaseConversion withRootTable(String val) {
            rootTable = val;
            return this;
        }

        @Override
        public IRootTable withDataSource(DataSource val) {
            dataSource = val;
            return this;
        }

        @Override
        public IBuild usingSnakeCaseConversion() {
            caseConversion = QueryGenerator::snakeCaseConversion;
            caseBackConversion = QueryGenerator::snakeCaseToCamelCase;
            return this;
        }

        @Override
        public IBuild usingNoCaseConversion() {
            caseConversion = QueryGenerator::noCaseConversion;
            return this;
        }

        public QueryGenerator build() {
            return new QueryGenerator(this);
        }
    }
}
