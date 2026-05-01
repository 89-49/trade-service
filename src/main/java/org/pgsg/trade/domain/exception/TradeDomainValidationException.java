package org.pgsg.trade.domain.exception;

import org.pgsg.common.exception.ErrorCode;

public class TradeDomainValidationException extends TradeServiceException {

    public TradeDomainValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TradeDomainValidationException(ErrorCode errorCode, String field) {
        super(errorCode, field);
    }

}
