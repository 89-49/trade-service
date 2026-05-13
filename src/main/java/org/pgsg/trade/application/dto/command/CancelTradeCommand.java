package org.pgsg.trade.application.dto.command;

import org.pgsg.trade.domain.model.CancelReasonType;

import java.util.UUID;

public record CancelTradeCommand(
		UUID cancelledBy,
		CancelReasonType cancelReasonType,
		String cancelReasonDetail
) {
}
