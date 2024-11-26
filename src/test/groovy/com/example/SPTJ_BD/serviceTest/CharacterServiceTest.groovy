package com.example.SPTJ_BD.serviceTest
import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.model.exception.CharacterNotFoundException
import com.example.SPTJ_BD.repository.CharacterRepository
import com.example.SPTJ_BD.service.CharacterService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


class CharacterServiceTest {

    CharacterRepository characterRepository = Mockito.mock(CharacterRepository)
    CharacterService characterService = new CharacterService(characterRepository)

    @Test
    void shouldCreateCharacterWhenGenderMasculineIsValid() {
        // Arrange - Entrada
        CharacterEntity characterEntity = new CharacterEntity(
                id: 1, name: "Naruto", gender: "M", gameOrigin: "Naruto 4"
        )
        // Act - Ação
        Boolean genderValid = characterService.genderCharacterIsValid(characterEntity)
        // Assert - Resultado esperado
        assertTrue(genderValid, "The gender M should be valid for the character.")
    }

    @Test
    void shouldCreateCharacterWhenGenderFeminineIsValid() {
        CharacterEntity characterEntity = new CharacterEntity(id: 2, name: "Hinata", gender: "F", gameOrigin: "Naruto 4")
        Boolean genderValid = characterService.genderCharacterIsValid(characterEntity)
        assertTrue(genderValid, "The gender F should be valid for the character.")
    }

    @Test
    void notCreateCharacterWhenGenderFeminineIsInvalid() {
        CharacterEntity characterEntity = new CharacterEntity(id: 2, name: "Hinata", gender: "FF", gameOrigin: "Naruto 4")
        Boolean genderValid = characterService.genderCharacterIsValid(characterEntity)
        assertFalse(genderValid, "The gender FF should be Invalid for the character.")
    }

    @Test
    void notCreateCharacterWhenGenderMasculineIsInvalid() {
        CharacterEntity characterEntity = new CharacterEntity(id: 1, name: "Naruto", gender: "MM", gameOrigin: "Naruto 4")
        Boolean genderValid = characterService.genderCharacterIsValid(characterEntity)
        assertFalse(genderValid, "The gender MM should be Invalid for the character.")
    }

    @Test
    void DevoDeletarUmPersonagemPorId() {
        CharacterEntity characterEntity = new CharacterEntity(
                id: 1, name: "Pedrão", gender: "M", gameOrigin: "Os cara de pau"
        )

        when(characterRepository.findById(3)).thenReturn(Optional.of(characterEntity))
        characterService.deleteCharacter(3)

        verify(characterRepository, times(1)).delete(characterEntity)
    }

}
