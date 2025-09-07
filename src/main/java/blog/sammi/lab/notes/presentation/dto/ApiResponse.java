package blog.sammi.lab.notes.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {
    
    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message", example = "Operation completed successfully")
    private String message;
    
    @JsonProperty("error_code")
    @Schema(description = "Machine-readable error code", example = "USER_NOT_FOUND")
    private String errorCode;
    
    @Schema(description = "Response data")
    private T data;
    
    @Schema(description = "Additional metadata")
    private Map<String, Object> meta;
    
    @Schema(description = "List of validation errors")
    private List<ValidationError> errors;
    
    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operasi berhasil")
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data, Map<String, Object> meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .meta(meta)
                .build();
    }
    
    // Error responses
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(errorCode.getDefaultMessage())
                .errorCode(errorCode.getCode())
                .build();
    }
    
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode.getCode())
                .build();
    }
    
    public static <T> ApiResponse<T> error(ErrorCode errorCode, List<ValidationError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(errorCode.getDefaultMessage())
                .errorCode(errorCode.getCode())
                .errors(errors)
                .build();
    }
    
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message, List<ValidationError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode.getCode())
                .errors(errors)
                .build();
    }
    
    // Paginated responses
    public static <T> ApiResponse<T> successWithPagination(T data, Page<?> page) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operasi berhasil")
                .data(data)
                .meta(Map.of("pagination", Pagination.from(page), "timestamp", LocalDateTime.now()))
                .build();
    }
    
    public static <T> ApiResponse<T> successWithPagination(String message, T data, Page<?> page) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .meta(Map.of("pagination", Pagination.from(page), "timestamp", LocalDateTime.now()))
                .build();
    }
    
    public static <T> ApiResponse<T> successWithMeta(T data, Meta meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operasi berhasil")
                .data(data)
                .meta(convertMetaToMap(meta))
                .build();
    }
    
    public static <T> ApiResponse<T> successWithMeta(String message, T data, Meta meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .meta(convertMetaToMap(meta))
                .build();
    }
    
    private static Map<String, Object> convertMetaToMap(Meta meta) {
        Map<String, Object> metaMap = new HashMap<>();
        if (meta.getPagination() != null) {
            metaMap.put("pagination", meta.getPagination());
        }
        if (meta.getTimestamp() != null) {
            metaMap.put("timestamp", meta.getTimestamp());
        }
        if (meta.getProcessingTime() != null) {
            metaMap.put("processingTime", meta.getProcessingTime());
        }
        if (meta.getAdditional() != null) {
            metaMap.putAll(meta.getAdditional());
        }
        return metaMap;
    }
    
    // Legacy error method for backward compatibility
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .build();
    }
}
