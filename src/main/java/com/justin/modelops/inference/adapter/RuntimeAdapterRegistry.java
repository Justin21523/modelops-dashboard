package com.justin.modelops.inference.adapter;

import com.justin.modelops.runtime.enums.BackendType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Selects the appropriate {@link RuntimeAdapter} for a backend type. When no real
 * adapter supports the requested type, it falls back to the mock adapter so Phase 1
 * tasks always execute.
 */
@Slf4j
@Component
public class RuntimeAdapterRegistry {

    private final List<RuntimeAdapter> adapters;
    private final MockRuntimeAdapter mockAdapter;

    public RuntimeAdapterRegistry(List<RuntimeAdapter> adapters, MockRuntimeAdapter mockAdapter) {
        this.adapters = adapters;
        this.mockAdapter = mockAdapter;
    }

    public RuntimeAdapter resolve(BackendType backendType) {
        if (backendType == null) {
            return mockAdapter;
        }
        return adapters.stream()
                .filter(adapter -> adapter.supports(backendType))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("No adapter supports backend type {}; falling back to mock adapter", backendType);
                    return mockAdapter;
                });
    }
}
