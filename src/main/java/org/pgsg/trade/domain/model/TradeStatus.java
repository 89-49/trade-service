package org.pgsg.trade.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TradeStatus {
    TRADING("거래 시작"),
    COMPLETED("거래 완료"),
    CANCELLED("거래 취소"),

    ;

    private final String description;
}
