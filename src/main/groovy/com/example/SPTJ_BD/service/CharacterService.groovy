package com.example.SPTJ_BD.service

import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.model.Enum.GenderCharacter
import com.example.SPTJ_BD.model.exception.CharacterNotFoundException
import com.example.SPTJ_BD.model.exception.InvalidGenderException
import com.example.SPTJ_BD.repository.CharacterRepository
import org.springframework.stereotype.Service

@Service
class CharacterService {

    private CharacterRepository characterRepository

    CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository
    }

    List<CharacterEntity> getAllCharacters() {
        return characterRepository.findAll()
    }

    CharacterEntity getCharacterById(Long id) {
        characterRepository.findById(id).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $id")
        }
    }

    CharacterEntity createCharacter(CharacterEntity input) {
        if (genderCharacterIsValid(input)) {
            characterRepository.save(input)
        } else {
            throw new InvalidGenderException("Invalid Character Gender!")
        }
    }

    static boolean genderCharacterIsValid(CharacterEntity input) {
        if (input.gender == GenderCharacter.F.getCode() ||
                input.gender == GenderCharacter.M.getCode()) {
            return true
        }
        return false
    }

    CharacterEntity updateCharacter(Long id, CharacterEntity characterEntity) {
        CharacterEntity updateCharacter = characterRepository.findById(id).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $id")
        }
        updateCharacter.name = characterEntity.name
        updateCharacter.gender = characterEntity.gender
        updateCharacter.gameOrigin = characterEntity.gameOrigin
        characterRepository.save(updateCharacter)
    }

    void deleteCharacter(Long id) {
        CharacterEntity characterEntity = characterRepository.findById(id).orElseThrow {
            new CharacterNotFoundException("Character not found with id: $id")
        }
        characterRepository.save(characterEntity)
    }

}
