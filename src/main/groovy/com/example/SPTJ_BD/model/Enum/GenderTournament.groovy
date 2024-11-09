package com.example.SPTJ_BD.model.Enum

enum GenderTournament {
    M_F("M/F"), F_M("F/M"), M("M"), F("F")
    private String code

    GenderTournament(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }

}