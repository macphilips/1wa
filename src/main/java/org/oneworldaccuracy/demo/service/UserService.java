package org.oneworldaccuracy.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.oneworldaccuracy.demo.controller.errors.EmailAlreadyUsedException;
import org.oneworldaccuracy.demo.controller.errors.NotFoundException;
import org.oneworldaccuracy.demo.domain.Authority;
import org.oneworldaccuracy.demo.domain.User;
import org.oneworldaccuracy.demo.repository.AuthorityRepository;
import org.oneworldaccuracy.demo.repository.UserRepository;
import org.oneworldaccuracy.demo.service.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.oneworldaccuracy.demo.domain.UserStatus.DEACTIVATED;
import static org.oneworldaccuracy.demo.domain.UserStatus.REGISTERED;
import static org.oneworldaccuracy.demo.domain.UserStatus.VERIFIED;
import static org.oneworldaccuracy.demo.service.AuthoritiesConstants.USER;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User registerUser(UserDTO userDTO, String password) {
        Optional<User> userOptional = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());

        if (userOptional.isPresent()) {
            throw new EmailAlreadyUsedException();
        }

        User newUser = User.builder()
            .email(userDTO.getEmail().toLowerCase())
            // TODO: remove and encode actual password
            .password(RandomStringUtils.randomAlphanumeric(60))
            .title(userDTO.getTitle())
            .firstName(userDTO.getFirstName())
            .lastName(userDTO.getLastName())
            .registeredDate(Instant.now())
            .verified(false)
            .status(REGISTERED)
            .activationKey(RandomStringUtils.randomNumeric(20))
            .build();

        // new user gets registration key
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);

        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);

        return newUser;
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = this.findUser(id).orElseThrow(() -> new NotFoundException("User not found"));

        Optional.ofNullable(userDTO.getEmail())
            .flatMap(userRepository::findOneByEmailIgnoreCase)
            .ifPresent((userWithEmail) -> {
                if (!userWithEmail.equals(user)) {
                    throw new EmailAlreadyUsedException();
                }
            });
        if (Objects.nonNull(userDTO.getFirstName())) user.setFirstName(userDTO.getFirstName());
        if (Objects.nonNull(userDTO.getLastName())) user.setLastName(userDTO.getLastName());
        if (Objects.nonNull(userDTO.getEmail())) user.setEmail(userDTO.getEmail());
        if (Objects.nonNull(userDTO.getTitle())) user.setTitle(userDTO.getTitle());

        return new UserDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Optional<User> findUser(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUser(Long id) {
        return userRepository.findById(id).map(UserDTO::new);
    }

    @Transactional
    public User deactivateUser(Long id) {
        User user = this.findUser(id).orElseThrow(() -> new NotFoundException("User not found"));
        user.setDeactivatedDate(Instant.now());
        user.setStatus(DEACTIVATED);
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setVerified(true);
                user.setStatus(VERIFIED);
                user.setActivationKey(null);
                log.debug("Activated user: {}", user);
                return user;
            });
    }
}
