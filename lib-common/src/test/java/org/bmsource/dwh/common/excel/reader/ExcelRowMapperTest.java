package org.bmsource.dwh.common.excel.reader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

public class ExcelRowMapperTest {

    List<String> headerColumn = new ArrayList<String>() {{
        add("integerColumn");
        add("doubleColumn");
        add("stringColumn");
        add("bigDecimalColumn");
    }};

    Map<String, String> mapping = new HashMap<String, String>() {{
        put("integerColumn", "integerProp");
        put("doubleColumn", "doubleProp");
        put("stringColumn", "stringProp");
        put("bigDecimalColumn", "bigDecimalProp");
    }};

    @Test
    public void testBeanMappingForTypes() {

        List<Object> row = new ArrayList<Object>() {{
            add(123);
            add(0.1234);
            add("string");
            add(999999.99);
        }};

        ExcelRowMapper<Bean> mapper = new ExcelRowMapper<>(Bean.class, headerColumn, mapping);
        Bean bean = mapper.map(row);
        Assertions.assertEquals(123, bean.getIntegerProp());
        Assertions.assertEquals(0.1234, bean.getDoubleProp());
        Assertions.assertEquals("string", bean.getStringProp());
        Assertions.assertEquals(new BigDecimal("999999.99"),bean.getBigDecimalProp());
    }

    public static class Bean {
        private int integerProp;

        private double doubleProp;

        private String stringProp;

        private Date dateProp;

        private BigDecimal bigDecimalProp;

        public int getIntegerProp() {
            return integerProp;
        }

        public void setIntegerProp(int integerProp) {
            this.integerProp = integerProp;
        }

        public double getDoubleProp() {
            return doubleProp;
        }

        public void setDoubleProp(double doubleProp) {
            this.doubleProp = doubleProp;
        }

        public String getStringProp() {
            return stringProp;
        }

        public void setStringProp(String stringProp) {
            this.stringProp = stringProp;
        }

        public Date getDateProp() {
            return dateProp;
        }

        public void setDateProp(Date dateProp) {
            this.dateProp = dateProp;
        }

        public BigDecimal getBigDecimalProp() {
            return bigDecimalProp;
        }

        public void setBigDecimalProp(BigDecimal bigDecimalProp) {
            this.bigDecimalProp = bigDecimalProp;
        }

        @Override
        public String toString() {
            return "Bean{" +
                "integerProp=" + integerProp +
                ", doubleProp=" + doubleProp +
                ", stringProp='" + stringProp + '\'' +
                ", dateProp=" + dateProp +
                ", bigDecimalProp=" + bigDecimalProp +
                '}';
        }
    }
}
