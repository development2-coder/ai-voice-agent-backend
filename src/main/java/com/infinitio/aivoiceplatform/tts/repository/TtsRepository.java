package com.infinitio.aivoiceplatform.tts.repository;

import com.infinitio.aivoiceplatform.tts.entity.Tts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for TTS.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsRepository
        extends JpaRepository<Tts, Long> {

    Optional<Tts> findByPublicId(String publicId);

    boolean existsByTtsCode(String ttsCode);

    boolean existsByTtsName(String ttsName);
}