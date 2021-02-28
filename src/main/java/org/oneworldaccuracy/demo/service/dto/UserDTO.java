package org.oneworldaccuracy.demo.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.oneworldaccuracy.demo.domain.Authority;
import org.oneworldaccuracy.demo.domain.User;
import org.oneworldaccuracy.demo.domain.UserStatus;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Data Transfer Object for the User Entity
 */

@Data // This annotation helps us generate boilerplate get/set/hash/equal/toString methods
@NoArgsConstructor // Generates a constructor with no arguments
public class UserDTO {
    private static final String emailRegex = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    private Long id;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email(regexp = emailRegex)
    @Size(min = 5, max = 254)
    private String email;

    private String title;

    private boolean verified = false;

    private Instant registeredDate = null;

    private Instant verifiedDate = null;

    private Instant deactivatedDate = null;

    private UserStatus status;

    private Set<String> authorities;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.title = user.getTitle();
        this.status = user.getStatus();
        this.verified = user.isVerified();
        this.deactivatedDate = user.getDeactivatedDate();
        this.registeredDate = user.getRegisteredDate();
        this.verifiedDate = user.getVerifiedDate();
        this.authorities = new HashSet<>();
        if (user.getAuthorities() != null) {
            this.authorities = user.getAuthorities().stream().map(Authority::getName).collect(toSet());
        }
    }
}
