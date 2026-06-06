package com.justin.modelops.model;

import com.justin.modelops.config.JpaAuditingConfig;
import com.justin.modelops.model.dto.ModelFilter;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.entity.ModelFormat;
import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.model.repository.AiModelSpecifications;
import com.justin.modelops.model.repository.ModelFormatRepository;
import com.justin.modelops.support.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the model schema (migrated by Flyway), JPA auditing, and dynamic
 * specification filtering work end-to-end against a real PostgreSQL instance.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class AiModelRepositoryIT extends AbstractPostgresContainerTest {

    @Autowired
    private AiModelRepository modelRepository;
    @Autowired
    private ModelFormatRepository formatRepository;

    @Test
    void persistsModelAndPopulatesAuditFields() {
        AiModel saved = modelRepository.save(buildModel("Llama 3 8B", ModelModality.TEXT,
                ModelFormatType.GGUF, ModelStatus.READY));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("system");
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void filtersByModalityAndStatusAndKeyword() {
        modelRepository.save(buildModel("Llama 3 8B", ModelModality.TEXT, ModelFormatType.GGUF, ModelStatus.READY));
        modelRepository.save(buildModel("Qwen2-VL 7B", ModelModality.MULTIMODAL, ModelFormatType.SAFETENSORS, ModelStatus.READY));
        modelRepository.save(buildModel("BGE-M3", ModelModality.EMBEDDING, ModelFormatType.ONNX, ModelStatus.ARCHIVED));

        var byModality = modelRepository.findAll(
                AiModelSpecifications.withFilter(new ModelFilter(ModelModality.TEXT, null, null, null, null)),
                PageRequest.of(0, 10));
        assertThat(byModality.getContent()).extracting(AiModel::getName).containsExactly("Llama 3 8B");

        var byStatus = modelRepository.findAll(
                AiModelSpecifications.withFilter(new ModelFilter(null, null, ModelStatus.READY, null, null)),
                PageRequest.of(0, 10));
        assertThat(byStatus.getTotalElements()).isEqualTo(2);

        var byKeyword = modelRepository.findAll(
                AiModelSpecifications.withFilter(new ModelFilter(null, null, null, "qwen", null)),
                PageRequest.of(0, 10));
        assertThat(byKeyword.getContent()).extracting(AiModel::getName).containsExactly("Qwen2-VL 7B");

        var byFormat = modelRepository.findAll(
                AiModelSpecifications.withFilter(new ModelFilter(null, ModelFormatType.ONNX, null, null, null)),
                PageRequest.of(0, 10));
        assertThat(byFormat.getContent()).extracting(AiModel::getName).containsExactly("BGE-M3");
    }

    private AiModel buildModel(String name, ModelModality modality, ModelFormatType formatType, ModelStatus status) {
        ModelFormat format = formatRepository.findByType(formatType).orElseThrow();
        AiModel model = new AiModel();
        model.setName(name);
        model.setProvider("Test Provider");
        model.setModality(modality);
        model.setFormat(format);
        model.setQuantization(QuantizationType.NONE);
        model.setStorageNote("alias://models/" + name.toLowerCase().replace(' ', '-'));
        model.setStatus(status);
        return model;
    }
}
