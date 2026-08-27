package com.gustavosdaniel.aircoffeeapi.domain.enums;

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
}
