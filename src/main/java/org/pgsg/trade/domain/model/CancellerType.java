package org.pgsg.trade.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CancellerType {
    SELLER("판매자"),
    BUYER("구매자"),
    SYSTEM("시스템"),

    ;

    private final String description;
}
