package com.uniConnect.global.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EnumPascalCaseConverter implements AttributeConverter<Enum<?>, String> {

    @Override
    public String convertToDatabaseColumn(Enum<?> attribute) {
        if (attribute == null) return null;
        // Enum.name() -> SAMPLING → PascalCase로 변환
        String name = attribute.name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public Enum<?> convertToEntityAttribute(String dbData) {
        // Hibernate가 알아서 Enum 매핑하므로 생략 가능
        return null;
    }
}