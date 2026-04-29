package org.pgsg.trade.global.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorConfigProperties;
import org.pgsg.common.exception.GlobalExceptionAdvice;
import org.pgsg.common.response.ErrorResponse;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class TradeExceptionHandler implements GlobalExceptionAdvice {

    private final ErrorConfigProperties errorConfigProperties;

    @ExceptionHandler(TradeServiceException.class)
    public ResponseEntity<ErrorResponse> handleTradeException(CustomException e) {
        String errorKey = e.getErrorCode().getErrorKey();
        var detail = errorConfigProperties.getConfigs().get(errorKey);

        if (detail == null) {
            log.error("[TraceID: {}] Undefined Error Key: field={}, errorKey={}",
                    MDC.get("traceId"), e.getField(), errorKey, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM-500", "정의되지 않은 서버 에러가 발생했습니다."));
        }

        log.error("[TraceID: {}] CustomException: field={}, errorKey={}, message={}",
                MDC.get("traceId"), e.getField(), errorKey, detail.getMessage(), e);

        HttpStatus status = HttpStatus.resolve(detail.getStatus());
        if (status == null) {
            log.warn("[TraceID: {}] Invalid HTTP Status Code in config: status={}, errorKey={}",
                    MDC.get("traceId"), detail.getStatus(), errorKey);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status, detail.getCode(), detail.getMessage()));
    }

}
