package com.justin.modelops.model.repository;

import com.justin.modelops.model.dto.ModelFilter;
import com.justin.modelops.model.entity.AiModel;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds dynamic JPA specifications from a {@link ModelFilter}, ignoring null fields.
 */
public final class AiModelSpecifications {

    private AiModelSpecifications() {
    }

    public static Specification<AiModel> withFilter(ModelFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.modality() != null) {
                predicates.add(cb.equal(root.get("modality"), filter.modality()));
            }
            if (filter.formatType() != null) {
                predicates.add(cb.equal(root.get("format").get("type"), filter.formatType()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (StringUtils.hasText(filter.keyword())) {
                String like = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("family")), like),
                        cb.like(cb.lower(root.get("provider")), like)));
            }
            if (filter.tagId() != null) {
                query.distinct(true);
                predicates.add(cb.equal(root.join("tags", JoinType.INNER).get("id"), filter.tagId()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
