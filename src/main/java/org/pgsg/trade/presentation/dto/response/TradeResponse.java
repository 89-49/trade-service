package org.pgsg.trade.presentation.dto.response;

import org.pgsg.trade.application.dto.result.TradeResult;
import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResponse(
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

    public static TradeResponse from(TradeResult result) {
        return new TradeResponse(
                result.tradeId(),
                result.reservationId(),
                result.tradeStatus(),
                result.buyerId(),
                result.buyerName(),
                result.buyerStatus(),
                result.sellerId(),
                result.sellerName(),
                result.sellerStatus(),
                result.productId(),
                result.productName(),
                result.productPrice(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
