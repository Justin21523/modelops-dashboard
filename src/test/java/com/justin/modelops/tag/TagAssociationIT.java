package com.justin.modelops.tag;

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
import com.justin.modelops.tag.entity.Tag;
import com.justin.modelops.tag.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the model_tags many-to-many association persists through the join table and is
 * usable for tag-based filtering against a real PostgreSQL instance.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class TagAssociationIT extends AbstractPostgresContainerTest {

    @Autowired
    private AiModelRepository modelRepository;
    @Autowired
    private ModelFormatRepository formatRepository;
    @Autowired
    private TagRepository tagRepository;

    @Test
    void modelTagAssociationPersistsAndIsFilterable() {
        Tag fast = new Tag();
        fast.setName("fast");
        fast.setColor("#22c55e");
        tagRepository.save(fast);

        ModelFormat gguf = formatRepository.findByType(ModelFormatType.GGUF).orElseThrow();
        AiModel model = new AiModel();
        model.setName("Llama 3 8B");
        model.setModality(ModelModality.TEXT);
        model.setFormat(gguf);
        model.setQuantization(QuantizationType.Q4_K_M);
        model.setStatus(ModelStatus.READY);
        model.addTag(fast);
        AiModel saved = modelRepository.saveAndFlush(model);

        AiModel reloaded = modelRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTags()).extracting(Tag::getName).containsExactly("fast");

        var filtered = modelRepository.findAll(
                AiModelSpecifications.withFilter(new ModelFilter(null, null, null, null, fast.getId())),
                PageRequest.of(0, 10));
        assertThat(filtered.getContent()).extracting(AiModel::getName).containsExactly("Llama 3 8B");
    }
}
