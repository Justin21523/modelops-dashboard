package com.justin.modelops.benchmark.dto;

import com.justin.modelops.benchmark.enums.BenchmarkType;

/**
 * Optional query filters for the benchmark list endpoint. Any null field is ignored.
 *
 * @param keyword case-insensitive match against name and description
 */
public record BenchmarkFilter(BenchmarkType type, String keyword, Long tagId) {
}
