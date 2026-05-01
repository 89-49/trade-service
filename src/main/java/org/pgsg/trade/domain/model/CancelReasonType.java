package org.pgsg.trade.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.trade.domain.exception.TradeDomainValidationException;
import org.pgsg.trade.domain.exception.TradeErrorCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CancelReasonType {
    // 공통 사유
    CHANGE_OF_MIND("단순 변심", Arrays.asList(CancellerType.SELLER, CancellerType.BUYER)),
    SCHEDULE_CONFLICT("일정 불가", Arrays.asList(CancellerType.SELLER, CancellerType.BUYER)),
    ETC("기타", Arrays.asList(CancellerType.SELLER, CancellerType.BUYER)),

    // 판매자 전용
    PRODUCT_ISSUE("상품 문제 발생", Collections.singletonList(CancellerType.SELLER)),
    DUPLICATE_REGISTRATION("중복 등록", Collections.singletonList(CancellerType.SELLER)),

    // 구매자 전용
    REFUND_REQUEST("환불 요청", Collections.singletonList(CancellerType.BUYER)),

    // 시스템 전용
    POLICY_VIOLATION("정책 위반 감지", Collections.singletonList(CancellerType.SYSTEM));

    private final String description;
    private final List<CancellerType> allowedCancellers;

    public static CancelReasonType from(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new TradeDomainValidationException(TradeErrorCode.CANCEL_REASON_INVALID));
    }

    public boolean allowedFor(CancellerType cancellerType) {
        return allowedCancellers.contains(cancellerType);
    }
}