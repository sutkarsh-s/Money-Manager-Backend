package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.dto.ApiError;
import in.utkarshsingh.money.manager.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles all business/domain exceptions
     */
    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<ApiError> handleBusinessException(BaseAppException ex) {

        ErrorCode errorCode = ex.getErrorCode();
        String traceId = generateTraceId();

        log.warn(
                "BusinessException | traceId={} | errorCode={} | message={}",
                traceId,
                errorCode.name(),
                ex.getMessage()
        );

        return buildErrorResponse(ex.getMessage(), errorCode, traceId);
    }

    /**
     * Handles validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {

        String traceId = generateTraceId();
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn(
                "ValidationException | traceId={} | message={}",
                traceId,
                message
        );

        return buildErrorResponse(message, errorCode, traceId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        String traceId = generateTraceId();
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        log.warn(
                "IllegalArgumentException | traceId={} | message={}",
                traceId,
                ex.getMessage()
        );

        return buildErrorResponse(ex.getMessage(), errorCode, traceId);
    }

    /**
     * Handles database constraint violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        String traceId = generateTraceId();
        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;

        log.warn(
                "DataIntegrityViolation | traceId={} | message={}",
                traceId,
                ex.getMessage()
        );

        return buildErrorResponse("Duplicate or invalid data.", errorCode, traceId);
    }

    /**
     * Handles unexpected system errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {

        String traceId = generateTraceId();
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error(
                "UnhandledException | traceId={} | message={}",
                traceId,
                ex.getMessage(),
                ex
        );

        return buildErrorResponse(
                "Internal server error. Please try again later.",
                errorCode,
                traceId
        );
    }

    /**
     * Common method to build ApiError response
     */
    private ResponseEntity<ApiError> buildErrorResponse(String message,
                                                        ErrorCode errorCode,
                                                        String traceId) {

        ApiError error = ApiError.builder()
                .success(false)
                .message(message)
                .status(errorCode.getHttpStatus().value())
                .errorCode(errorCode.name())
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(error);
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
