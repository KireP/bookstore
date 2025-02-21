package com.sporty.bookstore.inventory.entity;

import com.sporty.bookstore.inventory.enumeration.BookType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "books")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@SequenceGenerator(
        name = "books_sequence_generator",
        sequenceName = "books_sequence",
        allocationSize = 1
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "books_sequence_generator")
    @Column(name = "id")
    private BigInteger id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BookType type;

    @Column(name = "creation_date")
    @CreatedDate
    private LocalDateTime creationDate;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "price")
    private Double price;
}
