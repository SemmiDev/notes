package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
@Schema(description = "Pagination information")
public class Pagination {
    
    @Schema(description = "Current page number (0-based)", example = "0")
    private int currentPage;
    
    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;
    
    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;
    
    @Schema(description = "Total number of items", example = "47")
    private long totalItems;
    
    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
    
    @Schema(description = "Whether there is a next page", example = "true")
    private boolean hasNext;
    
    @Schema(description = "Whether there is a previous page", example = "false")
    private boolean hasPrevious;
    
    public static Pagination from(Page<?> page) {
        return Pagination.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
