package blog.sammi.lab.notes.presentation.exception;

import blog.sammi.lab.notes.presentation.dto.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object[] args;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(ErrorCode errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}
