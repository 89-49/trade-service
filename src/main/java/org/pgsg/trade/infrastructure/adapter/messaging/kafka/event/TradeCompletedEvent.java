package org.pgsg.trade.infrastructure.adapter.messaging.kafka.event;

import org.pgsg.trade.domain.model.Trade;

import java.util.UUID;

public record TradeCompletedEvent(
        UUID tradeId,
        UUID reservationId,
        UUID productId
) {
    public static TradeCompletedEvent create(Trade trade) {
        return new TradeCompletedEvent(
                trade.getId(),
                trade.getReservationId(),
                trade.getTradedItem().getProductId()
        );
    }
}
