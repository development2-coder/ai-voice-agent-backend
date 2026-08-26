package com.infinitio.aivoiceplatform.voice.repository;

import com.infinitio.aivoiceplatform.voice.entity.Voice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Voice.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface VoiceRepository
        extends JpaRepository<Voice, Long> {

    Optional<Voice> findByPublicId(String publicId);

    boolean existsByVoiceCode(String voiceCode);

    boolean existsByVoiceName(String voiceName);
}