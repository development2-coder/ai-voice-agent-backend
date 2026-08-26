package com.infinitio.aivoiceplatform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic Pagination Response.
 *
 * @param <T> Response Payload
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * Records.
     */
    private List<T> content;

    /**
     * Total Records.
     */
    private long totalElements;

    /**
     * Total Pages.
     */
    private int totalPages;

    /**
     * Current Page.
     */
    private int pageNumber;

    /**
     * Page Size.
     */
    private int pageSize;

    /**
     * Is First Page.
     */
    private boolean first;

    /**
     * Is Last Page.
     */
    private boolean last;

}