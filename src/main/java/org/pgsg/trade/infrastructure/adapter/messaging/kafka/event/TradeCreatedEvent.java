package org.pgsg.trade.infrastructure.adapter.messaging.kafka.event;

import org.pgsg.trade.domain.model.Trade;

import java.util.UUID;

public record TradeCreatedEvent(
        UUID tradeId,
        UUID productId,
        String productName,
        UUID sellerId,
        String sellerNickName,
        UUID buyerId,
        String buyerNickName
) {
    public static TradeCreatedEvent create(Trade trade) {
        return new TradeCreatedEvent(
                trade.getId(),
                trade.getTradedItem().getProductId(),
                trade.getTradedItem().getProductName(),
                trade.getParticipants().getSellerId(),
                trade.getParticipants().getSellerName(),
                trade.getParticipants().getBuyerId(),
                trade.getParticipants().getBuyerName()
        );
    }
}