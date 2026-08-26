package com.infinitio.aivoiceplatform.stt.repository;

import com.infinitio.aivoiceplatform.stt.entity.Stt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for STT.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttRepository
        extends JpaRepository<Stt, Long> {

    Optional<Stt> findByPublicId(String publicId);

    boolean existsBySttCode(String sttCode);

    boolean existsBySttName(String sttName);
}