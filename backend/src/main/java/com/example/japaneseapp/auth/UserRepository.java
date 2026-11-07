package com.example.japaneseapp.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    long countByRole(UserRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from AppUser account where account.role = :role order by account.id")
    List<AppUser> findAllByRoleForUpdate(@Param("role") UserRole role);
}
