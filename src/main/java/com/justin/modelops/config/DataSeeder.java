package com.justin.modelops.config;

import com.justin.modelops.hardware.entity.HardwareProfile;
import com.justin.modelops.hardware.enums.HardwareBackendType;
import com.justin.modelops.hardware.repository.HardwareProfileRepository;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.entity.ModelFormat;
import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.model.repository.ModelFormatRepository;
import com.justin.modelops.runtime.entity.RuntimeBackend;
import com.justin.modelops.runtime.enums.BackendStatus;
import com.justin.modelops.runtime.enums.BackendType;
import com.justin.modelops.runtime.repository.RuntimeBackendRepository;
import com.justin.modelops.user.entity.Role;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.enums.UserRole;
import com.justin.modelops.user.repository.RoleRepository;
import com.justin.modelops.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds dev-friendly sample data on startup when {@code app.seed.enabled=true}.
 * Storage locations use safe aliases ({@code alias://...}); no real host paths or
 * secrets are written. Seeding is skipped if any models already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "demo-password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelFormatRepository formatRepository;
    private final AiModelRepository modelRepository;
    private final HardwareProfileRepository hardwareRepository;
    private final RuntimeBackendRepository runtimeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (modelRepository.count() > 0) {
            log.info("Sample data already present; skipping seeding");
            return;
        }
        log.info("Seeding development sample data");
        seedDemoUser();
        seedHardware();
        seedRuntimes();
        seedModels();
    }

    private void seedDemoUser() {
        if (userRepository.existsByUsername(DEMO_USERNAME)) {
            return;
        }
        Role userRole = roleRepository.findByName(UserRole.USER).orElseThrow();
        Role adminRole = roleRepository.findByName(UserRole.ADMIN).orElseThrow();
        User demo = new User();
        demo.setUsername(DEMO_USERNAME);
        demo.setEmail("demo@example.com");
        demo.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        demo.setDisplayName("Demo User");
        demo.addRole(userRole);
        demo.addRole(adminRole);
        userRepository.save(demo);
        log.info("Seeded demo user '{}' (password: '{}')", DEMO_USERNAME, DEMO_PASSWORD);
    }

    private void seedHardware() {
        HardwareProfile workstation = new HardwareProfile();
        workstation.setName("Local Workstation");
        workstation.setGpuModel("NVIDIA RTX 4090");
        workstation.setVramMb(24576);
        workstation.setRamMb(65536);
        workstation.setBackendType(HardwareBackendType.CUDA);
        workstation.setOperatingSystem("Ubuntu 24.04");
        workstation.setDriverNotes("CUDA 12.x, driver 550+");
        hardwareRepository.save(workstation);
    }

    private void seedRuntimes() {
        RuntimeBackend mock = new RuntimeBackend();
        mock.setName("Mock Runtime");
        mock.setBackendType(BackendType.MOCK);
        mock.setStatus(BackendStatus.AVAILABLE);
        mock.setCapabilities("text-generation,simulation");
        mock.setDescription("Simulated runtime for Phase 1 development");
        runtimeRepository.save(mock);

        RuntimeBackend llama = new RuntimeBackend();
        llama.setName("llama.cpp (local)");
        llama.setBackendType(BackendType.LLAMA_CPP);
        llama.setStatus(BackendStatus.UNKNOWN);
        llama.setEndpointUrl("http://localhost:8081");
        llama.setCapabilities("text-generation");
        llama.setDescription("Placeholder backend; real adapter arrives in a later phase");
        runtimeRepository.save(llama);
    }

    private void seedModels() {
        ModelFormat gguf = formatRepository.findByType(ModelFormatType.GGUF).orElseThrow();
        ModelFormat safetensors = formatRepository.findByType(ModelFormatType.SAFETENSORS).orElseThrow();

        AiModel llama = new AiModel();
        llama.setName("Llama 3 8B Instruct");
        llama.setFamily("Llama 3");
        llama.setProvider("Meta");
        llama.setModality(ModelModality.TEXT);
        llama.setFormat(gguf);
        llama.setQuantization(QuantizationType.Q4_K_M);
        llama.setParameterSize("8B");
        llama.setEstimatedVramMb(6144);
        llama.setLicense("Llama 3 Community License");
        llama.setSourceUrl("https://huggingface.co/meta-llama");
        llama.setStorageNote("alias://models/llama3-8b-instruct");
        llama.setStatus(ModelStatus.READY);
        modelRepository.save(llama);

        AiModel qwenVl = new AiModel();
        qwenVl.setName("Qwen2-VL 7B");
        qwenVl.setFamily("Qwen2-VL");
        qwenVl.setProvider("Alibaba");
        qwenVl.setModality(ModelModality.MULTIMODAL);
        qwenVl.setFormat(safetensors);
        qwenVl.setQuantization(QuantizationType.FP16);
        qwenVl.setParameterSize("7B");
        qwenVl.setEstimatedVramMb(16384);
        qwenVl.setLicense("Apache-2.0");
        qwenVl.setSourceUrl("https://huggingface.co/Qwen");
        qwenVl.setStorageNote("alias://models/qwen2-vl-7b");
        qwenVl.setStatus(ModelStatus.READY);
        modelRepository.save(qwenVl);

        AiModel embed = new AiModel();
        embed.setName("BGE-M3 Embedding");
        embed.setFamily("BGE");
        embed.setProvider("BAAI");
        embed.setModality(ModelModality.EMBEDDING);
        embed.setFormat(safetensors);
        embed.setQuantization(QuantizationType.NONE);
        embed.setParameterSize("560M");
        embed.setEstimatedVramMb(2048);
        embed.setLicense("MIT");
        embed.setStorageNote("alias://models/bge-m3");
        embed.setStatus(ModelStatus.ARCHIVED);
        modelRepository.save(embed);
    }
}
