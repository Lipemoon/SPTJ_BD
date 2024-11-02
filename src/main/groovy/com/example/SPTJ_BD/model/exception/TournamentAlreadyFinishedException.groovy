package com.example.SPTJ_BD.model.exception

class TournamentAlreadyFinishedException extends RuntimeException {
    TournamentAlreadyFinishedException(String message) {
        super(message)
    }
}
