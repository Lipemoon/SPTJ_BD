package com.example.SPTJ_BD.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "SPTJ_TournamentTeamWinner")
class TournamentTeamWinnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idTeamWinner

    @Column(nullable = true)
    List<Long> characters

}
