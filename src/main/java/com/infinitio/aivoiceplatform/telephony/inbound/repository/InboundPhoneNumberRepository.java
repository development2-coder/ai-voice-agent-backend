package com.infinitio.aivoiceplatform.telephony.inbound.repository;

import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for resolving phone numbers configured for inbound
 * telephony processing.
 *
 * <p>
 * This repository is intentionally separate from the existing
 * phone number repository so that the current outbound
 * implementation remains unchanged.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface InboundPhoneNumberRepository
        extends JpaRepository<PhoneNumber, Long> {

    /**
     * Finds an active phone number configured for inbound calls.
     *
     * <p>
     * A phone number can accept inbound calls when its direction
     * is either INBOUND or BOTH. A null direction is also accepted
     * for backward compatibility with existing phone number records.
     * </p>
     *
     * @param phoneNumbers candidate phone number representations
     * @param providerCode telephony provider code
     * @return matching inbound phone numbers
     */
    @Query("""
            SELECT phoneNumber
            FROM PhoneNumber phoneNumber
            WHERE phoneNumber.isDeleted = 0
              AND phoneNumber.isActive = 1
              AND UPPER(phoneNumber.provider) = UPPER(:providerCode)
              AND phoneNumber.phoneNumber IN :phoneNumbers
              AND (
                    UPPER(phoneNumber.direction) = 'INBOUND'
                    OR UPPER(phoneNumber.direction) = 'BOTH'
                    OR phoneNumber.direction IS NULL
                  )
            """)
    List<PhoneNumber> findInboundPhoneNumbers(
            @Param("phoneNumbers") List<String> phoneNumbers,
            @Param("providerCode") String providerCode
    );
}