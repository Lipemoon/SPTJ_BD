package com.example.SPTJ_BD.model.Enum

enum FormatTournament {
    ONE_VS_ONE("1v1"),TWO_VS_TWO("2v2"),THREE_VS_THREE("3v3")
    private String code

    FormatTournament(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }

}