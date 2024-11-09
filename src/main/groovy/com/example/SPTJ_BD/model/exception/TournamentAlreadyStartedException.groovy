package com.example.SPTJ_BD.model.exception

class TournamentAlreadyStartedException extends RuntimeException {
    TournamentAlreadyStartedException(String message) {
        super(message)
    }
}
