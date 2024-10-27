package com.example.SPTJ_BD.repository

import com.example.SPTJ_BD.entity.CharacterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {

}