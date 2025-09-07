package blog.sammi.lab.notes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Validation error details for a specific field")
public class ValidationError {
    
    @Schema(description = "Field name that failed validation", example = "email")
    private String field;
    
    @Schema(description = "Value that was provided for the field", example = "invalid-email")
    private Object value;
    
    @Schema(description = "Validation error message", example = "must be a well-formed email address")
    private String message;
}
