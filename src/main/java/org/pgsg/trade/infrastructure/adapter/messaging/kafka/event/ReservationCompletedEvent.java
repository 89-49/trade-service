package org.pgsg.trade.infrastructure.adapter.messaging.kafka.event;

import org.pgsg.trade.application.dto.command.CreateTradeCommand;

import java.util.UUID;

public record ReservationCompletedEvent(
        UUID reservationId,
        UUID productId,
        String productName,
        Long productPrice,
        UUID sellerId,
        String sellerNickName,
        UUID buyerId,
        String buyerNickName
) {

    public CreateTradeCommand toCommand() {
        return new CreateTradeCommand(
                reservationId,
                productId,
                productName,
                productPrice,
                sellerId,
                sellerNickName,
                buyerId,
                buyerNickName
        );
    }
}
