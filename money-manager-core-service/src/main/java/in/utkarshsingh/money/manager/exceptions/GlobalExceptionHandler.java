package in.utkarshsingh.money.manager.exceptions;

import in.utkarshsingh.money.manager.dto.ApiError;
import in.utkarshsingh.money.manager.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<ApiError> handleBusinessException(BaseAppException ex) {
        String traceId = getCorrelationId();
        log.warn("BusinessException | traceId={} | errorCode={} | message={}",
                traceId, ex.getErrorCode().name(), ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getErrorCode(), traceId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = getCorrelationId();
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        log.warn("ValidationException | traceId={} | message={}", traceId, message);
        return buildErrorResponse(message, ErrorCode.VALIDATION_ERROR, traceId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        String traceId = getCorrelationId();
        log.warn("IllegalArgumentException | traceId={} | message={}", traceId, ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ErrorCode.VALIDATION_ERROR, traceId);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String traceId = getCorrelationId();
        log.warn("DataIntegrityViolation | traceId={} | message={}", traceId, ex.getMessage());
        return buildErrorResponse("Duplicate or invalid data.", ErrorCode.DATA_INTEGRITY_VIOLATION, traceId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        String traceId = getCorrelationId();
        log.error("UnhandledException | traceId={} | message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse("Internal server error. Please try again later.", ErrorCode.INTERNAL_SERVER_ERROR, traceId);
    }

    private ResponseEntity<ApiError> buildErrorResponse(String message, ErrorCode errorCode, String traceId) {
        ApiError error = ApiError.builder()
                .success(false)
                .message(message)
                .status(errorCode.getHttpStatus().value())
                .errorCode(errorCode.name())
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(error);
    }

    private String getCorrelationId() {
        String id = MDC.get("correlationId");
        return id != null ? id : UUID.randomUUID().toString();
    }
}
