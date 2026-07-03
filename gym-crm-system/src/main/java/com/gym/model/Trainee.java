package com.gym.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = true)
public class Trainee extends User {

    private LocalDate dateOfBirth;

    private String address;
}
