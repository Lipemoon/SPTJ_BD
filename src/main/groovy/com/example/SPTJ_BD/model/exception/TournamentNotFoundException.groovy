package com.example.SPTJ_BD.model.exception

class TournamentNotFoundException extends RuntimeException {
    TournamentNotFoundException(String message) {
        super(message)
    }
}
