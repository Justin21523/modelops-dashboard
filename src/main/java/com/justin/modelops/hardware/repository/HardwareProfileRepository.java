package com.justin.modelops.hardware.repository;

import com.justin.modelops.hardware.entity.HardwareProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HardwareProfileRepository extends JpaRepository<HardwareProfile, Long> {
}
