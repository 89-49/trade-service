package org.pgsg.trade.presentation.dto.request;

import org.pgsg.trade.application.dto.command.CancelTradeCommand;
import org.pgsg.trade.domain.model.CancelReasonType;
import org.pgsg.trade.domain.model.CancellerType;

import java.util.UUID;

public record CancelTradeRequest(
		CancellerType cancelledBy,
		CancelReasonType cancelReasonType,
		String cancelReasonDetail
) {
	public CancelTradeCommand toCommand(UUID tradeId, UUID participantId) {
		return new CancelTradeCommand(
				tradeId,
				participantId,
				cancelledBy(),
				cancelReasonType(),
				cancelReasonDetail()
		);
	}
}
