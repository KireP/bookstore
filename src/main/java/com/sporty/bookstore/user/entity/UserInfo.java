package com.sporty.bookstore.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Table(name = "users")
@Setter
@Getter
@SequenceGenerator(
        name = "users_sequence_generator",
        sequenceName = "users_sequence",
        allocationSize = 1
)
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence_generator")
    private BigInteger id;
    private String username;
    private String password;
    private String roles;

}
