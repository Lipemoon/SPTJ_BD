package com.example.SPTJ_BD.controller

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.model.InvalidFormatTournamentException
import com.example.SPTJ_BD.service.CharacterService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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



}
