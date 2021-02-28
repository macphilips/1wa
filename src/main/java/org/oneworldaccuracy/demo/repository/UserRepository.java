package org.oneworldaccuracy.demo.repository;

import org.oneworldaccuracy.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A DAO repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByEmailIgnoreCase(String email);

    Optional<User> findOneByActivationKey(String key);
}
