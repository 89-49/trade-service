package org.pgsg.trade.global.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorConfigProperties;
import org.pgsg.common.exception.GlobalExceptionAdvice;
import org.pgsg.common.response.ErrorResponse;
import org.pgsg.trade.domain.exception.TradeServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class TradeExceptionHandler implements GlobalExceptionAdvice {

    private final ErrorConfigProperties errorConfigProperties;

    @ExceptionHandler(TradeServiceException.class)
    public ErrorResponse handleTradeException(CustomException e) {
        String errorKey = e.getErrorCode().getErrorKey();
        var detail = errorConfigProperties.getConfigs().get(errorKey);

        if (detail == null) {
            log.error("Undefined Error Key: field={}, errorKey={}", e.getField(), errorKey, e);
            return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getField(), "정의되지 않은 서버 에러가 발생했습니다.");
        }

        log.error("TradeException: field={}, errorKey={}, message={}", e.getField(), errorKey, detail.getMessage(), e);
        return ErrorResponse.of(HttpStatus.valueOf(detail.getStatus()), e.getField(), detail.getMessage());
    }

}
