package com.gym.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(exclude = "password")
public abstract class User {

    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private boolean isActive;

    private UUID userId;
}
