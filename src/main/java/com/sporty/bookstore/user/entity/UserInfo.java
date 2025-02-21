package com.sporty.bookstore.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "users_sequence_generator",
        sequenceName = "users_sequence",
        allocationSize = 1
)
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence_generator")
    @Column(name = "id")
    private BigInteger id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "roles")
    private String roles;

}
