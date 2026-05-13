package org.pgsg.trade.application.port.in;

import org.pgsg.trade.application.dto.command.CreateTradeCommand;
import org.pgsg.trade.application.dto.command.CompleteTradeCommand;
import org.pgsg.trade.application.dto.result.CompleteTradeResult;
import org.pgsg.trade.application.dto.result.TradeResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TradeUseCase {

    void createTrade(CreateTradeCommand command);

    Page<TradeResult> getTrades(Pageable pageable);

    TradeResult getTrade(UUID tradeId);

    CompleteTradeResult completeTrade(CompleteTradeCommand command);

}
