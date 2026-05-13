package org.pgsg.trade.presentation.dto.response;

import org.pgsg.trade.application.dto.result.CancelTradeResult;
import org.pgsg.trade.domain.model.CancelReasonType;
import org.pgsg.trade.domain.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CancelTradeResponse(
		UUID tradeId,
		TradeStatus tradeStatus,
		CancelReasonType cancelReasonType,
		String cancelReasonDetail,
		LocalDateTime cancelledAt
) {
	public static CancelTradeResponse from(CancelTradeResult result) {
		return new CancelTradeResponse(
				result.tradeId(),
				result.tradeStatus(),
				result.cancelReasonType(),
				result.cancelReasonDetail(),
				result.cancelledAt()
		);
	}
}
