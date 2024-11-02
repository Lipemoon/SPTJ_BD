package com.example.SPTJ_BD.service

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.CharacterTeamEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.model.FormatTournament
import com.example.SPTJ_BD.model.GenderTournament
import com.example.SPTJ_BD.model.exception.InvalidFormatTournamentException
import com.example.SPTJ_BD.model.exception.InvalidGenderException
import com.example.SPTJ_BD.model.output.BattleStatus
import com.example.SPTJ_BD.model.output.ResponseChooseWinner
import com.example.SPTJ_BD.model.output.ResponseTournament
import com.example.SPTJ_BD.model.output.ResponseTournamentBattle
import com.example.SPTJ_BD.model.StatusTournament
import com.example.SPTJ_BD.model.exception.TournamentNotFoundException
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.repository.CharacterTeamRepository
import com.example.SPTJ_BD.repository.TournamentRepository
import org.springframework.stereotype.Service

@Service
class TournamentService {

    private Random random
    private TournamentRepository tournamentRepository
    private CharacterRepository characterRepository
    private CharacterTeamRepository characterTeamRepository

    TournamentService(TournamentRepository tournamentRepository, Random random, CharacterRepository characterRepository, CharacterTeamRepository characterTeamRepository) {
        this.tournamentRepository = tournamentRepository
        this.random = random
        this.characterRepository = characterRepository
        this.characterTeamRepository = characterTeamRepository
    }

    List<TournamentEntity> getAllTournaments() {
        return tournamentRepository.findAll()
    }

    TournamentEntity createTournament(TournamentEntity input, List<CharacterEntity> characters) {
        if (genderTournamentIsInvalid(input)) {
            throw new InvalidGenderException("")
        }
        if (formatTournamentIsInvalid(input)) {
            throw new InvalidFormatTournamentException("")
        }
        if (input.format == FormatTournament.ONE_VS_ONE.getCode()) {
            addCharactersTournament1v1(input, characters)
        } else if (input.format == FormatTournament.TWO_VS_TWO.getCode()) {
            addCharactersTournament2v2(input, characters)
        } else if (input.format == FormatTournament.THREE_VS_THREE.getCode()) {
            addCharactersTournament3v3(input, characters)
        }
        tournamentRepository.save(input)
    }

    static boolean genderTournamentIsInvalid(TournamentEntity input) {
        if (input.categoryByGenre != GenderTournament.F.getCode() &&
                input.categoryByGenre != GenderTournament.M.getCode() &&
                input.categoryByGenre != GenderTournament.F_M.getCode() &&
                input.categoryByGenre != GenderTournament.M_F.getCode()) {
            return true
        }
        return false
    }

    static boolean formatTournamentIsInvalid(TournamentEntity input) {
        if (input.format != FormatTournament.ONE_VS_ONE.getCode() &&
                input.format != FormatTournament.TWO_VS_TWO.getCode() &&
                input.format != FormatTournament.THREE_VS_THREE.getCode()) {
            return true
        }
        return false
    }

    TournamentEntity getTournamentById(Long id) {
        tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
    }

    TournamentEntity updateTournament(Long id, TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        TournamentEntity updateTournament = tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
        updateTournament.name = tournamentEntity.name
        updateTournament.gameOrigin = tournamentEntity.gameOrigin
        updateTournament.categoryByGenre = tournamentEntity.categoryByGenre
        updateTournament.format = tournamentEntity.format
        if (updateTournament.format == FormatTournament.ONE_VS_ONE.getCode()) {
            addCharactersTournament1v1(tournamentEntity, characters)
        } else if (updateTournament.format == FormatTournament.TWO_VS_TWO.getCode()) {
            addCharactersTournament2v2(tournamentEntity, characters)
        } else if (updateTournament.format == FormatTournament.THREE_VS_THREE.getCode()) {
            addCharactersTournament3v3(tournamentEntity, characters)
        }
        tournamentRepository.save(updateTournament)
    }

    void deleteTournament(Long id) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
        tournamentRepository.save(tournamentEntity)
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
        } else if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode()) {
            //
        } else if (tournamentEntity.format == FormatTournament.THREE_VS_THREE.getCode()) {
           //
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

    private void addCharactersTournament1v1(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        for (CharacterEntity character : characters) {
            if (character.gameOrigin == tournamentEntity.gameOrigin && character.gender == tournamentEntity.categoryByGenre) {
                tournamentEntity.characters.add(character.id)
            } else if (character.gameOrigin == tournamentEntity.gameOrigin && tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
                tournamentEntity.characters.add(character.id)
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


    private void addCharactersTournament2v2(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        List<Long> charactersTournaments = []
        for (CharacterEntity character : characters) {
            if (character.gameOrigin == tournamentEntity.gameOrigin && character.gender == tournamentEntity.categoryByGenre) {
                charactersTournaments.add(character.id)
            } else if (character.gameOrigin == tournamentEntity.gameOrigin && tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
                charactersTournaments.add(character.id)
            }
        }
        if (charactersTournaments.size() % 2 == 0) {
            while (charactersTournaments.size() > 0) {
                CharacterTeamEntity characterTeam = new CharacterTeamEntity()
                characterTeam.idTournament = tournamentEntity.id
                Long id = charactersTournaments.get(random.nextInt(charactersTournaments.size()))
                characterTeam.characters.add(id)
                charactersTournaments.remove(id)
                id = charactersTournaments.get(random.nextInt(charactersTournaments.size()))
                characterTeam.characters.add(id)
                charactersTournaments.remove(id)
                characterTeamRepository.save(characterTeam)
            }
        } else {
            throw new RuntimeException("Falta 1 personagem para fechar um time certinho")
        }

    }

    private void addCharactersTournament3v3(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        List<Long> charactersTournaments = []
        for (CharacterEntity character : characters) {
            if (character.gameOrigin == tournamentEntity.gameOrigin && character.gender == tournamentEntity.categoryByGenre) {
                charactersTournaments.add(character.id)
            } else if (character.gameOrigin == tournamentEntity.gameOrigin && tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
                charactersTournaments.add(character.id)
            }
        }
        if (charactersTournaments.size() % 3 == 0) {
            while (charactersTournaments.size() > 0) {
                CharacterTeamEntity characterTeam = new CharacterTeamEntity()
                characterTeam.idTournament = tournamentEntity.id
                Long id = charactersTournaments.get(random.nextInt(charactersTournaments.size()))
                characterTeam.characters.add(id)
                charactersTournaments.remove(id)
                id = charactersTournaments.get(random.nextInt(charactersTournaments.size()))
                characterTeam.characters.add(id)
                charactersTournaments.remove(id)
                id = charactersTournaments.get(random.nextInt(charactersTournaments.size()))
                characterTeam.characters.add(id)
                charactersTournaments.remove(id)
                characterTeamRepository.save(characterTeam)
            }
        } else {
            Long falta = charactersTournaments.size() % 3
            if (falta != 0) {
                falta = 3 - falta
                throw new RuntimeException("Falta $falta personagens para fechar um time certo")
           }
       }
    }

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
