package com.example.SPTJ_BD.service

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.CharacterTeamEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.entity.TournamentTeamWinnerEntity
import com.example.SPTJ_BD.model.Enum.FormatTournament
import com.example.SPTJ_BD.model.Enum.GenderTournament
import com.example.SPTJ_BD.model.exception.CharacterNotFoundException
import com.example.SPTJ_BD.model.exception.TournamentAlreadyStartedException
import com.example.SPTJ_BD.model.exception.TournamentNotStartedException
import com.example.SPTJ_BD.model.exception.TournamentRegisterException
import com.example.SPTJ_BD.model.input.TournamentFinalizedInput
import com.example.SPTJ_BD.model.exception.InvalidFormatTournamentException
import com.example.SPTJ_BD.model.exception.InvalidGenderException
import com.example.SPTJ_BD.model.exception.TournamentAlreadyFinishedException
import com.example.SPTJ_BD.model.output.Battle1v1Status
import com.example.SPTJ_BD.model.output.BattleTeamStatus
import com.example.SPTJ_BD.model.output.ResponseChooseWinner
import com.example.SPTJ_BD.model.output.ResponseTournament
import com.example.SPTJ_BD.model.Enum.StatusTournament
import com.example.SPTJ_BD.model.exception.TournamentNotFoundException
import com.example.SPTJ_BD.model.output.ResponseTournamentBattle1v1
import com.example.SPTJ_BD.model.output.ResponseTournamentBattleTeam
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.repository.CharacterTeamRepository
import com.example.SPTJ_BD.repository.TournamentRepository
import com.example.SPTJ_BD.repository.TournamentTeamWinnerRepository
import org.springframework.stereotype.Service

@Service
class TournamentService {

    private Random random
    private TournamentRepository tournamentRepository
    private CharacterRepository characterRepository
    private CharacterTeamRepository characterTeamRepository
    private TournamentTeamWinnerRepository tournamentTeamWinnerRepository

    TournamentService(TournamentRepository tournamentRepository, Random random, CharacterRepository characterRepository, CharacterTeamRepository characterTeamRepository, TournamentTeamWinnerRepository tournamentTeamWinnerRepository) {
        this.tournamentRepository = tournamentRepository
        this.random = random
        this.characterRepository = characterRepository
        this.characterTeamRepository = characterTeamRepository
        this.tournamentTeamWinnerRepository = tournamentTeamWinnerRepository
    }

    List<TournamentEntity> getAllTournaments() {
        return tournamentRepository.findAll()
    }

    TournamentEntity createTournament(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        if (genderTournamentIsInvalid(tournamentEntity)) {
            throw new InvalidGenderException("Gender of Tournament is Invalid!")
        }
        if (formatTournamentIsInvalid(tournamentEntity)) {
            throw new InvalidFormatTournamentException("Format of Tournament is Invalid!")
        }
        if (tournamentEntity.format == FormatTournament.ONE_VS_ONE.getCode()) {
            addCharactersTournament1v1(tournamentEntity, characters)
        } else {
            tournamentRepository.save(tournamentEntity)
            addCharactersTournamentTeam(tournamentEntity, characters)
        }
        tournamentRepository.save(tournamentEntity)
    }

    TournamentEntity registerTournamentFinalized(TournamentFinalizedInput input) {
        TournamentEntity tournamentEntity = new TournamentEntity()
        List<Long> idsCharactersWinners = []
        tournamentEntity.name = input.name
        tournamentEntity.gameOrigin = input.gameOrigin
        tournamentEntity.categoryByGenre = input.categoryByGenre
        tournamentEntity.format = input.format
        idsCharactersWinners.addAll(input.idCharacterWinner)
        tournamentEntity.status = StatusTournament.TOURNAMENT_FINALIZED.getCode()
        tournamentRepository.save(tournamentEntity)
        if (genderTournamentIsInvalid(tournamentEntity)) {
            throw new InvalidGenderException("Gender of Tournament is Invalid!")
        }
        if (formatTournamentIsInvalid(tournamentEntity)) {
            throw new InvalidFormatTournamentException("Format of Tournament is Invalid!")
        }
        if (tournamentEntity.format == FormatTournament.ONE_VS_ONE.getCode() &&
                idsCharactersWinners.size() == 1) {
                conferirDadosDoTorneioFormato1v1(tournamentEntity, idsCharactersWinners)
            } else {
                throw new TournamentRegisterException("Id Character Winner is different of format Tournament")
            }
            if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode() &&
                    input.idCharacterWinner.size() == 2) {
                conferirDadosDoTorneioFormato2v2(tournamentEntity, idsCharactersWinners)
            } else {
                throw new TournamentRegisterException("Id Character Winner is different of format Tournament")
            }
            if (tournamentEntity.format == FormatTournament.THREE_VS_THREE.getCode() &&
                    input.idCharacterWinner.size() == 3) {
                conferirDadosDoTorneioFormato3v3(tournamentEntity, idsCharactersWinners)
            } else {
                throw new TournamentRegisterException("Id Character Winner is different of format Tournament")
            }
        tournamentRepository.save(tournamentEntity)
        }

    TournamentEntity getTournamentById(Long id) {
        tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
    }

