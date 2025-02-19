package com.sporty.bookstore.inventory.repository.specification;

import com.sporty.bookstore.inventory.entity.Book;
import com.sporty.bookstore.inventory.enumeration.BookType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

public final class BookSpecification {

    private BookSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Book> getGenericSearchSpecification(Collection<BigInteger> ids,
                                                                    Collection<BookType> types,
                                                                    LocalDateTime creationDateFrom,
                                                                    LocalDateTime creationDateTo,
                                                                    String title,
                                                                    String author,
                                                                    Double priceFrom,
                                                                    Double priceTo) {
        return byIds(ids)
                .and(byTypes(types))
                .and(byCreationDateFrom(creationDateFrom))
                .and(byCreationDateTo(creationDateTo))
                .and(byTitle(title))
                .and(byAuthor(author))
                .and(byPriceFrom(priceFrom))
                .and(byPriceTo(priceTo));
    }

    public static Specification<Book> byIds(Collection<BigInteger> ids) {
        return getConditionalSpecification(
                CollectionUtils.isNotEmpty(ids),
                (root, query, criteriaBuilder) -> root.get(Book.Fields.id).in(ids)
        );
    }

    public static Specification<Book> byTypes(Collection<BookType> types) {
        return getConditionalSpecification(
                CollectionUtils.isNotEmpty(types),
                (root, query, criteriaBuilder) -> root.get(Book.Fields.type).in(types)
        );
    }

    public static Specification<Book> byCreationDateFrom(LocalDateTime creationDateFrom) {
        return getConditionalSpecification(
                Objects.nonNull(creationDateFrom),
                (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                        root.get(Book.Fields.creationDate),
                        creationDateFrom
                )
        );
    }

    public static Specification<Book> byCreationDateTo(LocalDateTime creationDateTo) {
        return getConditionalSpecification(
                Objects.nonNull(creationDateTo),
                (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                        root.get(Book.Fields.creationDate),
                        creationDateTo
                )
        );
    }

    public static Specification<Book> byTitle(String title) {
        return getConditionalSpecification(
                StringUtils.isNotBlank(title),
                (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(Book.Fields.title)),
                        "%" + title.toLowerCase() + "%"
                )
        );
    }

    public static Specification<Book> byAuthor(String author) {
        return getConditionalSpecification(
                StringUtils.isNotBlank(author),
                (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(Book.Fields.author)),
                        "%" + author.toLowerCase() + "%"
                )
        );
    }

    public static Specification<Book> byPriceFrom(Double priceFrom) {
        return getConditionalSpecification(
                Objects.nonNull(priceFrom),
                (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                        root.get(Book.Fields.price),
                        priceFrom
                )
        );
    }

    public static Specification<Book> byPriceTo(Double priceTo) {
        return getConditionalSpecification(
                Objects.nonNull(priceTo),
                (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                        root.get(Book.Fields.price),
                        priceTo
                )
        );
    }

    private static Specification<Book> getConditionalSpecification(boolean condition, Specification<Book> originalSpecification) {
        return (root, query, criteriaBuilder) -> {
            if (condition) {
                return originalSpecification.toPredicate(root, query, criteriaBuilder);
            } else {
                return criteriaBuilder.conjunction();
            }
        };
    }
}
