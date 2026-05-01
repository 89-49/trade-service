package org.pgsg.trade.application.dto.command;

import java.util.UUID;

public record CompleteTradeCommand(
        UUID tradeId,
        UUID participantId
) {
}