//    CharacterEntity getWinnerTournamentById(Long id) {
//        TournamentEntity tournamentEntity = tournamentRepository.findById(id).orElseThrow {
//            new TournamentNotFoundException("Tournament not found with id $id")
//        }
//        if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
//            CharacterEntity characterEntity = characterRepository.findById(tournamentEntity.winnerOfTournament).get()
//            return characterEntity
//        } else {
//            throw new RuntimeException("Esse torneio id $id não foi finalizado!")
//        }
//    }

//    TournamentEntity updateTournament(Long id, TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
//        TournamentEntity updateTournament = tournamentRepository.findById(id).orElseThrow {
//            new TournamentNotFoundException("Tournament not found with id $id")
//        }
//        updateTournament.name = tournamentEntity.name
//        updateTournament.gameOrigin = tournamentEntity.gameOrigin
//        updateTournament.categoryByGenre = tournamentEntity.categoryByGenre
//        updateTournament.format = tournamentEntity.format
//        if (updateTournament.format == FormatTournament.ONE_VS_ONE.getCode()) {
//            addCharactersTournament1v1(tournamentEntity, characters)
//        } else {
//            addCharactersTournamentTeam(tournamentEntity, characters)
//        }
//        tournamentRepository.save(updateTournament)
//    }

    void deleteTournament(Long id) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
        tournamentRepository.delete(tournamentEntity)
    }

    ResponseTournament startTournament(Long idTournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.status == StatusTournament.NOT_INITIALIZED.getCode()) {
            tournamentEntity.status = StatusTournament.TOURNAMENT_STARTED.getCode()
            tournamentRepository.save(tournamentEntity)
            return new ResponseTournament(message: "Tournament Started of Success!",
                    id: tournamentEntity.id, name: tournamentEntity.name,
                    gameName: tournamentEntity.gameOrigin,
                    status: tournamentEntity.status,
                    format: tournamentEntity.format)
        } else {
            throw new TournamentAlreadyStartedException("Tournament already Started!")
        }
    }

    ResponseTournamentBattle1v1 startMatchOfTournament1v1(Long idTournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.format == FormatTournament.ONE_VS_ONE.getCode()) {
            if (tournamentIsFinished(tournamentEntity)) {
                throw new TournamentAlreadyFinishedException("Tournament already finished!")
            }
            if (matchIsStarted(tournamentEntity)) {
                throw new RuntimeException("Você não pode iniciar outra partida sem " +
                        "primeiro escolher um personagem deste torneio para ganhar!")
            }
            if (tournamentIsNotFinished(tournamentEntity)) {
                Battle1v1Status battleStatus = start1v1Battle(tournamentEntity)
                if (tournamentIsFinished(tournamentEntity)) {
                    CharacterEntity characterEntity = characterRepository.findById(tournamentEntity.winnerOfTournament).get()
                    return new ResponseTournamentBattle1v1(message: "Tournament Finished, Winner" +
                            "${characterEntity.name}", player1: null,
                            player2: null, statusTournament: tournamentEntity.status)
                } else {
                    return new ResponseTournamentBattle1v1(message: "Confronto Iniciado", player1:
                            battleStatus.player1, player2: battleStatus.player2,
                            statusTournament: battleStatus.statusTournament)
                }
            } else {
                throw new TournamentNotStartedException("Tournament not started first!")
            }
        } else {
            throw new InvalidFormatTournamentException("Torneio que você tentou iniciar tem outro formato!")
        }
    }

    ResponseChooseWinner chooseWinnerOfMatch1v1(Long idTournament, Long idPlayer) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentIsFinished(tournamentEntity)) {
            throw new TournamentAlreadyFinishedException("Tournament already finished!")
        }
        if (tournamentIsNotFinished(tournamentEntity)) {
            if (tournamentEntity.charactersFighting.isEmpty()) {
                throw new RuntimeException("Não tem personagens lutando nesse momento para você " +
                        "escolher algum vencedor!")
            } else {
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
        } else {
            throw new RuntimeException("Torneio não foi Iniciado primeiro!")
        }
    }

    ResponseChooseWinner chooseWinnerOfMatchTeam(Long idTournament, Long idTeam) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode() ||
            tournamentEntity.format == FormatTournament.THREE_VS_THREE.getCode()) {
            for (Long idTeams : tournamentEntity.charactersFighting.findAll()) {
                if (idTeams == idTeam) {
                    List<CharacterEntity> charactersEntity = verificarPersonagensTorneioTeam(tournamentEntity, idTeam)
                    if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode()) {
                        tournamentRepository.save(tournamentEntity)
                        return new ResponseChooseWinner(message: "Team com os Personagens" +
                                "${charactersEntity.name.get(0)} e ${charactersEntity.name.get(1)} ganharam!")
                    } else {
                        tournamentRepository.save(tournamentEntity)
                        return new ResponseChooseWinner(message: "Team com os Personagens" +
                                "${charactersEntity.name.get(0)}, ${charactersEntity.name.get(1)} e " +
                                "${charactersEntity.name.get(2)} ganharam!")
                    }
                    }
                }
            throw new RuntimeException("Id player nao existe na tabela characters Fighting!")
        } else {
            throw new InvalidFormatTournamentException("Torneio que você tentou iniciar tem outro formato!")
        }
    }

    private static void addCharactersTournament1v1(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        for (CharacterEntity character : characters) {
            if (character.gameOrigin == tournamentEntity.gameOrigin && character.gender == tournamentEntity.categoryByGenre) {
                tournamentEntity.characters.add(character.id)
            } else if (character.gameOrigin == tournamentEntity.gameOrigin && tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
                tournamentEntity.characters.add(character.id)
            }
        }
    }

    private Battle1v1Status start1v1Battle(TournamentEntity tournamentEntity) {
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
            return new Battle1v1Status(player1: null, player2: null, statusTournament: tournamentEntity.status)
        }
        if (tournamentEntity.characters.isEmpty() &&
                tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
            tournamentEntity.characters.addAll(tournamentEntity.charactersWinnersOfRound)
            tournamentEntity.charactersWinnersOfRound.clear()
        }
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
        return new Battle1v1Status(player1: characterEntity1, player2: characterEntity2,
                statusTournament: tournamentEntity.status)
    }

    private void addCharactersTournamentTeam(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        List<Long> charactersTournaments = []
        for (CharacterEntity character : characters) {
            if (character.gameOrigin == tournamentEntity.gameOrigin && character.gender == tournamentEntity.categoryByGenre) {
                charactersTournaments.add(character.id)
            } else if (character.gameOrigin == tournamentEntity.gameOrigin && tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
                charactersTournaments.add(character.id)
            }
        }
        if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode()) {
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
                adicionarTimesNoTorneio(tournamentEntity)
            } else {
                throw new RuntimeException("Falta 1 personagem para fechar um time certinho")
            }
        } else {
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
                adicionarTimesNoTorneio(tournamentEntity)
            } else {
                Long falta = charactersTournaments.size() % 3
                if (falta != 0) {
                    falta = 3 - falta
                    throw new RuntimeException("Falta $falta personagens para fechar um time certo")
                }
            }
        }
    }

    ResponseTournamentBattleTeam startMatchOfTournamentTeam(Long idTournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode() ||
                tournamentEntity.format == FormatTournament.THREE_VS_THREE.getCode()) {
            if (tournamentIsFinished(tournamentEntity)) {
                throw new TournamentAlreadyFinishedException("Tournament already finished!")
            }
            if (matchIsStarted(tournamentEntity)) {
                throw new RuntimeException("Já tem uma partida em andamento!")
            }
            if (tournamentIsNotFinished(tournamentEntity)) {
                BattleTeamStatus battleTeamStatus = startTeamBattle(tournamentEntity)
                if (tournamentIsFinished(tournamentEntity)) {
                    return new ResponseTournamentBattleTeam(message: "Torneio Finalizado, " +
                            "Equipe vencedora ",
                            team1: battleTeamStatus.team1, team2: null, statusTournament: tournamentEntity.status)
                } else {
                    return new ResponseTournamentBattleTeam(message: "Confronto Iniciado",
                            team1: battleTeamStatus.team1, team2: battleTeamStatus.team2,
                            statusTournament: battleTeamStatus.statusTournament)
                }
            } else {
                throw new RuntimeException("Torneio não foi Iniciado primeiro!")
            }
        } else {
            throw new InvalidFormatTournamentException("Torneio que você tentou iniciar tem outro formato!")
        }
    }

    private BattleTeamStatus startTeamBattle(TournamentEntity tournamentEntity) {
        if (tournamentEntity.teams.size() == 1) {
            tournamentEntity.teamWinnersOfRound.add(tournamentEntity.teams.get(0))
            tournamentEntity.teams.clear()
        }
        if (tournamentEntity.teams.size() == 0 && tournamentEntity.teamWinnersOfRound.size() == 1) {
            TournamentTeamWinnerEntity teamWinnerEntity = new TournamentTeamWinnerEntity()
            teamWinnerEntity.idTournament = tournamentEntity.id
            CharacterTeamEntity characterTeam = characterTeamRepository.findById(tournamentEntity.teamWinnersOfRound.get(0)).get()
            Long idCharacter = characterTeam.characters.get(0)
            teamWinnerEntity.characters.add(idCharacter)
            idCharacter = characterTeam.characters.get(1)
            teamWinnerEntity.characters.add(idCharacter)
            tournamentTeamWinnerRepository.save(teamWinnerEntity)
            List<TournamentTeamWinnerEntity> teamsWinners = tournamentTeamWinnerRepository.findAll()
            for (TournamentTeamWinnerEntity teamWinner : teamsWinners) {
                if (teamWinner.idTournament == tournamentEntity.id) {
                    tournamentEntity.teamWinner = teamWinner.id
                }
            }
            List<CharacterTeamEntity> characterTeamEntity = characterTeamRepository.findAll()
            for (CharacterTeamEntity team : characterTeamEntity) {
                if (team.idTournament == tournamentEntity.id) {
                    characterTeamRepository.delete(team)
                }
            }
            tournamentEntity.teamWinnersOfRound.clear()
            tournamentEntity.status = StatusTournament.TOURNAMENT_FINALIZED.getCode()
            Map<Long, List<CharacterEntity>> team1 = new HashMap<>()
            tournamentRepository.save(tournamentEntity)
            return new BattleTeamStatus(team1: team1, team2: null, statusTournament: tournamentEntity.status)
        }
        if (tournamentEntity.teams.isEmpty() &&
                tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
            tournamentEntity.teams.addAll(tournamentEntity.teamWinnersOfRound)
            tournamentEntity.teamWinnersOfRound.clear()
        }
        Map<Long, List<CharacterEntity>> team1 = pegarAleatorioTeam1ParaBatalhar(tournamentEntity)
        Map<Long, List<CharacterEntity>> team2 = pegarAleatorioTeam2ParaBatalhar(tournamentEntity)
        tournamentEntity.matchIsStarted = true
        tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()
        tournamentRepository.save(tournamentEntity)
        return new BattleTeamStatus(team1: team1, team2: team2, statusTournament: tournamentEntity.status)
    }

    private Map<Long, List<CharacterEntity>> pegarAleatorioTeam1ParaBatalhar(TournamentEntity tournamentEntity) {
        List<CharacterEntity> charactersTeam1 = []
        Map<Long, List<CharacterEntity>> team1 = new HashMap<>()
        Long id = tournamentEntity.teams.get(random.nextInt(tournamentEntity.teams.size()))
        CharacterTeamEntity characterTeam = characterTeamRepository.findById(id).get()
        Long idCharacter = characterTeam.characters.get(0)
        CharacterEntity characterEntity = characterRepository.findById(idCharacter).get()
        idCharacter = characterTeam.characters.get(1)
        CharacterEntity characterEntity2 = characterRepository.findById(idCharacter).get()
        charactersTeam1.add(characterEntity)
        charactersTeam1.add(characterEntity2)
        team1.put(characterTeam.idTeam, charactersTeam1)
        tournamentEntity.teams.remove(id)
        tournamentEntity.charactersFighting.add(id)
        return team1
    }

    private Map<Long, List<CharacterEntity>> pegarAleatorioTeam2ParaBatalhar(TournamentEntity tournamentEntity) {
        List<CharacterEntity> charactersTeam2 = []
        Map<Long, List<CharacterEntity>> team2 = new HashMap<>()
        Long id = tournamentEntity.teams.get(random.nextInt(tournamentEntity.teams.size()))
        CharacterTeamEntity characterTeam = characterTeamRepository.findById(id).get()
        Long idCharacter = characterTeam.characters.get(0)
        CharacterEntity characterEntity = characterRepository.findById(idCharacter).get()
        idCharacter = characterTeam.characters.get(1)
        CharacterEntity characterEntity2 = characterRepository.findById(idCharacter).get()
        charactersTeam2.add(characterEntity)
        charactersTeam2.add(characterEntity2)
        team2.put(characterTeam.idTeam, charactersTeam2)
        tournamentEntity.teams.remove(id)
        tournamentEntity.charactersFighting.add(id)
        return team2
    }

    private static boolean genderTournamentIsInvalid(TournamentEntity tournamentEntity) {
        if (tournamentEntity.categoryByGenre != GenderTournament.F.getCode() &&
                tournamentEntity.categoryByGenre != GenderTournament.M.getCode() &&
                tournamentEntity.categoryByGenre != GenderTournament.F_M.getCode() &&
                tournamentEntity.categoryByGenre != GenderTournament.M_F.getCode()) {
            return true
        }
        return false
    }

    private static boolean formatTournamentIsInvalid(TournamentEntity tournamentEntity) {
        if (tournamentEntity.format != FormatTournament.ONE_VS_ONE.getCode() &&
                tournamentEntity.format != FormatTournament.TWO_VS_TWO.getCode() &&
                tournamentEntity.format != FormatTournament.THREE_VS_THREE.getCode()) {
            return true
        }
        return false
    }

    private static boolean tournamentIsFinished(TournamentEntity tournamentEntity) {
        if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
            return true
        }
        return false
    }

    private static boolean tournamentIsNotFinished(TournamentEntity tournamentEntity) {
        if (tournamentEntity.status == StatusTournament.TOURNAMENT_STARTED.getCode() ||
                tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
            return true
        }
        return false
    }

    private static boolean matchIsStarted(TournamentEntity tournamentEntity) {
        if (tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode() &&
                tournamentEntity.matchIsStarted) {
            return true
        }
        return false
    }

    private void conferirDadosDoTorneioFormato1v1(TournamentEntity tournamentEntity, List<Long> idsCharactersWinners) {
        Long idCharacter = idsCharactersWinners.get(0)
        CharacterEntity characterEntity = characterRepository.findById(idCharacter).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $idCharacter")
        }
        if (tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
            if (characterEntity.gameOrigin != tournamentEntity.gameOrigin) {
                throw new TournamentRegisterException("Character id $idCharacter not equals tournament game origin")
            }
        } else if (characterEntity.gameOrigin != tournamentEntity.gameOrigin ||
                characterEntity.gender != tournamentEntity.categoryByGenre) {
            throw new TournamentRegisterException("Character id $idCharacter not equals tournament category")
        }
        tournamentEntity.winnerOfTournament = idCharacter
    }

    private void conferirDadosDoTorneioFormato2v2(TournamentEntity tournamentEntity, List<Long> idsCharactersWinners) {
        Long idCharacter1 = idsCharactersWinners.get(0)
        Long idCharacter2 = idsCharactersWinners.get(1)
        CharacterEntity characterEntity = characterRepository.findById(idCharacter1).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $idCharacter1")
        }
        CharacterEntity characterEntity2 = characterRepository.findById(idCharacter2).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $idCharacter2")
        }
        if (tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
            if (characterEntity.gameOrigin != tournamentEntity.gameOrigin) {
                throw new TournamentRegisterException("Character id $idCharacter1 not equals tournament game origin")
            } else if (characterEntity2.gameOrigin != tournamentEntity.gameOrigin) {
                throw new TournamentRegisterException("Character id $idCharacter2 not equals tournament game origin")
            }
        } else if (characterEntity.gameOrigin != tournamentEntity.gameOrigin ||
                characterEntity.gender != tournamentEntity.categoryByGenre) {
            throw new TournamentRegisterException("Character id $idCharacter1 not equals tournament game origin")
        } else if (characterEntity2.gameOrigin != tournamentEntity.gameOrigin ||
                characterEntity2.gender != tournamentEntity.categoryByGenre) {
            throw new TournamentRegisterException("Character id $idCharacter2 not equals tournament game origin")
        }
        TournamentTeamWinnerEntity teamWinnerEntity = new TournamentTeamWinnerEntity()
        teamWinnerEntity.characters.add(idCharacter1)
        teamWinnerEntity.characters.add(idCharacter2)
        teamWinnerEntity.idTournament = tournamentEntity.id
        tournamentTeamWinnerRepository.save(teamWinnerEntity)
        tournamentEntity.teamWinner = teamWinnerEntity.id
    }

    private void conferirDadosDoTorneioFormato3v3(TournamentEntity tournamentEntity, List<Long> idsCharactersWinners) {
        Long idCharacter1 = idsCharactersWinners.get(0)
        Long idCharacter2 = idsCharactersWinners.get(1)
        Long idCharacter3 = idsCharactersWinners.get(2)
        CharacterEntity characterEntity = characterRepository.findById(idCharacter1).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $idCharacter1")
        }
        CharacterEntity characterEntity2 = characterRepository.findById(idCharacter2).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $idCharacter2")
        }
        CharacterEntity characterEntity3 = characterRepository.findById(idCharacter3).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $idCharacter3")
        }
        if (tournamentEntity.categoryByGenre == GenderTournament.M_F.getCode() || tournamentEntity.categoryByGenre == GenderTournament.F_M.getCode()) {
            if (characterEntity.gameOrigin != tournamentEntity.gameOrigin) {
                throw new TournamentRegisterException("Character id $idCharacter1 not equals tournament game origin")
            } else if (characterEntity2.gameOrigin != tournamentEntity.gameOrigin) {
                throw new TournamentRegisterException("Character id $idCharacter2 not equals tournament game origin")
            } else if (characterEntity3.gameOrigin != tournamentEntity.gameOrigin) {
                throw new TournamentRegisterException("Character id $idCharacter3 not equals tournament game origin")
            }
        } else if (characterEntity.gameOrigin != tournamentEntity.gameOrigin ||
                characterEntity.gender != tournamentEntity.categoryByGenre) {
            throw new TournamentRegisterException("Character id $idCharacter1 not equals tournament category")
        } else if (characterEntity2.gameOrigin != tournamentEntity.gameOrigin ||
                characterEntity2.gender != tournamentEntity.categoryByGenre) {
            throw new TournamentRegisterException("Character id $idCharacter2 not equals tournament category")
        } else if (characterEntity3.gameOrigin != tournamentEntity.gameOrigin ||
                characterEntity3.gender != tournamentEntity.categoryByGenre) {
            throw new TournamentRegisterException("Character id $idCharacter3 not equals tournament category")
        }
        TournamentTeamWinnerEntity teamWinnerEntity = new TournamentTeamWinnerEntity()
        teamWinnerEntity.characters.add(idCharacter1)
        teamWinnerEntity.characters.add(idCharacter2)
        teamWinnerEntity.characters.add(idCharacter3)
        teamWinnerEntity.idTournament = tournamentEntity.id
        tournamentTeamWinnerRepository.save(teamWinnerEntity)
    }

    private List<CharacterEntity> verificarPersonagensTorneioTeam(TournamentEntity tournamentEntity, Long idTeam) {
        CharacterTeamEntity characterTeam = characterTeamRepository.findById(idTeam).orElseThrow {
            new RuntimeException()
        }
        Long idCharacter
        List<CharacterEntity> charactersTeam = []
            idCharacter = characterTeam.characters.get(0)
            CharacterEntity characterEntity1 = characterRepository.findById(idCharacter).get()
            idCharacter = characterTeam.characters.get(1)
            CharacterEntity characterEntity2 = characterRepository.findById(idCharacter).get()
            charactersTeam.add(characterEntity1)
            charactersTeam.add(characterEntity2)
        if (tournamentEntity.format == FormatTournament.THREE_VS_THREE.getCode()) {
            idCharacter = characterTeam.characters.get(2)
            CharacterEntity characterEntity3 = characterRepository.findById(idCharacter).get()
            charactersTeam.add(characterEntity3)
        }
        tournamentEntity.matchIsStarted = false
        tournamentEntity.teamWinnersOfRound.add(idTeam)
        tournamentEntity.charactersFighting.clear()
        return charactersTeam
    }

    private void adicionarTimesNoTorneio(TournamentEntity tournamentEntity) {
        List<CharacterTeamEntity> characterTeamEntity = characterTeamRepository.findAll()
        for (CharacterTeamEntity characterTeam: characterTeamEntity) {
            if (characterTeam.idTournament == tournamentEntity.id) {
                tournamentEntity.teams.add(characterTeam.idTeam)
            }
        }
    }
}

