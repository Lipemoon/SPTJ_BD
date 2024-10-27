package com.example.SPTJ_BD.repository

import com.example.SPTJ_BD.entity.TournamentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TournamentRepository extends JpaRepository<TournamentEntity, Long> {

}