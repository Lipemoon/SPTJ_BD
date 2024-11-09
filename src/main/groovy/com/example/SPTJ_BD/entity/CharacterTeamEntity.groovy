package com.example.SPTJ_BD.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "TeamCharacters")
class CharacterTeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idTeam

    @Column(nullable = false)
    List<Long> characters = []

    @Column(nullable = false)
    Long idTournament // Tem que ser o id do TournamentEntity

}
