package org.pgsg.trade.application.dto.command;

import org.pgsg.trade.domain.model.CancelReasonType;
import org.pgsg.trade.domain.model.CancellerType;

import java.util.UUID;

public record CancelTradeCommand(
		UUID tradeId,
		UUID participantId,
		CancellerType cancelledBy,
		CancelReasonType cancelReasonType,
		String cancelReasonDetail
) {
}
