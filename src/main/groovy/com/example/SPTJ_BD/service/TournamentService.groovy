package com.example.SPTJ_BD.service
import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.model.FormatTournament
import com.example.SPTJ_BD.model.GenderTournament
import com.example.SPTJ_BD.model.InvalidFormatTournamentException
import com.example.SPTJ_BD.model.InvalidGenderException
import com.example.SPTJ_BD.model.output.BattleStatus
import com.example.SPTJ_BD.model.output.ResponseChooseWinner
import com.example.SPTJ_BD.model.output.ResponseTournament
import com.example.SPTJ_BD.model.output.ResponseTournamentBattle
import com.example.SPTJ_BD.model.StatusTournament
import com.example.SPTJ_BD.model.TournamentNotFoundException
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.repository.TournamentRepository
import org.springframework.stereotype.Service

@Service
class TournamentService {

    private Random random
    private TournamentRepository tournamentRepository
    private CharacterRepository characterRepository

    TournamentService(TournamentRepository tournamentRepository, Random random, CharacterRepository characterRepository) {
        this.tournamentRepository = tournamentRepository
        this.random = random
        this.characterRepository = characterRepository
    }

    List<TournamentEntity> getAllTournaments() {
        return tournamentRepository.findAll()
    }

    TournamentEntity createTournament(TournamentEntity input, List<CharacterEntity> characters) {
        if (input.categoryByGenre != GenderTournament.F.getCode() &&
                input.categoryByGenre != GenderTournament.M.getCode() &&
                input.categoryByGenre != GenderTournament.F_M.getCode() &&
                input.categoryByGenre != GenderTournament.M_F.getCode()) {
            throw new InvalidGenderException("Invalid Tournament Genre Category!")
        } else if (input.format != FormatTournament.ONE_VS_ONE.getCode() &&
                input.format != FormatTournament.TWO_VS_TWO.getCode() &&
                input.format != FormatTournament.THREE_VS_THREE.getCode()) {
            throw new InvalidFormatTournamentException("Invalid Tournament Format!")
        } else {
            if (input.format == FormatTournament.ONE_VS_ONE.getCode()) {
                addCharactersTournament1v1(input, characters)
            } else if (input.format == FormatTournament.TWO_VS_TWO.getCode()) {
                //addCharactersTournament2v2(input, tournamentCharacters)
            } else if (input.format == FormatTournament.THREE_VS_THREE.getCode()) {
                //addCharactersTournament3v3(input, tournamentCharacters)
            }
            tournamentRepository.save(input)
        }
    }

