package com.justin.modelops.evaluation.repository;

import com.justin.modelops.evaluation.dto.EvaluationFilter;
import com.justin.modelops.evaluation.entity.EvaluationRecord;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds dynamic JPA specifications from an {@link EvaluationFilter}, ignoring null fields.
 */
public final class EvaluationRecordSpecifications {

    private EvaluationRecordSpecifications() {
    }

    public static Specification<EvaluationRecord> withFilter(EvaluationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.modelId() != null) {
                predicates.add(cb.equal(root.get("model").get("id"), filter.modelId()));
            }
            if (filter.benchmarkId() != null) {
                predicates.add(cb.equal(root.get("benchmark").get("id"), filter.benchmarkId()));
            }
            if (filter.hardwareProfileId() != null) {
                predicates.add(cb.equal(root.get("hardwareProfile").get("id"), filter.hardwareProfileId()));
            }
            if (filter.runtimeBackendId() != null) {
                predicates.add(cb.equal(root.get("runtimeBackend").get("id"), filter.runtimeBackendId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
