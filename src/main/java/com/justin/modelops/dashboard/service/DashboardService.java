package com.justin.modelops.dashboard.service;

import com.justin.modelops.config.RedisConfig;
import com.justin.modelops.dashboard.dto.DashboardSummaryResponse;
import com.justin.modelops.dashboard.dto.FastestModelEntry;
import com.justin.modelops.dashboard.dto.ModelDistributionResponse;
import com.justin.modelops.inference.dto.InferenceTaskResponse;
import com.justin.modelops.inference.enums.InferenceTaskStatus;
import com.justin.modelops.inference.mapper.InferenceTaskMapper;
import com.justin.modelops.inference.repository.InferenceTaskRepository;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.repository.AiModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only aggregation for dashboard views. The summary is cached in Redis with a
 * short TTL (see {@link RedisConfig}) to absorb repeated landing-page loads.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AiModelRepository modelRepository;
    private final InferenceTaskRepository taskRepository;
    private final InferenceTaskMapper taskMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = RedisConfig.DASHBOARD_CACHE, key = "'summary'")
    public DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                modelRepository.count(),
                modelRepository.countByStatus(ModelStatus.READY),
                modelRepository.countByStatus(ModelStatus.ARCHIVED),
                taskRepository.count(),
                taskRepository.countByStatus(InferenceTaskStatus.RUNNING),
                taskRepository.countByStatus(InferenceTaskStatus.SUCCEEDED),
                taskRepository.countByStatus(InferenceTaskStatus.FAILED),
                taskRepository.averageLatencyMsForSucceeded(),
                taskRepository.averageTokensPerSecondForSucceeded());
    }

    @Transactional(readOnly = true)
    public List<InferenceTaskResponse> recentTasks() {
        return taskRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = RedisConfig.DASHBOARD_CACHE, key = "'distribution'")
    public ModelDistributionResponse modelDistribution() {
        return new ModelDistributionResponse(
                toCountMap(modelRepository.countGroupedByModality()),
                toCountMap(modelRepository.countGroupedByFormat()));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = RedisConfig.DASHBOARD_CACHE, key = "'fastest-' + #limit")
    public List<FastestModelEntry> fastestModels(int limit) {
        return taskRepository.findFastestModels(PageRequest.of(0, limit)).stream()
                .map(row -> new FastestModelEntry(
                        (Long) row[0],
                        (String) row[1],
                        (Double) row[2],
                        (Double) row[3],
                        (Long) row[4]))
                .toList();
    }

    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : rows) {
            counts.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return counts;
    }
}
