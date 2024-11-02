package com.example.SPTJ_BD.controller
import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.model.exception.CharacterNotFoundException
import com.example.SPTJ_BD.model.exception.InvalidFormatTournamentException
import com.example.SPTJ_BD.service.CharacterService
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

@RestController
@RequestMapping("/sptj/characters")
class CharacterController {

    private CharacterService characterService

    CharacterController(CharacterService characterService) {
        this.characterService = characterService
    }

    @PostMapping
    ResponseEntity registerCharacter(@RequestBody CharacterEntity input) {
        try {
            CharacterEntity characterEntity = characterService.createCharacter(input)
            return ResponseEntity.status(HttpStatus.CREATED).body(characterEntity)
        } catch (InvalidFormatTournamentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body([error: exception.getMessage()])
        }
    }

    @GetMapping
    List<CharacterEntity> getAllCharacters() {
        characterService.getAllCharacters()
    }

    @GetMapping("/{id}")
    ResponseEntity getCharacterById(@PathVariable("id") Long id) {
        try {
            CharacterEntity characterEntity = characterService.getCharacterById(id)
            return ResponseEntity.status(HttpStatus.OK).body(characterEntity)
        } catch (CharacterNotFoundException exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
        }
    }

    @PutMapping("/{id}")
    ResponseEntity updateCharacter(@PathVariable("id") Long id, @RequestBody CharacterEntity characterEntity) {
        try {
            CharacterEntity updatedCharacter = characterService.updateCharacter(id, characterEntity)
            return ResponseEntity.status(HttpStatus.OK).body(updatedCharacter)
        } catch (CharacterNotFoundException exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
        }
    }

    @DeleteMapping("/{id}")
    ResponseEntity deleteCharacter(@PathVariable("id") Long id) {
        try {
            CharacterEntity characterEntity = characterService.deleteCharacter(id)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(characterEntity)
        } catch (CharacterNotFoundException exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body([error: exception.message])
        }
    }

}
