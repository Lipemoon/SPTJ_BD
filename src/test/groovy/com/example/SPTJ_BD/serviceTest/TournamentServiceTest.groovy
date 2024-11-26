package com.example.SPTJ_BD.serviceTest

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.model.Enum.StatusTournament
import com.example.SPTJ_BD.model.input.TournamentFinalizedInput
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.repository.TournamentRepository
import com.example.SPTJ_BD.service.TournamentService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.mockito.Mockito.when

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    CharacterRepository characterRepository = Mockito.mock(CharacterRepository)
    TournamentRepository tournamentRepository = Mockito.mock(TournamentRepository)

    @InjectMocks
    TournamentService tournamentService

    @Test
    void shouldCreateTournamentWhenGenderTournamentIsValid() {
        // Arrange
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "1v1"
        )
        // Act
        Boolean genderTournamentInvalid = tournamentService.genderTournamentIsInvalid(tournamentEntity)
        // Assert
        assertEquals(false, genderTournamentInvalid)
    }

    @Test
    void notCreateTournamentWhenGenderTournamentIsInvalid() {
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "MM", format: "1v1"
        )
        Boolean genderTournamentInvalid = tournamentService.genderTournamentIsInvalid(tournamentEntity)
        assertEquals(true, genderTournamentInvalid)
    }

    @Test
    void shouldCreateTournamentWhenFormatTournamentIsValid() {
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "1v1"
        )
        Boolean genderTournamentInvalid = tournamentService.formatTournamentIsInvalid(tournamentEntity)
        assertEquals(false, genderTournamentInvalid)
    }

    @Test
    void notCreateTournamentWhenFormatTournamentIsInvalid() {
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "10v10"
        )
        Boolean genderTournamentInvalid = tournamentService.formatTournamentIsInvalid(tournamentEntity)
        assertEquals(true, genderTournamentInvalid)
    }

    @Test
    void shouldStartTournamentWhenStatusTournamentIsStarted() {
        String statusTournament = StatusTournament.TOURNAMENT_STARTED.getCode()
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "1v1", status: statusTournament
        )
        Boolean startTournament = tournamentService.tournamentIsNotStarted(tournamentEntity)
        assertEquals(false, startTournament)
    }

    @Test
    void notStartTournamentWhenStatusTournamentIsNotStarted() {
        String statusTournament = StatusTournament.NOT_INITIALIZED.getCode()
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "10v10", status: statusTournament
        )
        Boolean startTournament = tournamentService.tournamentIsNotStarted(tournamentEntity)
        assertEquals(true, startTournament)
    }

    @Test
    void tournamentIsFinished() {
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "1v1", status: StatusTournament.TOURNAMENT_FINALIZED.getCode()
        )
        Boolean finishedTournament = tournamentService.tournamentIsFinished(tournamentEntity)
        assertTrue(finishedTournament)
    }

    @Test
    void tournamentIsNotFinished() {
        TournamentEntity tournamentEntity = new TournamentEntity(
                id: 1, gameOrigin: "Naruto 4", categoryByGenre: "M", format: "1v1", status: StatusTournament.TOURNAMENT_PROGRESS.getCode()
        )
        Boolean finishedTournament = tournamentService.tournamentIsNotFinished(tournamentEntity)
        assertTrue(finishedTournament)
    }


}
