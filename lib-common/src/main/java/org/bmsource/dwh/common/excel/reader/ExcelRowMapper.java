package org.bmsource.dwh.common.excel.reader;


import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ExcelRowMapper<Bean> {

    private static Logger logger = LoggerFactory.getLogger(ExcelRowMapper.class);

    private static final String[] datePatterns = {
        "yyyy-MM-dd HH:mm:ss",
        "dd.MM.YYYY HH:mm:ss",
        "dd.MM.YYYY",
        "dd/MM/yyyy",
    };

    private final Class<Bean> typeParameterClass;

    private final Map<Integer, String> indexMapping = new LinkedHashMap<>();

    public ExcelRowMapper(Class<Bean> beanClass, List<String> columns, Map<String, String> mapping) {
        this.typeParameterClass = beanClass;
        mapping.forEach((key, value) -> {
            Optional<String> o = columns.stream().filter(obj -> obj.equals(key)).findFirst();
            o.ifPresent(s -> indexMapping.put(columns.indexOf(s), value));
        });
    }

    public Bean map(List<Object> row) {
        if(row == null)
            return null;
        try {
            Bean bean = typeParameterClass.newInstance();
            this.indexMapping.forEach((index, attribute) -> {
                if (row.size() > index) {
                    try {
                        BeanUtils.copyProperty(bean, attribute, row.get(index));
                    } catch (Exception e) {
                        logger.debug("Conversion failed for bean {} attribute {} value {} Error {}",
                            typeParameterClass.getName(),
                            attribute,
                            row.get(index),
                            e.getMessage());
                    }
                }
            });
            return bean;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Bean> mapList(List<List<Object>> rows) {
        return rows.stream()
            .map(row -> map(row))
            .collect(Collectors.toList());
    }
}
