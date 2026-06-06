package com.justin.modelops.benchmark.repository;

import com.justin.modelops.benchmark.dto.BenchmarkFilter;
import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds dynamic JPA specifications from a {@link BenchmarkFilter}, ignoring null fields.
 */
public final class BenchmarkSpecifications {

    private BenchmarkSpecifications() {
    }

    public static Specification<BenchmarkDefinition> withFilter(BenchmarkFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (StringUtils.hasText(filter.keyword())) {
                String like = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (filter.tagId() != null) {
                query.distinct(true);
                predicates.add(cb.equal(root.join("tags", JoinType.INNER).get("id"), filter.tagId()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
