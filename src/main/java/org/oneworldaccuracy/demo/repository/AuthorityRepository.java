package org.oneworldaccuracy.demo.repository;

import org.oneworldaccuracy.demo.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A DAO repository for User entity
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
