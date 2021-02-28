package org.oneworldaccuracy.demo.controller;

import org.oneworldaccuracy.demo.controller.errors.BadRequestException;
import org.oneworldaccuracy.demo.controller.errors.NotFoundException;
import org.oneworldaccuracy.demo.controller.utils.PaginationUtil;
import org.oneworldaccuracy.demo.domain.User;
import org.oneworldaccuracy.demo.service.MailService;
import org.oneworldaccuracy.demo.service.UserService;
import org.oneworldaccuracy.demo.service.dto.PageableResultDTO;
import org.oneworldaccuracy.demo.service.dto.UserDTO;
import org.oneworldaccuracy.demo.service.dto.UserRegistrationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

/**
 * REST controller that define endpoints for CRUD operations on the User resource.
 */
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final MailService mailService;

    public UserController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * {@code POST  /users}  : Creates a new user.
     * <p>
     * Creates a new user if the email is not already used, and sends an
     * mail with a verification link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link UserDTO} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the email is already in use.
     * @throws BadRequestException {@code 400 (Bad Request)} if the email is already in use.
     */
    @PostMapping("/user")
    @ResponseStatus(CREATED)
    public UserDTO createUser(@Valid @RequestBody UserRegistrationDTO userDTO) {
        if (userDTO.getId() != null) throw new BadRequestException("Use PUT request to update entity");
        User user = userService.registerUser(userDTO, userDTO.getPassword());
        mailService.sendVerifyEmail(user);
        return new UserDTO(user);
    }

    /**
     * {@code GET /users/:id} : get the "id" user.
     *
     * @param id the id of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "id" user, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/user/{id}")
    @ResponseStatus(OK)
    public UserDTO getUser(@PathVariable Long id) {
        return userService.getUser(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * {@code GET /users} : get all users.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    @ResponseStatus(OK)
    public ResponseEntity<PageableResultDTO<User>> getUsers(Pageable pageable) {
        final Page<User> page = userService.getUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(new PageableResultDTO<>(page), headers, OK);
    }

    /**
     * {@code PUT /user/:id} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user, or with status {@code 400 (Bad Request)} if the email is already in use.
     */
    @PutMapping("/user/{id}")
    public UserDTO updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    /**
     * {@code DELETE /users/:id} : Deactivate user with "id"
     *
     * @param id the id of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/user/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        mailService.sendDeactivationEmail(userService.deactivateUser(id));
    }

    /**
     * {@code GET  /activate/:key} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 400 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/user/activate/{key}")
    public void activateAccount(@PathVariable String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new BadRequestException("No user was found for this activation key");
        }
    }
}
