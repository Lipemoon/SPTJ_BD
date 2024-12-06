package com.example.SPTJ_BD.model.Enum

enum GenderTournament {
    M_F("Masculino e Feminino"),
    M("Masculino"), F("Feminino")
    private String code

    GenderTournament(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }

}