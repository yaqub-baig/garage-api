package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    private List<T> content;           // Current page data
    private int pageNumber;            // Current page (0-indexed)
    private int pageSize;              // Items per page
    private long totalElements;        // Total items across all pages
    private int totalPages;            // Total number of pages
    private boolean first;             // Is this the first page?
    private boolean last;              // Is this the last page?
    private boolean empty;             // Is content empty?
    private int numberOfElements;      // Items in current page
}
