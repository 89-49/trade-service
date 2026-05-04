package org.pgsg.trade.application.port.in;

import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;

public interface TradeUseCase {

    void createTrade(CreateTradeCommand command);

    CompleteTradeResult completeTrade(CompleteTradeCommand command);

}
