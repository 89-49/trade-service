package org.pgsg.trade.presentation.dto.request;

import org.pgsg.trade.application.dto.command.CancelTradeCommand;
import org.pgsg.trade.domain.model.CancelReasonType;

import java.util.UUID;

public record CancelTradeRequest(
		UUID cancelledBy,
		CancelReasonType cancelReasonType,
		String cancelReasonDetail
) {
	public CancelTradeCommand toCommand() {
		return new CancelTradeCommand(
				cancelledBy(),
				cancelReasonType(),
				cancelReasonDetail()
		);
	}
}
