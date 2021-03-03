package org.oneworldaccuracy.demo.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

/**
 * REST controller that define endpoints for CRUD operations on the User resource.
 */
@RestController
@RequestMapping("/api")
@ApiOperation(tags = "User", value = "User")
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
    @ApiOperation(value = " Creates a new user.", tags = "User", notes = "Creates a new user if the email is not already used, and sends an mail with a verification link. The user needs to be activated on creation.")
    @ApiResponses({@ApiResponse(code = 400, message = "Bad Request")})
    @PostMapping("/user")
    @ResponseStatus(CREATED)
    public UserDTO createUser(@ApiParam(name = "user", value = "The details for the user to create") @Valid @RequestBody UserRegistrationDTO userDTO) {
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
    @ApiOperation(value = "Find user by id", tags = "User")
    @ApiResponses({@ApiResponse(code = 404, message = "Not Found")})
    @GetMapping("/user/{id}")
    @ResponseStatus(OK)
    public UserDTO getUser(@ApiParam("The id of the user to find.") @PathVariable Long id) {
        return userService.getUser(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * {@code GET /users} : Get all users.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @ApiOperation(value = "Get all users", tags = "User", notes = "This endpoint returns a pageable result of users registered on the platform")
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
    @ApiOperation(value = "Updates an existing User.", tags = "User")
    @ApiResponses({@ApiResponse(code = 400, message = "Bad Request")})
    @PutMapping("/user/{id}")
    @ResponseStatus(OK)
    public UserDTO updateUser(@PathVariable Long id, @ApiParam(name = "user", value = "The user data to update") @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    /**
     * {@code DELETE /users/:id} : Deactivate user with "id"
     *
     * @param id the id of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @ApiOperation(value = "Deactivate user with id", tags = "User")
    @ApiResponses({@ApiResponse(code = 404, message = "Not Found")})
    @DeleteMapping("/user/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteUser(@ApiParam("The id of the user to delete.") @PathVariable Long id) {
        mailService.sendDeactivationEmail(userService.deactivateUser(id));
    }

    /**
     * {@code GET  /user/activate/:key} : activate the registered user.
     *
     * @param key the activation/verification key.
     * @throws BadRequestException {@code 500 (Bad request)} if the user couldn't be activated.
     */
    @ApiOperation(value = "Activate/verify the registered user.", tags = "User")
    @ApiResponses({@ApiResponse(code = 400, message = "Bad Request")})
    @GetMapping("/user/activate/{key}")
    @ResponseStatus(OK)
    public void activateAccount(@ApiParam("The user activation key.") @PathVariable String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new BadRequestException("No user was found for this activation key");
        }
        mailService.sendOnboardingEmail(user.get());
    }

    /**
     * {@code GET  /users/authorities : Fetch all the available authorities on the platform
     *
     * @return {@link Set<String>} of available authorities on the platform
     */
    @ApiOperation(value = "Fetch all the available authorities on the platform", tags = "User")
    @GetMapping("/users/authorities")
    @ResponseStatus(OK)
    public Set<String> getAuthorities() {
        return userService.getAuthorities();
    }
}