    ResponseTournament startTournament(Long idTournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.status == StatusTournament.NOT_INITIALIZED.getCode()) {
            tournamentEntity.status = StatusTournament.TOURNAMENT_STARTED.getCode()
            tournamentRepository.save(tournamentEntity)
            return new ResponseTournament(message: "Torneio Iniciado com sucesso!",
                    id: tournamentEntity.id, name: tournamentEntity.name,
                    gameName: tournamentEntity.gameOrigin,
                    status: tournamentEntity.status,
                    format: tournamentEntity.format)
        } else {
            throw new RuntimeException("Torneio já iniciado!")
        }
    }

    ResponseTournamentBattle startMatchOfTournament(Long idTournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.format == FormatTournament.ONE_VS_ONE.getCode()) {
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
                throw new RuntimeException("Torneio já foi Finalizado!")
            }
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode() &&
                    tournamentEntity.matchIsStarted) {
                throw new RuntimeException("Você não pode iniciar outra partida sem " +
                        "primeiro escolher um personagem deste torneio para ganhar!")
            } else if (tournamentEntity.status == StatusTournament.TOURNAMENT_STARTED.getCode() ||
                    tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
                BattleStatus battleStatus = start1v1Battle(tournamentEntity)
                if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
                    CharacterEntity characterEntity = characterRepository.findById(tournamentEntity.winnerOfTournament).get()
                    return new ResponseTournamentBattle(message: "Torneio Finalizado, Campeão " +
                            "${characterEntity.name}", player1: null,
                            player2: null, statusTournament: tournamentEntity.status)
                } else {
                    return new ResponseTournamentBattle(message: "Confronto Iniciado", player1:
                            battleStatus.player1, player2: battleStatus.player2,
                            statusTournament: battleStatus.statusTournament)
                }
            } else {
                throw new RuntimeException("Torneio não foi Iniciado primeiro!")
            }
        }
    }

    ResponseChooseWinner chooseWinnerOfMatch1v1(Long idTournament, Long idPlayer) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
            throw new RuntimeException("Torneio já finalizado!")
        } else if (tournamentEntity.status == StatusTournament.TOURNAMENT_STARTED.getCode() ||
                tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
            for (Long id : tournamentEntity.charactersFighting.findAll()) {
                if (id == idPlayer) {
                    CharacterEntity characterEntity = characterRepository.findById(idPlayer).get()
                    tournamentEntity.matchIsStarted = false
                    tournamentEntity.charactersWinnersOfRound.add(idPlayer)
                    tournamentEntity.charactersFighting.clear()
                    tournamentRepository.save(tournamentEntity)
                    return new ResponseChooseWinner(message: "Personagem ${characterEntity.name} ganhou!")
                }
            }
            throw new RuntimeException("Id player nao existe na tabela characters Fighting!")
        }
        throw new RuntimeException("Não tem personagens lutando nesse momento para você " +
                "escolher algum vencedor!")
    }

    private void addCharactersTournament1v1(TournamentEntity input, List<CharacterEntity> characters) {
        for (CharacterEntity character : characters) {
            if (character.gameOrigin == input.gameOrigin && character.gender == input.categoryByGenre) {
                input.characters.add(character.id)
            } else if (character.gameOrigin == input.gameOrigin && input.categoryByGenre == GenderTournament.M_F.getCode() || input.categoryByGenre == GenderTournament.F_M.getCode()) {
                input.characters.add(character)
            }
        }
    }

    private BattleStatus start1v1Battle(TournamentEntity tournamentEntity) {
        if (tournamentEntity.characters.size() == 1) {
            tournamentEntity.charactersWinnersOfRound.add(tournamentEntity.characters.get(0))
            tournamentEntity.characters.clear()
        }
        if (tournamentEntity.characters.size() == 0 &&
                tournamentEntity.charactersWinnersOfRound.size() == 1) {
            tournamentEntity.winnerOfTournament = tournamentEntity.charactersWinnersOfRound.get(0)
            tournamentEntity.charactersWinnersOfRound.clear()
            tournamentEntity.status = StatusTournament.TOURNAMENT_FINALIZED.getCode()
            tournamentRepository.save(tournamentEntity)
            return new BattleStatus(player1: null, player2: null, statusTournament: tournamentEntity.status)
        }
        if (tournamentEntity.characters.isEmpty() &&
                tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
            tournamentEntity.characters.addAll(tournamentEntity.charactersWinnersOfRound)
            tournamentEntity.charactersWinnersOfRound.clear()
        }
        if (tournamentEntity.characters.size() > 1) {
            Long id = tournamentEntity.characters.get(random.nextInt(tournamentEntity.characters.size()))
            CharacterEntity characterEntity1 = characterRepository.findById(id).get()
            tournamentEntity.characters.remove(id)
            tournamentEntity.charactersFighting.add(id)
            id = tournamentEntity.characters.get(random.nextInt(tournamentEntity.characters.size()))
            CharacterEntity characterEntity2 = characterRepository.findById(id).get()
            tournamentEntity.characters.remove(id)
            tournamentEntity.charactersFighting.add(id)
            tournamentEntity.matchIsStarted = true
            tournamentEntity.status = StatusTournament.TOURNAMENT_PROGRESS.getCode()
            tournamentRepository.save(tournamentEntity)
            return new BattleStatus(player1: characterEntity1, player2: characterEntity2,
                    statusTournament: tournamentEntity.status)
        }

    }






