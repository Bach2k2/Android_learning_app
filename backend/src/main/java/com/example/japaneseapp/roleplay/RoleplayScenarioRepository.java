package com.example.japaneseapp.roleplay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleplayScenarioRepository extends JpaRepository<RoleplayScenario, String> {
    List<RoleplayScenario> findAllByCharacter_IdOrderByTitleAsc(String characterId);
    Optional<RoleplayScenario> findByIdAndCharacter_Id(String id, String characterId);
}
