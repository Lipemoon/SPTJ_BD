package com.example.SPTJ_BD.serviceTest

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.service.CharacterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito

import static org.junit.jupiter.api.Assertions.assertTrue

class CharacterServiceTest {

    CharacterRepository characterRepository = Mockito.mock(CharacterRepository)
    CharacterService characterService = new CharacterService(characterRepository)

    @Test
    void shouldCreateCharacterWhenGenderMasculineIsValid() {
        // Arrange
        CharacterEntity characterEntity = new CharacterEntity(id: 1, name: "Naruto", gender: "M", gameOrigin: "Naruto 4")

        // Act
        Boolean genderValid = characterService.genderCharacterIsValid(characterEntity)

        // Assert
        assertTrue(genderValid, "The gender M should be valid for the character.")
    }

    @Test
    void shouldCreateCharacterWhenGenderFeminineIsValid() {
        // Arrange
        CharacterEntity characterEntity = new CharacterEntity(id: 2, name: "Hinata", gender: "F", gameOrigin: "Naruto 4")

        // Act
        Boolean genderValid = characterService.genderCharacterIsValid(characterEntity)

        // Assert
        assertTrue(genderValid, "The gender F should be valid for the character.")
    }


}
