package org.pgsg.trade.application.dto.result;

import org.pgsg.trade.domain.model.ParticipantStatus;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeStatus;

import java.util.UUID;

public record CompleteTradeResult(
        UUID tradeId,
        TradeStatus tradeStatus,
        ParticipantStatus buyerStatus,
        ParticipantStatus sellerStatus,
        boolean tradeCompleted,
        boolean eventPublished
) {

    public static CompleteTradeResult from(Trade trade, boolean eventPublished) {
        return new CompleteTradeResult(
                trade.getId(),
                trade.getStatus(),
                trade.getBuyerStatus(),
                trade.getSellerStatus(),
                trade.getStatus() == TradeStatus.COMPLETED,
                eventPublished
        );
    }
}
