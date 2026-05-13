package org.pgsg.trade.application.dto.result;

import org.pgsg.trade.domain.model.CancelReasonType;
import org.pgsg.trade.domain.model.Trade;
import org.pgsg.trade.domain.model.TradeHistory;
import org.pgsg.trade.domain.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CancelTradeResult(
		UUID tradeId,
		TradeStatus tradeStatus,
		CancelReasonType cancelReasonType,
		String cancelReasonDetail,
		LocalDateTime cancelledAt
) {
	public static CancelTradeResult from(Trade trade, TradeHistory tradeHistory) {
		return new CancelTradeResult(
				trade.getId(),
				trade.getStatus(),
				tradeHistory.getCancelReasonType(),
				tradeHistory.getCancelReasonDetail(),
				tradeHistory.getCreatedAt()
		);
	}
}
