package com.example.SPTJ_BD.model.Enum

enum StatusTournament {
    NOT_INITIALIZED("Not Initialized"),
    TOURNAMENT_STARTED("Tournament Started"),
    TOURNAMENT_PROGRESS("Tournament in Progress"),
    TOURNAMENT_FINALIZED("Tournament Finalized")
    private String code

    StatusTournament(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }

}