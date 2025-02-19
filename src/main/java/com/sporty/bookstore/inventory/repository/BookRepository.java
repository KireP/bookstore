package com.sporty.bookstore.inventory.repository;

import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import com.sporty.bookstore.inventory.repository.specification.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

@Repository
@Transactional(readOnly = true)
public interface BookRepository extends JpaRepository<Book, BigInteger>, JpaSpecificationExecutor<Book> {

    default Page<Book> searchBooks(Collection<BigInteger> ids,
                                   Collection<BookType> types,
                                   LocalDateTime creationDateFrom,
                                   LocalDateTime creationDateTo,
                                   String title,
                                   String author,
                                   Double priceFrom,
                                   Double priceTo,
                                   Pageable pageable) {
        return findAll(
                BookSpecification.getGenericSearchSpecification(
                        ids,
                        types,
                        creationDateFrom,
                        creationDateTo,
                        title,
                        author,
                        priceFrom,
                        priceTo
                ),
                pageable
        );
    }
}