//    void addCharactersTournament2v2(TournamentInput input, List<Character> tournamentCharacters) {
//        Integer id = 1
//        Map<String, List<Character>> team = new HashMap<>()
//        if (tournamentCharacters.size() % 2 == 0) {
//            while (tournamentCharacters.size() > 0) {
//                List<Character> equipe = []
//                Character character1 = tournamentCharacters.get(random.nextInt(tournamentCharacters.size()))
//                tournamentCharacters.remove(character1)
//                Character character2 = tournamentCharacters.get(random.nextInt(tournamentCharacters.size()))
//                tournamentCharacters.remove(character2)
//                equipe.add(character1)
//                equipe.add(character2)
//                team.put(id.toString(), equipe)
//                id += 1
//            }
//            Tournament newTournament = new Tournament(input.id, input.name, input.day, input.month,
//                    input.year, input.gameName, input.categoryByGenre, tournamentCharacters, input.format, team)
//            tournaments.add(newTournament)
//        } else {
//            throw new RuntimeException("Falta 1 personagem para fechar um time certinho")
//        }
//    }
//
//    void addCharactersTournament3v3(TournamentInput input, List<Character> tournamentCharacters) {
//        Integer id = 1
//        Map<String, List<Character>> team = new HashMap<>()
//        if (tournamentCharacters.size() % 3 == 0) {
//            while (tournamentCharacters.size() > 0) {
//                List<Character> equipe = []
//                Character character1 = tournamentCharacters.get(random.nextInt(tournamentCharacters.size()))
//                tournamentCharacters.remove(character1)
//                Character character2 = tournamentCharacters.get(random.nextInt(tournamentCharacters.size()))
//                tournamentCharacters.remove(character2)
//                Character character3 = tournamentCharacters.get(random.nextInt(tournamentCharacters.size()))
//                tournamentCharacters.remove(character3)
//                equipe.add(character1)
//                equipe.add(character2)
//                equipe.add(character3)
//                team.put(id.toString(), equipe)
//                id += 1
//            }
//            Tournament newTournament = new Tournament(input.id, input.name, input.day, input.month,
//                    input.year, input.gameName, input.categoryByGenre, tournamentCharacters, input.format, team)
//            tournaments.add(newTournament)
//        } else {
//            Integer resto = tournamentCharacters.size() % 3
//            if (resto != 0) {
//                resto = 3 - resto
//                throw new RuntimeException("Falta ${resto} personagens para fechar um time certo")
//            }
//        }
//    }
//
//    ResponseOutputBattleTeamStatus startMatchOfTournamentTeam(Integer idTournament) {
//        for (Tournament tournament : getTournaments()) {
//            if (tournament.id == idTournament) {
//                if (tournament.format == Format.TWO_VS_TWO.getCode()) {
//                    if (tournament.status == Status.TOURNAMENT_PROGRESS.getCode() && tournament.matchIsStarted) {
//                        throw new RuntimeException("Você não pode iniciar outra partida sem " +
//                                "primeiro escolher um personagem para ganhar!")
//                    } else if (tournament.status == Status.TOURNAMENT_STARTED.getCode() || tournament.status == Status.TOURNAMENT_PROGRESS.getCode()) {
//                        BattleTeamStatusOutput battleTeamStatusOutput =
//                                startTournamentTeamBattle(tournament)
//                        if (tournament.status == Status.TOURNAMENT_FINALIZED.getCode()) {
//                            return new ResponseOutputBattleTeamStatus(message: "Torneio Finalizado, " +
//                                    "Equipe vencedora ${}")
//                        } else {
//                            return new ResponseOutputBattleTeamStatus(message: "Confronto Iniciado", team1:
//                                    battleTeamStatusOutput.team1, team2: battleTeamStatusOutput.team2, statusTournament: battleTeamStatusOutput.statusTournament)
//                        }
//                    }
//                } else {
//                    throw new RuntimeException("Formato do torneio diferente!")
//                }
//            } else {
//                throw new RuntimeException("Torneio Inexistente!")
//            }
//        }
//        throw new RuntimeException("Você não pode iniciar uma partida sem iniciar o " +
//                "Torneio primeiro!")
//    }


}
