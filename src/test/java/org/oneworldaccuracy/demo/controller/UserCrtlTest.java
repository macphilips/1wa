package org.oneworldaccuracy.demo.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneworldaccuracy.demo.DemoApplication;
import org.oneworldaccuracy.demo.domain.Authority;
import org.oneworldaccuracy.demo.domain.User;
import org.oneworldaccuracy.demo.domain.UserStatus;
import org.oneworldaccuracy.demo.repository.UserRepository;
import org.oneworldaccuracy.demo.service.dto.UserDTO;
import org.oneworldaccuracy.demo.service.dto.UserRegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.oneworldaccuracy.demo.service.AuthoritiesConstants.ADMIN;
import static org.oneworldaccuracy.demo.service.AuthoritiesConstants.USER;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
class UserCrtlTest {
    private static final String DEFAULT_TITLE = "Mr";
    private static final String UPDATED_TITLE = "Mrs";

    private static final Long DEFAULT_ID = 1L;

    private static final String DEFAULT_PASSWORD = "p@$$W0rd!";
    private static final String UPDATED_PASSWORD = "p@$$W0rd";

    private static final String DEFAULT_EMAIL = "johndoe@local.host";
    private static final String UPDATED_EMAIL = "john.doe@local.host";

    private static final String DEFAULT_FIRSTNAME = "John";
    private static final String UPDATED_FIRSTNAME = "Jane";

    private static final String DEFAULT_LASTNAME = "Doe";
    private static final String UPDATED_LASTNAME = "Doo";

    private static final UserStatus DEFAULT_STATUS = UserStatus.REGISTERED;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc restUserMockMvc;

    public User createUser() {
        return User.builder()
            .title(DEFAULT_TITLE)
            .firstName(DEFAULT_FIRSTNAME)
            .lastName(DEFAULT_LASTNAME)
            .email(DEFAULT_EMAIL)
            .title(DEFAULT_TITLE)
            .status(DEFAULT_STATUS)
            .authorities(new HashSet<>(Collections.singletonList(new Authority(USER))))
            .verified(true)
            .password(RandomStringUtils.random(60))
            .build();
    }

