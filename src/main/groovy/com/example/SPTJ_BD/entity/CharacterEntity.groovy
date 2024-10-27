package com.example.SPTJ_BD.entity

import com.example.SPTJ_BD.model.GenderCharacter
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "SPTJ_Character")
class CharacterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(nullable = false)
    String name

    @Column(nullable = false, length = 1)
    String gender

    @Column(nullable = false)
    String gameOrigin

}
