package me.nuguri.common.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 엔티티 boolean 컬럼 true -> Y, false -> N 매핑 컨버터
 */
@Converter
public class BooleanColumnConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean booleanValue) {
        return booleanValue != null ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String stringValue) {
        return stringValue.equals("Y");
    }
}
