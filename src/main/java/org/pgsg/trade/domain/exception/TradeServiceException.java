package org.pgsg.trade.domain.exception;

import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorCode;

public class TradeServiceException extends CustomException {

    public TradeServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TradeServiceException(ErrorCode errorCode, String field) {
        super(errorCode, field);
    }

}
