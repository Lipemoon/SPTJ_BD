package com.example.SPTJ_BD.service

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.CharacterTeamEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.model.FormatTournament
import com.example.SPTJ_BD.model.GenderTournament
import com.example.SPTJ_BD.model.exception.InvalidFormatTournamentException
import com.example.SPTJ_BD.model.exception.InvalidGenderException
import com.example.SPTJ_BD.model.exception.TournamentAlreadyFinishedException
import com.example.SPTJ_BD.model.output.Battle1v1Status
import com.example.SPTJ_BD.model.output.BattleTeamStatus
import com.example.SPTJ_BD.model.output.ResponseChooseWinner
import com.example.SPTJ_BD.model.output.ResponseTournament
import com.example.SPTJ_BD.model.StatusTournament
import com.example.SPTJ_BD.model.exception.TournamentNotFoundException
import com.example.SPTJ_BD.model.output.ResponseTournamentBattle1v1
import com.example.SPTJ_BD.model.output.ResponseTournamentBattleTeam
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

    TournamentEntity createTournament(TournamentEntity tournamentEntity, List<CharacterEntity> characters) {
        if (genderTournamentIsInvalid(tournamentEntity)) {
            throw new InvalidGenderException("")
        }
        if (formatTournamentIsInvalid(tournamentEntity)) {
            throw new InvalidFormatTournamentException("")
        }
        if (tournamentEntity.format == FormatTournament.ONE_VS_ONE.getCode()) {
            addCharactersTournament1v1(tournamentEntity, characters)
        } else {
            tournamentRepository.save(tournamentEntity)
            addCharactersTournamentTeam(tournamentEntity, characters)
        }
        tournamentRepository.save(tournamentEntity)
    }

    static boolean genderTournamentIsInvalid(TournamentEntity tournamentEntity) {
        if (tournamentEntity.categoryByGenre != GenderTournament.F.getCode() &&
                tournamentEntity.categoryByGenre != GenderTournament.M.getCode() &&
                tournamentEntity.categoryByGenre != GenderTournament.F_M.getCode() &&
                tournamentEntity.categoryByGenre != GenderTournament.M_F.getCode()) {
            return true
        }
        return false
    }

    static boolean formatTournamentIsInvalid(TournamentEntity tournamentEntity) {
        if (tournamentEntity.format != FormatTournament.ONE_VS_ONE.getCode() &&
                tournamentEntity.format != FormatTournament.TWO_VS_TWO.getCode() &&
                tournamentEntity.format != FormatTournament.THREE_VS_THREE.getCode()) {
            return true
        }
        return false
    }

    TournamentEntity getTournamentById(Long id) {
        tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
    }

    CharacterEntity getWinnerTournamentById(Long id) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(id).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id $id")
        }
        if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
            CharacterEntity characterEntity = characterRepository.findById(tournamentEntity.winnerOfTournament).get()
            return characterEntity
        } else {
            throw new RuntimeException("Esse torneio id $id não foi finalizado!")
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
        } else {
            addCharactersTournamentTeam(tournamentEntity, characters)
        }
        tournamentRepository.save(updateTournament)
    }

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
            return new ResponseTournament(message: "Torneio Iniciado com sucesso!",
                    id: tournamentEntity.id, name: tournamentEntity.name,
                    gameName: tournamentEntity.gameOrigin,
                    status: tournamentEntity.status,
                    format: tournamentEntity.format)
        } else {
            throw new RuntimeException("Torneio já iniciado!")
        }
    }

    ResponseTournamentBattle1v1 startMatchOfTournament1v1(Long idTournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.format == FormatTournament.ONE_VS_ONE.getCode()) {
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
                throw new TournamentAlreadyFinishedException("Tournament already finished!")
            }
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode() &&
                    tournamentEntity.matchIsStarted) {
                throw new RuntimeException("Você não pode iniciar outra partida sem " +
                        "primeiro escolher um personagem deste torneio para ganhar!")
            } else if (tournamentEntity.status == StatusTournament.TOURNAMENT_STARTED.getCode() ||
                    tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
                Battle1v1Status battleStatus = start1v1Battle(tournamentEntity)
                if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
                    CharacterEntity characterEntity = characterRepository.findById(tournamentEntity.winnerOfTournament).get()
                    return new ResponseTournamentBattle1v1(message: "Torneio Finalizado, Campeão " +
                            "${characterEntity.name}", player1: null,
                            player2: null, statusTournament: tournamentEntity.status)
                } else {
                    return new ResponseTournamentBattle1v1(message: "Confronto Iniciado", player1:
                            battleStatus.player1, player2: battleStatus.player2,
                            statusTournament: battleStatus.statusTournament)
                }
            } else {
                throw new RuntimeException("Torneio não foi Iniciado primeiro!")
            }
        } else {
            throw new InvalidFormatTournamentException("Torneio que você tentou iniciar tem outro formato!")
        }
    }

    ResponseChooseWinner chooseWinnerOfMatch1v1(Long idTournament, Long idPlayer) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
            throw new TournamentAlreadyFinishedException("Tournament already finished!")
        } else if (tournamentEntity.status == StatusTournament.TOURNAMENT_STARTED.getCode() ||
                tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
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
        }
    }

    ResponseChooseWinner chooseWinnerOfMatchTeam(Long idTournament, Long idTeam) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(idTournament).orElseThrow {
            new TournamentNotFoundException("Tournament not found with id: $idTournament")
        }
        if (tournamentEntity.format == FormatTournament.TWO_VS_TWO.getCode()) {
            for (Long idTeams : tournamentEntity.charactersFighting.findAll()) {
                if (idTeams == idTeam) {
                    CharacterTeamEntity characterTeam = characterTeamRepository.findById(idTeam).get()
                    Long idCharacter = characterTeam.characters.get(0)
                    CharacterEntity characterEntity1 = characterRepository.findById(idCharacter).get()
                    idCharacter = characterTeam.characters.get(1)
                    CharacterEntity characterEntity2 = characterRepository.findById(idCharacter).get()
                    tournamentEntity.matchIsStarted = false
                    tournamentEntity.teamWinnersOfRound.add(idTeam)
                    tournamentEntity.charactersFighting.clear()
                    tournamentRepository.save(tournamentEntity)
                    return new ResponseChooseWinner(message: "Team com os Personagens" +
                            "${characterEntity1.name} e ${characterEntity2.name} ganhou!")
                }
            }
            throw new RuntimeException("Id player nao existe na tabela characters Fighting!")
        } else if (tournamentEntity.format == FormatTournament.THREE_VS_THREE.getCode()) {
            for (Long idTeams : tournamentEntity.charactersFighting.findAll()) {
                if (idTeams == idTeam) {
                    CharacterTeamEntity characterTeam = characterTeamRepository.findById(idTeam).get()
                    Long idCharacter = characterTeam.characters.get(0)
                    CharacterEntity characterEntity1 = characterRepository.findById(idCharacter).get()
                    idCharacter = characterTeam.characters.get(1)
                    CharacterEntity characterEntity2 = characterRepository.findById(idCharacter).get()
                    idCharacter = characterTeam.characters.get(2)
                    CharacterEntity characterEntity3 = characterRepository.findById(idCharacter).get()
                    tournamentEntity.matchIsStarted = false
                    tournamentEntity.teamWinnersOfRound.add(idTeam)
                    tournamentEntity.charactersFighting.clear()
                    tournamentRepository.save(tournamentEntity)
                    return new ResponseChooseWinner(message: "Team com os Personagens" +
                            "${characterEntity1.name}, ${characterEntity2.name} e " +
                            "${characterEntity3.name} ganhou!")
                }
            }
            throw new RuntimeException("Id player nao existe na tabela characters Fighting!")
        }
        throw new InvalidFormatTournamentException("Torneio que você tentou iniciar tem outro formato!")
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
                for (int i = 1; i <= contadorIdTeams; i++) {
                    CharacterTeamEntity characterTeam = characterTeamRepository.findById(i).get()
                    if (characterTeam.idTournament == tournamentEntity.id) {
                        tournamentEntity.teams.add(characterTeam.idTeam)
                    }
                }
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
                for (int i = 1; i <= contadorIdTeams; i++) {
                    CharacterTeamEntity characterTeam = characterTeamRepository.findById(i).get()
                    if (characterTeam.idTournament == tournamentEntity.id) {
                        tournamentEntity.teams.add(characterTeam.idTeam)
                    }
                }
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
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
                throw new TournamentAlreadyFinishedException("Tournament already finished!")
            }
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode() &&
                    tournamentEntity.matchIsStarted) {
                throw new RuntimeException("Já tem uma partida em andamento!")
            }
            if (tournamentEntity.status == StatusTournament.TOURNAMENT_STARTED.getCode() ||
                    tournamentEntity.status == StatusTournament.TOURNAMENT_PROGRESS.getCode()) {
                BattleTeamStatus battleTeamStatus = startTeamBattle(tournamentEntity)
                if (tournamentEntity.status == StatusTournament.TOURNAMENT_FINALIZED.getCode()) {
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
            tournamentEntity.teamWinner = tournamentEntity.teamWinnersOfRound.get(0)
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

}
