package com.example.SPTJ_BD.controller
import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.entity.TournamentEntity
import com.example.SPTJ_BD.model.exception.CharactersAreNotFightingException
import com.example.SPTJ_BD.model.exception.TournamentAlreadyFinishedException
import com.example.SPTJ_BD.model.exception.TournamentAlreadyStartedException
import com.example.SPTJ_BD.model.exception.TournamentMatchIsStartedException
import com.example.SPTJ_BD.model.exception.TournamentNotStartedException
import com.example.SPTJ_BD.model.input.TournamentFinalizedInput
import com.example.SPTJ_BD.model.exception.InvalidFormatTournamentException
import com.example.SPTJ_BD.model.exception.InvalidGenderException
import com.example.SPTJ_BD.model.output.ResponseChooseWinner
import com.example.SPTJ_BD.model.output.ResponseTournament
import com.example.SPTJ_BD.model.exception.TournamentNotFoundException
import com.example.SPTJ_BD.model.output.ResponseTournamentBattle1v1
import com.example.SPTJ_BD.model.output.ResponseTournamentBattleTeam
import com.example.SPTJ_BD.service.CharacterService
import com.example.SPTJ_BD.service.TournamentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

    @PostMapping("/finished")
    ResponseEntity registerTournamentFinished(@RequestBody TournamentFinalizedInput input) {
        try {
            TournamentEntity tournamentEntity = tournamentService.registerTournamentFinalized(input)
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

    @GetMapping("/{id}")
    ResponseEntity getTournamentById(@PathVariable("id") Long id) {
        try {
            TournamentEntity tournamentEntity = tournamentService.getTournamentById(id)
            return ResponseEntity.status(HttpStatus.OK).body(tournamentEntity)
        } catch (TournamentNotFoundException exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
        }
    }

//    @GetMapping("/{id}/characterWinner")
//    ResponseEntity getWinnerTournamentById(@PathVariable("id") Long id) {
//        try {
//            CharacterEntity characterEntity = tournamentService.getWinnerTournamentById(id)
//            return ResponseEntity.status(HttpStatus.OK).body(characterEntity)
//        } catch (TournamentNotFoundException exception) {
//            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
//        } catch (RuntimeException exception) {
//            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
//        }
//    }

//    @PutMapping("/{id}")
//    ResponseEntity updateTournament(@PathVariable("id") Long id, @RequestBody TournamentEntity tournamentEntity) {
//        try {
//            List<CharacterEntity> characters = characterService.getAllCharacters()
//            TournamentEntity updatedTournament = tournamentService.updateTournament(id, tournamentEntity, characters)
//            return ResponseEntity.status(HttpStatus.OK).body(updatedTournament)
//        } catch (TournamentNotFoundException exception) {
//            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
//        }
//    }

    @DeleteMapping("/{id}")
    ResponseEntity deleteTournament(@PathVariable("id") Long id) {
        try {
            tournamentService.deleteTournament(id)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (TournamentNotFoundException exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
        }
    }
    
    @PostMapping("/{id}/start")
    ResponseEntity startTournament(@PathVariable("id") Long idTournament) {
        try {
            ResponseTournament responseTournament = tournamentService.startTournament(idTournament)
            return ResponseEntity.status(HttpStatus.OK).body(responseTournament)
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentAlreadyStartedException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @PostMapping("/1v1/{id}/startMatch")
    ResponseEntity startMatchOfTournament1v1(@PathVariable("id") Long idTournament) {
        try {
            ResponseTournamentBattle1v1 responseTournamentBattle = tournamentService.startMatchOfTournament1v1(idTournament)
            return ResponseEntity.status(HttpStatus.OK).body(responseTournamentBattle)
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentNotStartedException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (InvalidFormatTournamentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentMatchIsStartedException exception) {
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
        } catch (TournamentNotStartedException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentAlreadyFinishedException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (CharactersAreNotFightingException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @PostMapping("/teams/{id}/startMatch")
    ResponseEntity startMatchOfTournamentTeam(@PathVariable("id") Long idTournament) {
        try {
            ResponseTournamentBattleTeam responseTournamentBattleTeam = tournamentService.startMatchOfTournamentTeam(idTournament)
            return ResponseEntity.status(HttpStatus.OK).body(responseTournamentBattleTeam)
        } catch (InvalidFormatTournamentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentNotStartedException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (TournamentMatchIsStartedException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @PostMapping("/teams/{idTournament}/chooseWinner/{idTeam}")
    ResponseEntity chooseWinnerOfMatchTeam(@PathVariable("idTournament") Long idTournament, @PathVariable("idTeam") Long idTeam) {
        try {
            ResponseChooseWinner responseChooseWinner = tournamentService.chooseWinnerOfMatchTeam(idTournament, idTeam)
            return ResponseEntity.status(HttpStatus.OK).body(responseChooseWinner)
        } catch (TournamentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (InvalidFormatTournamentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        } catch (CharactersAreNotFightingException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }


}
