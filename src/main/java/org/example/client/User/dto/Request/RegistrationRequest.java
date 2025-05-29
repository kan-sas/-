package org.example.client.User.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.client.core.common.dto.UserRoleEnum;

import javax.management.relation.Role;

@Data
@AllArgsConstructor
public class RegistrationRequest {

    private String username;
    private String password;

    private String firstName;
    private String lastName;

    private UserRoleEnum role;

}


