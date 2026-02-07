package in.utkarshsingh.money.manager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiError {

    private boolean success;
    private String message;
    private int status;
    private String errorCode;
    private String traceId;
    private LocalDateTime timestamp;
}

