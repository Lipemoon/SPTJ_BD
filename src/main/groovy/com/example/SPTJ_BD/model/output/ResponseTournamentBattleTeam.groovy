package com.example.SPTJ_BD.model.output

import com.example.SPTJ_BD.entity.CharacterEntity

class ResponseTournamentBattleTeam {
    String message
    Map<Long, List<CharacterEntity>> team1 = new HashMap<>()
    Map<Long, List<CharacterEntity>> team2 = new HashMap<>()
    String statusTournament
}
