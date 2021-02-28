package org.oneworldaccuracy.demo.repository;

import org.oneworldaccuracy.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DAO repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
