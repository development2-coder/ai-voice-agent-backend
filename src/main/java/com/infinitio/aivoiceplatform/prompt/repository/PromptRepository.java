package com.infinitio.aivoiceplatform.prompt.repository;

import com.infinitio.aivoiceplatform.prompt.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Prompt.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface PromptRepository
        extends JpaRepository<Prompt, Long> {

    Optional<Prompt> findByPublicId(String publicId);

    boolean existsByPromptCode(String promptCode);

    boolean existsByPromptName(String promptName);
}