    @BeforeEach
    public void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldAssertThatEndpointFetchesUserById() throws Exception {
        User user = createUser();
        user = userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/user/{id}", user.getId())
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));
    }

    @Test
    public void shouldFailWith404ErrorWhenUserToFetchDoesNotExist() throws Exception {
        restUserMockMvc.perform(get("/api/user/12356789")
            .contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    public void shouldAssertThatEndpointRegistersUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();
        // should create a new user
        UserRegistrationDTO registerUser = new UserRegistrationDTO();
        registerUser.setTitle(DEFAULT_TITLE);
        registerUser.setFirstName(DEFAULT_FIRSTNAME);
        registerUser.setLastName(DEFAULT_LASTNAME);
        registerUser.setEmail(DEFAULT_EMAIL);
        registerUser.setPassword(DEFAULT_PASSWORD);
        HashSet<String> authorities = new HashSet<>();
        authorities.add(USER);
        authorities.add(ADMIN);
        registerUser.setAuthorities(authorities);

        restUserMockMvc.perform(post("/api/user")
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(registerUser)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.status").value(UserStatus.REGISTERED.toString()))
            .andExpect(jsonPath("$.verified").value(false))
            .andExpect(jsonPath("$.authorities").value(hasItems(USER, ADMIN)))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));

        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
    }

    @Test
    public void shouldAssertThatEndpointRegistersUserWithoutAuthorityDefaultsToRole_User() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();
        // should create a new user
        UserRegistrationDTO registerUser = new UserRegistrationDTO();
        registerUser.setTitle(DEFAULT_TITLE);
        registerUser.setFirstName(DEFAULT_FIRSTNAME);
        registerUser.setLastName(DEFAULT_LASTNAME);
        registerUser.setEmail(DEFAULT_EMAIL);
        registerUser.setPassword(DEFAULT_PASSWORD);

        restUserMockMvc.perform(post("/api/user")
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(registerUser)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.status").value(UserStatus.REGISTERED.toString()))
            .andExpect(jsonPath("$.verified").value(false))
            .andExpect(jsonPath("$.authorities").value(hasItems(USER)))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));

        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenRegisterEndpointReceivesInvalidRoleOrAuthority() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();
        // should create a new user
        UserRegistrationDTO registerUser = new UserRegistrationDTO();
        registerUser.setTitle(DEFAULT_TITLE);
        registerUser.setFirstName(DEFAULT_FIRSTNAME);
        registerUser.setLastName(DEFAULT_LASTNAME);
        registerUser.setEmail(DEFAULT_EMAIL);
        registerUser.setPassword(DEFAULT_PASSWORD);
        HashSet<String> authorities = new HashSet<>();
        authorities.add(USER);
        authorities.add("INVALID_ROLE");
        registerUser.setAuthorities(authorities);

        restUserMockMvc.perform(post("/api/user")
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(registerUser)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.detail").value("Invalid authority INVALID_ROLE"));

        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenRegisterEndpointReceivesDataWithAnIdField() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        UserRegistrationDTO registerUser = new UserRegistrationDTO();
        registerUser.setId(DEFAULT_ID);
        registerUser.setTitle(DEFAULT_TITLE);
        registerUser.setFirstName(DEFAULT_FIRSTNAME);
        registerUser.setLastName(DEFAULT_LASTNAME);
        registerUser.setEmail(DEFAULT_EMAIL);
        registerUser.setPassword(DEFAULT_PASSWORD);

        restUserMockMvc.perform(post("/api/user")
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(registerUser)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Use PUT request to update entity"));

        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenRegisterEndpointReceivesEmailThatAlreadyInUse() throws Exception {
        User user = createUser();
        userRepository.saveAndFlush(user);

        int databaseSizeBeforeCreate = userRepository.findAll().size();

        UserRegistrationDTO registerUser = new UserRegistrationDTO();
        registerUser.setTitle(DEFAULT_TITLE);
        registerUser.setFirstName(DEFAULT_FIRSTNAME);
        registerUser.setLastName(DEFAULT_LASTNAME);
        registerUser.setEmail(DEFAULT_EMAIL);
        registerUser.setPassword(DEFAULT_PASSWORD);

        restUserMockMvc.perform(post("/api/user")
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(registerUser)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Email is already in use!"));

        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void shouldAssertThatUpdateEndpointUpdatesExistingUser() throws Exception {
        User user = createUser();
        user = userRepository.saveAndFlush(user);

        UserRegistrationDTO updateUser = new UserRegistrationDTO();
        updateUser.setTitle(UPDATED_TITLE);
        updateUser.setFirstName(UPDATED_FIRSTNAME);
        updateUser.setLastName(UPDATED_LASTNAME);
        updateUser.setEmail(UPDATED_EMAIL);
        updateUser.setPassword(UPDATED_PASSWORD);
        updateUser.setVerified(false);

        HashSet<String> authorities = new HashSet<>();
        authorities.add(ADMIN);
        updateUser.setAuthorities(authorities);

        restUserMockMvc.perform(put("/api/user/{id}", user.getId())
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updateUser)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.firstName").value(UPDATED_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(UPDATED_LASTNAME))
            .andExpect(jsonPath("$.email").value(UPDATED_EMAIL))
            .andExpect(jsonPath("$.status").value(UserStatus.REGISTERED.toString()))
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.authorities").value(hasSize(1)))
            .andExpect(jsonPath("$.authorities").value(hasItem(ADMIN)))
            .andExpect(jsonPath("$.title").value(UPDATED_TITLE));
    }

    @Test
    public void shouldFailWithBadRequestErrorWhenUpdateEndpointReceivesEmailThatAlreadyInUseAndDoesNotBelongUserWithTheId() throws Exception {
        User user = createUser();
        user = userRepository.saveAndFlush(user);

        User anotherUser = createUser();
        anotherUser.setEmail("existing-user@local.host");
        anotherUser.setFirstName("Jack");
        anotherUser.setLastName("Ma");
        userRepository.saveAndFlush(anotherUser);

        UserRegistrationDTO updateUser = new UserRegistrationDTO();
        updateUser.setTitle(UPDATED_TITLE);
        updateUser.setFirstName(UPDATED_FIRSTNAME);
        updateUser.setLastName(UPDATED_LASTNAME);
        updateUser.setEmail("existing-user@local.host");
        updateUser.setPassword(UPDATED_PASSWORD);
        updateUser.setVerified(false);

        restUserMockMvc.perform(put("/api/user/{id}", user.getId())
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updateUser)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.detail").value("Email is already in use!"));
    }

    @Test
    public void shouldFailWith404ErrorWhenUserToUpdateDoesNotExist() throws Exception {
        restUserMockMvc.perform(put("/api/user/12356789")
            .contentType(APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(new UserDTO(createUser()))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    public void shouldAssertThatEndpointDeactivatesExistingUser() throws Exception {
        User user = createUser();
        user = userRepository.saveAndFlush(user);

        Optional<User> byId = userRepository.findById(user.getId());
        assertThat(byId.get().getStatus()).isEqualTo(UserStatus.REGISTERED);
        assertThat(Objects.isNull(byId.get().getDeactivatedDate())).isTrue();

        restUserMockMvc.perform(delete("/api/user/{id}", user.getId())
            .contentType(APPLICATION_JSON))
            .andExpect(status().isNoContent());

        byId = userRepository.findById(user.getId());
        assertThat(byId.get().getStatus()).isEqualTo(UserStatus.DEACTIVATED);
        assertThat(byId.get().getStatus()).isEqualTo(UserStatus.DEACTIVATED);
        assertThat(Objects.isNull(byId.get().getDeactivatedDate())).isFalse();
    }

    @Test
    public void shouldFailWith404ErrorWhenUserToDeleteDoesNotExist() throws Exception {
        restUserMockMvc.perform(delete("/api/user/12356789")
            .contentType(APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    public void shouldAssertThatEndpointFetchAllUsers() throws Exception {
        User user = createUser();
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/users?sort=id,desc")
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.results.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.results.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.results.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    @Test
    public void shouldAssertThatEndpointActivateUserWithValidKey() throws Exception {
        User user = createUser();
        String activationKey = RandomStringUtils.randomAlphabetic(20);
        user.setEmail("activate-user@local.host");
        user.setActivationKey(activationKey);
        user.setVerified(false);
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/user/activate/{key}", activationKey))
            .andExpect(status().isOk());

        user = userRepository.findOneByEmailIgnoreCase("activate-user@local.host").orElse(null);
        assertThat(user.isVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.VERIFIED);
        assertThat(Objects.isNull(user.getVerifiedDate())).isFalse();
    }

    @Test
    public void shouldFailWithBadRequestWhenActivationKeyDoesNotExists() throws Exception {
        User user = createUser();
        String activationKey = RandomStringUtils.randomAlphabetic(20);
        user.setEmail("activate-user@local.host");
        user.setActivationKey(activationKey);
        user.setVerified(false);
        userRepository.saveAndFlush(user);

        restUserMockMvc.perform(get("/api/user/activate/{key}", "activationKey"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAssertThatEndpointFetchAllAuthorities() throws Exception {
        restUserMockMvc.perform(get("/api/users/authorities")
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*]").value(hasItems(USER, ADMIN)));
    }
}
