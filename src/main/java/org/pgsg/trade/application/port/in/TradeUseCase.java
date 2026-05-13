package org.pgsg.trade.application.port.in;

import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.dto.result.TradeResult;

import java.util.List;
import java.util.UUID;

public interface TradeUseCase {

    void createTrade(CreateTradeCommand command);

    List<TradeResult> getTrades();

    TradeResult getTrade(UUID tradeId);

    CompleteTradeResult completeTrade(CompleteTradeCommand command);

}
