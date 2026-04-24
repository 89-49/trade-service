package org.pgsg.trade.domain.exception;

// TODO: 추후 Common의 Exception을 상속받도록 수정
public class TradeDomainValidationException extends RuntimeException{

    public TradeDomainValidationException(String message) {
        super(message);
    }

}
