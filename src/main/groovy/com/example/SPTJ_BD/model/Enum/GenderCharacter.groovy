package com.example.SPTJ_BD.model.Enum

enum GenderCharacter {
    M("Masculino"),
    F("Feminino")

    private final String code

    GenderCharacter(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }
}