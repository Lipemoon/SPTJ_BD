package com.example.SPTJ_BD.entity
import com.example.SPTJ_BD.model.Enum.StatusTournament
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Tournament")
class TournamentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(nullable = false)
    String name

    @Column(nullable = false)
    String gameOrigin

    @Column(nullable = false, length = 3)
    String categoryByGenre

    @Column(nullable = false, length = 3)
    String format

    @Column(nullable = true)
    String status = StatusTournament.NOT_INITIALIZED.getCode()

    @Column(nullable = true)
    List<Long> characters = []

    @Column(nullable = true)
    List<Long> charactersWinnersOfRound = []

    @Column(nullable = true)
    Long winnerOfTournament

    @Column(nullable = false)
    Boolean matchIsStarted = false

    @Column(nullable = true)
    List<Long> charactersFighting = []

    @Column(nullable = true)
    List<Long> teams = []

    @Column(nullable = true)
    List<Long> teamWinnersOfRound = []

    @Column(nullable = true)
    Long teamWinner

}
