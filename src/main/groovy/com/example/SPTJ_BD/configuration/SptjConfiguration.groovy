package com.example.SPTJ_BD.configuration

import com.example.SPTJ_BD.controller.CharacterController
import com.example.SPTJ_BD.controller.TournamentController
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.repository.TournamentRepository
import com.example.SPTJ_BD.service.CharacterService
import com.example.SPTJ_BD.service.TournamentService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SptjConfiguration {

    @Bean
    CharacterController characterController(CharacterService characterService) {
        return new CharacterController(characterService)
    }

    @Bean
    TournamentController tournamentController(TournamentService tournamentService, CharacterService characterService) {
        return new TournamentController(tournamentService, characterService)
    }

    @Bean
    CharacterService characterService(CharacterRepository characterRepository) {
        return new CharacterService(characterRepository)
    }

    @Bean
    Random random() {
        return new Random()
    }

    @Bean
    TournamentService tournamentService(TournamentRepository tournamentRepository, Random random, CharacterRepository characterRepository) {
        return new TournamentService(tournamentRepository, random, characterRepository)
    }

}
