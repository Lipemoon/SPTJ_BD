package com.example.SPTJ_BD.serviceTest

import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.service.TournamentService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension

import static org.junit.jupiter.api.Assertions.assertEquals

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @InjectMocks
    TournamentService tournamentService

    @Test
    void shouldCreateTournamentWhenGenderTournamentValid() {
        // Arrange
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "1v1"
        )

        // Act
        Boolean genderTournamentInvalid = tournamentService.genderTournamentIsInvalid(tournamentEntity)

        // Assert
        assertEquals(false, genderTournamentInvalid)
    }



}
