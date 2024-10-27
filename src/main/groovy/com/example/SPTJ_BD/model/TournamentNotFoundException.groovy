package com.example.SPTJ_BD.model

class TournamentNotFoundException extends RuntimeException {
    TournamentNotFoundException(String message) {
        super(message)
    }
}
