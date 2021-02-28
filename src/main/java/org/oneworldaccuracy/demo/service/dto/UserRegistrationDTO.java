package org.oneworldaccuracy.demo.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.oneworldaccuracy.demo.validator.ValidPassword;

/**
 * Data Transfer Object for the Create/Register User
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRegistrationDTO extends UserDTO {
    public static final int PASSWORD_MIN_LENGTH = 4;
    public static final int PASSWORD_MAX_LENGTH = 100;

    @ValidPassword
    private String password;
}
