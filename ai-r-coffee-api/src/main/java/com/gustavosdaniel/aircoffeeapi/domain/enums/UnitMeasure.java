package com.gustavosdaniel.aircoffeeapi.domain.enums;

import com.gustavosdaniel.aircoffeeapi.exception.BusinessRuleException;

public enum UnitMeasure {

    UN("UNIDADE"),
    KIT("KIT"),
    KG("QUILOGRAMA"),
    G("GRAMA"),
    L("LITRO"),
    ML("MILILITRO"),
    M("METRO"),
    CX("CAIXA"),
    PR("PAR");

    private final String description;

    UnitMeasure(String description) {
        this.description = description;
    }

    public static UnitMeasure fromCode(String code) {
        if (code == null) {
            throw new BusinessRuleException("Unidade não pode ser nula");
        }

        try {
            return UnitMeasure.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Unidade de medida inválida: '" + code + "'");
        }
    }
}
