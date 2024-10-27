package com.example.SPTJ_BD.service
import com.example.SPTJ_BD.entity.CharacterEntity
import com.example.SPTJ_BD.model.GenderCharacter
import com.example.SPTJ_BD.model.InvalidGenderException
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

    CharacterEntity createCharacter(CharacterEntity input) {
        if (input.gender == GenderCharacter.F.getCode() ||
                input.gender == GenderCharacter.M.getCode()) {
                characterRepository.save(input)
        } else {
            throw new InvalidGenderException("Invalid Character Gender!")
        }
    }

}
