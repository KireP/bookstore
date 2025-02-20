package com.sporty.bookstore.purchase.entity;

import com.sporty.bookstore.user.entity.UserInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Table(name = "user_loyalty_points")
@Setter
@Getter
@SequenceGenerator(
        name = "user_loyalty_points_sequence_generator",
        sequenceName = "user_loyalty_points_sequence",
        allocationSize = 1
)
public class UserLoyaltyPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_loyalty_points_sequence_generator")
    @Column(name = "id")
    private BigInteger id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints;
}
