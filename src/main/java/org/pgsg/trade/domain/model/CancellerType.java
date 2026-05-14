package org.pgsg.trade.domain.model;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public enum CancellerType {
    SELLER("판매자"),
    BUYER("구매자"),
    SYSTEM("시스템"),

    ;

    private final String description;

    public static CancellerType find(UUID participantId, UUID buyerId, UUID sellerId) {
        return switch (participantId) {
			case UUID userId when userId.equals(buyerId) -> CancellerType.BUYER;
            case UUID userId when userId.equals(sellerId) -> CancellerType.SELLER;
			case null, default -> CancellerType.SYSTEM;
		};
    }
}
