package blog.sammi.lab.notes.infrastructure.config;

import blog.sammi.lab.notes.presentation.dto.ApiResponse;
import blog.sammi.lab.notes.presentation.dto.ErrorCode;
import blog.sammi.lab.notes.presentation.dto.ValidationError;
import blog.sammi.lab.notes.presentation.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    
    private final StructuredLogger structuredLogger;
    
    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logError("BusinessException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage());
        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logError("ValidationException", ex, request);
        
        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.VALIDATION_ERROR, errors);
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle authentication failures
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        
        logError("AuthenticationException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.AUTHENTICATION_FAILED);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle access denied
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logError("AccessDeniedException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.ACCESS_DENIED);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle database constraint violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logError("DataIntegrityViolationException", ex, request);
        
        ErrorCode errorCode = ErrorCode.DUPLICATE_RESOURCE;
        String message = "Data sudah ada atau melanggar constraint database";
        
        // Check for specific constraint violations
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        if (rootCause.contains("email")) {
            errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;
        } else if (rootCause.contains("username")) {
            errorCode = ErrorCode.USERNAME_ALREADY_EXISTS;
        }
        
        ApiResponse<Void> response = ApiResponse.error(errorCode, message);
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logError("MissingParameterException", ex, request);
        
        ValidationError error = ValidationError.builder()
                .field(ex.getParameterName())
                .value(null)
                .message("Parameter wajib diisi")
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR, 
                "Parameter yang diperlukan tidak ada", 
                List.of(error)
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logError("TypeMismatchException", ex, request);
        
        ValidationError error = ValidationError.builder()
                .field(ex.getName())
                .value(ex.getValue())
                .message("Tipe data tidak valid")
                .build();
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR, 
                "Format parameter tidak valid", 
                List.of(error)
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle HTTP method not supported
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logError("MethodNotSupportedException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.OPERATION_NOT_ALLOWED, 
                "Metode HTTP tidak didukung: " + ex.getMethod()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * Handle malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logError("MessageNotReadableException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR, 
                "Format JSON tidak valid"
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle 404 - No resource found (Spring Boot 3.x)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        logError("NoResourceFoundException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND, 
                "Endpoint tidak ditemukan: " + ex.getResourcePath()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle 404 - No handler found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logError("NoHandlerFoundException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND, 
                "Endpoint tidak ditemukan: " + ex.getRequestURL()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logError("UnhandledException", ex, request);
        
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Map field error to validation error
     */
    private ValidationError mapFieldError(FieldError fieldError) {
        return ValidationError.builder()
                .field(fieldError.getField())
                .value(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }
    
    /**
     * Map error code to HTTP status
     */
    private HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case AUTHENTICATION_FAILED, INVALID_CREDENTIALS, TOKEN_EXPIRED, INVALID_TOKEN -> HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED, INSUFFICIENT_PERMISSIONS -> HttpStatus.FORBIDDEN;
            case USER_NOT_FOUND, RESOURCE_NOT_FOUND, NOTE_NOT_FOUND, CATEGORY_NOT_FOUND, TAG_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USER_ALREADY_EXISTS, EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS, DUPLICATE_RESOURCE -> HttpStatus.CONFLICT;
            case TOO_MANY_REQUESTS, RATE_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    /**
     * Log error with structured logging
     */
    private void logError(String errorType, Exception ex, HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("errorType", errorType);
        context.put("requestUri", request.getRequestURI());
        context.put("requestMethod", request.getMethod());
        context.put("userAgent", request.getHeader("User-Agent"));
        context.put("remoteAddr", request.getRemoteAddr());
        
        structuredLogger.logError("API_ERROR", ex, context);
    }
}
