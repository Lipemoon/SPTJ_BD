package com.example.SPTJ_BD.controller
import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.model.InvalidFormatTournamentException
import com.example.SPTJ_BD.model.InvalidGenderException
import com.example.SPTJ_BD.model.output.ResponseChooseWinner
import com.example.SPTJ_BD.model.output.ResponseTournament
import com.example.SPTJ_BD.model.output.ResponseTournamentBattle
import com.example.SPTJ_BD.model.TournamentNotFoundException
import com.example.SPTJ_BD.service.CharacterService
import com.example.SPTJ_BD.service.TournamentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
//Todo - Fazer no Banco de Dados postgre:
//Todo - Inserir 10 registros ou mais e mostrar como que faz para mostrar um que tenha um id especifico
//Todo - Como fazer para mostrar onde tem um tipo ou outro tipo
//Todo - Como fazer para buscar tipo onde o tipo ethanol e o preco = 10
//Todo - Como fazer para buscar registros maiores que 5 e menores que 11
//Todo - Como fazer para mostrar a quantidade e o tipo de cada registro
//Todo - Como fazer para contar quantos registros temos na tabela

@RestController
@RequestMapping("/sptj/tournaments")
class TournamentController {

    private TournamentService tournamentService
    private CharacterService characterService

    TournamentController(TournamentService tournamentService, CharacterService characterService) {
        this.tournamentService = tournamentService
        this.characterService = characterService
    }

    @PostMapping
    ResponseEntity registerTournament(@RequestBody TournamentEntity input) {
        try {
            List<CharacterEntity> characters = characterService.getAllCharacters()
            TournamentEntity tournamentEntity = tournamentService.createTournament(input, characters)
            return ResponseEntity.status(HttpStatus.CREATED).body(tournamentEntity)
        } catch (InvalidGenderException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (InvalidFormatTournamentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @GetMapping
    List<TournamentEntity> getAllTournaments() {
        tournamentService.getAllTournaments()
    }

    @PostMapping("/{id}/start")
    ResponseEntity startTournament(@PathVariable("id") Long idTournament) {
        try {
            ResponseTournament responseTournament = tournamentService.startTournament(idTournament)
            return ResponseEntity.status(HttpStatus.OK).body(responseTournament)
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @PostMapping("/{id}/startMatch")
    ResponseEntity startMatchOfTournament(@PathVariable("id") Long idTournament) {
        try {
            ResponseTournamentBattle responseTournamentBattle = tournamentService.startMatchOfTournament(idTournament)
            return ResponseEntity.status(HttpStatus.OK).body(responseTournamentBattle)
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @PostMapping("/1v1/{idTournament}/chooseWinner/{idPlayer}")
    ResponseEntity chooseWinnerOfMatch1v1(@PathVariable("idTournament") Long idTournament, @PathVariable("idPlayer") Long idPlayer) {
        try {
            ResponseChooseWinner responseChooseWinner = tournamentService.chooseWinnerOfMatch1v1(idTournament, idPlayer)
            return ResponseEntity.status(HttpStatus.OK).body(responseChooseWinner)
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

//    @PostMapping("/team/{idTournament}/chooseWinner/{idTeam}")
//    ResponseEntity chooseWinnerOfMatchTeam(@PathVariable("idTournament") Long idTournament, @PathVariable("idPlayer") Long idPlayer) {
//        try {
//
//        } catch (RuntimeException exception) {
//
//        }
//    }



}
