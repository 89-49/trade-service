package org.pgsg.trade.application.dto.result;

import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResult(
        UUID tradeId,
        UUID reservationId,
        TradeStatus tradeStatus,
        UUID buyerId,
        String buyerName,
        ParticipantStatus buyerStatus,
        UUID sellerId,
        String sellerName,
        ParticipantStatus sellerStatus,
        UUID productId,
        String productName,
        Long productPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TradeResult from(Trade trade) {
        return new TradeResult(
                trade.getId(),
                trade.getReservationId(),
                trade.getStatus(),
                trade.getParticipants().getBuyerId(),
                trade.getParticipants().getBuyerName(),
                trade.getBuyerStatus(),
                trade.getParticipants().getSellerId(),
                trade.getParticipants().getSellerName(),
                trade.getSellerStatus(),
                trade.getTradedItem().getProductId(),
                trade.getTradedItem().getProductName(),
                trade.getTradedItem().getProductPrice(),
                trade.getCreatedAt(),
                trade.getUpdatedAt()
        );
    }
}
