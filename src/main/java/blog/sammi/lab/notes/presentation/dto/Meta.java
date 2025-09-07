package blog.sammi.lab.notes.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response metadata")
public class Meta {
    
    @Schema(description = "Pagination information")
    private Pagination pagination;
    
    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;
    
    @Schema(description = "Request processing time in milliseconds")
    private Long processingTime;
    
    @Schema(description = "Additional metadata")
    private Map<String, Object> additional;
    
    // Factory methods for common use cases
    public static Meta withPagination(Page<?> page) {
        return Meta.builder()
                .pagination(Pagination.from(page))
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static Meta withTimestamp() {
        return Meta.builder()
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static Meta withProcessingTime(long processingTime) {
        return Meta.builder()
                .timestamp(LocalDateTime.now())
                .processingTime(processingTime)
                .build();
    }
    
    public static Meta withPaginationAndProcessingTime(Page<?> page, long processingTime) {
        return Meta.builder()
                .pagination(Pagination.from(page))
                .timestamp(LocalDateTime.now())
                .processingTime(processingTime)
                .build();
    }
}
