package com.infinitio.aivoiceplatform.phonenumber.repository;

import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Phone Number.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface PhoneNumberRepository
        extends JpaRepository<PhoneNumber, Long> {

    Optional<PhoneNumber> findByPublicId(
            String publicId
    );

    boolean existsByPhoneNumber(
            String phoneNumber
    );

    boolean existsByProviderNumberId(
            String providerNumberId
    );
}