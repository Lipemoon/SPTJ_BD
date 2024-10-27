package com.example.SPTJ_BD.model

enum GenderCharacter {
    M("M"),
    F("F")

    private final String code

    GenderCharacter(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }
}