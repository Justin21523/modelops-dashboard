package com.justin.modelops.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank @Size(max = 64) String name,
        @Size(max = 16) String color,
        @Size(max = 256) String description) {
}
