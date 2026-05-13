package org.pgsg.trade.infrastructure.adapter.messaging.kafka.event;

import org.pgsg.trade.domain.model.CancelReasonType;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeHistory;

import java.time.LocalDateTime;
import java.util.UUID;

public record TradeCancelledEvent(
		UUID tradeId,
		UUID reservationId,
		UUID productId,
		UUID sellerId,
		UUID buyerId,
		CancelReasonType cancelReasonType,
		LocalDateTime occurredAt
) {
	public static TradeCancelledEvent create(Trade trade, TradeHistory tradeHistory) {
		return new TradeCancelledEvent(
				trade.getId(),
				trade.getReservationId(),
				trade.getTradedItem().getProductId(),
				trade.getParticipants().getSellerId(),
				trade.getParticipants().getBuyerId(),
				tradeHistory.getCancelReasonType(),
				tradeHistory.getCreatedAt()
		);
	}
}
